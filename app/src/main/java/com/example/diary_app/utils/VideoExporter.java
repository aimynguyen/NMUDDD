package com.example.diary_app.utils;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class VideoExporter {

    public static final int WIDTH             = 720;
    public static final int HEIGHT            = 1280;
    public static final int FPS               = 15;
    public static final int SECONDS_PER_IMAGE = 1;
    private static final int BIT_RATE         = 2_000_000;

    public interface ExportListener {
        void onProgress(String msg);
        void onSuccess(Uri uri);
        void onError(String msg);
    }

    // ── Entry point ───────────────────────────────────────────────────────────
    public static void export(Context ctx, List<Bitmap> bitmaps, int audioRawResId,
                              ExportListener cb) {
        new Thread(() -> {
            try { run(ctx, bitmaps, audioRawResId, cb); }
            catch (Exception e) {
                // In log để debug
                android.util.Log.e("VideoExporter", "Export failed", e);
                String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                post(cb, msg);
            }
        }).start();
    }

    // ── Core ──────────────────────────────────────────────────────────────────
    private static void run(Context ctx, List<Bitmap> bitmaps, int audioRawResId,
                            ExportListener cb) throws Exception {

        // ── 1. Encode video frames ────────────────────────────────────────────
        progress(cb, "Đang mã hóa ảnh...");

        MediaFormat vFmt = MediaFormat.createVideoFormat("video/avc", WIDTH, HEIGHT);
        // Bắt buộc dùng YUV420SemiPlanar (NV12) thay vì Flexible để tránh lỗi xọc màu/sai vị trí pixel
        vFmt.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
        vFmt.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        vFmt.setInteger(MediaFormat.KEY_FRAME_RATE, FPS);
        vFmt.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

        MediaCodec vEnc = MediaCodec.createEncoderByType("video/avc");
        vEnc.configure(vFmt, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        vEnc.start();

        List<byte[]>  vData  = new ArrayList<>();
        List<Long>    vPts   = new ArrayList<>();
        List<Integer> vFlags = new ArrayList<>();
        MediaFormat   vOut   = null;

        long ptsUs = 0;
        long frameDur = 1_000_000L / FPS;
        int  framesPerImg = FPS * SECONDS_PER_IMAGE;
        MediaCodec.BufferInfo bi = new MediaCodec.BufferInfo();

        for (int idx = 0; idx < bitmaps.size(); idx++) {
            progress(cb, "Xử lý ảnh " + (idx+1) + "/" + bitmaps.size() + "...");
            Bitmap sc = Bitmap.createScaledBitmap(bitmaps.get(idx), WIDTH, HEIGHT, true);
            byte[] yuv = toNV12(sc, WIDTH, HEIGHT);
            sc.recycle();

            for (int f = 0; f < framesPerImg; f++) {
                int inId = vEnc.dequeueInputBuffer(100_000);
                if (inId >= 0) {
                    ByteBuffer b = vEnc.getInputBuffer(inId);
                    if (b != null) {
                        b.clear(); b.put(yuv);
                        vEnc.queueInputBuffer(inId, 0, yuv.length, ptsUs, 0);
                    }
                    ptsUs += frameDur;
                }
                vOut = drainEnc(vEnc, bi, vData, vPts, vFlags, vOut);
            }
        }

        // EOS
        int inId = vEnc.dequeueInputBuffer(100_000);
        if (inId >= 0)
            vEnc.queueInputBuffer(inId, 0, 0, ptsUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
        vOut = drainEncFull(vEnc, bi, vData, vPts, vFlags, vOut);
        vEnc.stop(); vEnc.release();

        if (vOut == null) throw new Exception("Video encoder không trả về output format");
        if (vData.isEmpty()) throw new Exception("Không có frame video nào được encode");

        // ── 2. Transcode audio → AAC ──────────────────────────────────────────
        progress(cb, "Chuyển đổi âm thanh sang AAC...");
        AudioTranscoder.AacResult aac = AudioTranscoder.transcode(ctx, audioRawResId);

        // ── 3. Mux video + AAC ────────────────────────────────────────────────
        progress(cb, "Ghép video và âm thanh...");

        File tmp = new File(ctx.getCacheDir(), "diary_" + System.currentTimeMillis() + ".mp4");
        MediaMuxer muxer = new MediaMuxer(tmp.getAbsolutePath(),
                MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

        int vTrack = muxer.addTrack(vOut);
        int aTrack = muxer.addTrack(aac.format);  // AAC format → không còn lỗi
        muxer.start();

        // Đan xen (Interleave) Video và Audio để tạo file MP4 chuẩn
        MediaCodec.BufferInfo vbi = new MediaCodec.BufferInfo();
        MediaCodec.BufferInfo abi = new MediaCodec.BufferInfo();

        int vIndex = 0;
        long writtenAudio = 0;
        int aacSize = aac.samples.size();
        long videoDurUs = ptsUs;

        while (vIndex < vData.size() || (writtenAudio < videoDurUs && aacSize > 0)) {
            // Write Video chunk
            if (vIndex < vData.size()) {
                ByteBuffer buf = ByteBuffer.wrap(vData.get(vIndex));
                vbi.offset = 0; vbi.size = vData.get(vIndex).length;
                vbi.presentationTimeUs = vPts.get(vIndex); 
                vbi.flags = vFlags.get(vIndex); // QUAN TRỌNG: Cờ Keyframe
                muxer.writeSampleData(vTrack, buf, vbi);
                vIndex++;
            }

            // Write Audio chunk
            if (writtenAudio < videoDurUs && aacSize > 0) {
                // Ghi 1-2 frame audio tương ứng với thời lượng video vừa ghi
                // Để đơn giản, ghi vài frame audio sau mỗi frame video
                for (int j = 0; j < 3 && writtenAudio < videoDurUs; j++) {
                    int aIdx = (int) ((writtenAudio / 21_333L) % aacSize);
                    byte[] s = aac.samples.get(aIdx);
                    ByteBuffer buf = ByteBuffer.wrap(s);
                    abi.offset = 0; abi.size = s.length;
                    abi.presentationTimeUs = writtenAudio; 
                    abi.flags = 1; // BUFFER_FLAG_KEY_FRAME cho Audio
                    muxer.writeSampleData(aTrack, buf, abi);

                    long dur = (aIdx + 1 < aacSize)
                            ? (aac.timestamps.get(aIdx + 1) - aac.timestamps.get(aIdx))
                            : 21_333L;
                    if (dur <= 0) dur = 21_333L;
                    writtenAudio += dur;
                }
            }
        }

        muxer.stop(); muxer.release();

        // ── 4. Lưu vào Gallery ────────────────────────────────────────────────
        progress(cb, "Lưu vào thư viện...");
        Uri uri = saveToGallery(ctx, tmp);
        tmp.delete();

        if (uri != null) {
            new Handler(Looper.getMainLooper()).post(() -> { if (cb != null) cb.onSuccess(uri); });
        } else {
            throw new Exception("Không lưu được vào thư viện");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static MediaFormat drainEnc(MediaCodec enc, MediaCodec.BufferInfo bi,
                                         List<byte[]> data, List<Long> pts, List<Integer> flags, MediaFormat fmt) {
        while (true) {
            int id = enc.dequeueOutputBuffer(bi, 0);
            if (id == MediaCodec.INFO_TRY_AGAIN_LATER) break;
            if (id == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) { fmt = enc.getOutputFormat(); continue; }
            if (id >= 0) {
                ByteBuffer ob = enc.getOutputBuffer(id);
                if ((bi.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0 && bi.size > 0 && ob != null)
                    collect(ob, bi, data, pts, flags);
                enc.releaseOutputBuffer(id, false);
            }
        }
        return fmt;
    }

    private static MediaFormat drainEncFull(MediaCodec enc, MediaCodec.BufferInfo bi,
                                             List<byte[]> data, List<Long> pts, List<Integer> flags, MediaFormat fmt) {
        while (true) {
            int id = enc.dequeueOutputBuffer(bi, 100_000);
            if (id == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) { fmt = enc.getOutputFormat(); continue; }
            if (id >= 0) {
                boolean eos = (bi.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
                ByteBuffer ob = enc.getOutputBuffer(id);
                if ((bi.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0 && bi.size > 0 && ob != null)
                    collect(ob, bi, data, pts, flags);
                enc.releaseOutputBuffer(id, false);
                if (eos) break;
            } else break;
        }
        return fmt;
    }

    private static void collect(ByteBuffer buf, MediaCodec.BufferInfo bi,
                                 List<byte[]> data, List<Long> pts, List<Integer> flags) {
        if (buf == null) return;  // guard null buffer
        byte[] copy = new byte[bi.size];
        buf.position(bi.offset); buf.limit(bi.offset + bi.size);
        buf.get(copy);
        data.add(copy); pts.add(bi.presentationTimeUs); flags.add(bi.flags);
    }

    /** Bitmap ARGB → NV12 (YUV420 semi-planar) */
    private static byte[] toNV12(Bitmap bmp, int w, int h) {
        int[] argb = new int[w * h];
        bmp.getPixels(argb, 0, w, 0, 0, w, h);
        byte[] yuv = new byte[w * h * 3 / 2];
        int uv = w * h;
        for (int i = 0; i < h; i++) for (int j = 0; j < w; j++) {
            int p = argb[i*w+j], r=(p>>16)&0xff, g=(p>>8)&0xff, b=p&0xff;
            yuv[i*w+j] = (byte)(((66*r+129*g+25*b+128)>>8)+16);
            if ((i&1)==0 && (j&1)==0) {
                yuv[uv+(i/2)*w+j]   = (byte)(((-38*r-74*g+112*b+128)>>8)+128);
                yuv[uv+(i/2)*w+j+1] = (byte)(((112*r-94*g-18*b+128)>>8)+128);
            }
        }
        return yuv;
    }

    private static Uri saveToGallery(Context ctx, File src) {
        try {
            String name = "diary_" + System.currentTimeMillis() + ".mp4";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues cv = new ContentValues();
                cv.put(MediaStore.Video.Media.DISPLAY_NAME, name);
                cv.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
                cv.put(MediaStore.Video.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_DCIM + "/DiaryApp");
                cv.put(MediaStore.Video.Media.IS_PENDING, 1);
                Uri uri = ctx.getContentResolver()
                        .insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, cv);
                if (uri == null) return null;
                try (InputStream in = new FileInputStream(src);
                     OutputStream out = ctx.getContentResolver().openOutputStream(uri)) {
                    byte[] buf = new byte[8192]; int n;
                    while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
                }
                cv.clear(); cv.put(MediaStore.Video.Media.IS_PENDING, 0);
                ctx.getContentResolver().update(uri, cv, null, null);
                return uri;
            } else {
                File dir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM), "DiaryApp");
                dir.mkdirs();
                File dest = new File(dir, name);
                try (InputStream in = new FileInputStream(src);
                     OutputStream out = new FileOutputStream(dest)) {
                    byte[] buf = new byte[8192]; int n;
                    while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
                }
                android.media.MediaScannerConnection.scanFile(ctx,
                        new String[]{dest.getAbsolutePath()}, new String[]{"video/mp4"}, null);
                return Uri.fromFile(dest);
            }
        } catch (Exception e) { return null; }
    }

    private static void progress(ExportListener cb, String msg) {
        new Handler(Looper.getMainLooper()).post(() -> { if (cb != null) cb.onProgress(msg); });
    }

    private static void post(ExportListener cb, String err) {
        new Handler(Looper.getMainLooper()).post(() -> { if (cb != null) cb.onError("Lỗi: " + err); });
    }
}
