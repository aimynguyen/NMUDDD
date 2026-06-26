package com.example.diary_app.utils;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/** Decode bất kỳ định dạng audio (MP3/OGG/...) → AAC để dùng trong MediaMuxer MP4 */
public class AudioTranscoder {

    public static class AacResult {
        public final MediaFormat format;
        public final List<byte[]> samples;
        public final List<Long>   timestamps;
        public final long         durationUs;

        AacResult(MediaFormat fmt, List<byte[]> s, List<Long> ts) {
            this.format     = fmt;
            this.samples    = s;
            this.timestamps = ts;
            this.durationUs = ts.isEmpty() ? 0 : ts.get(ts.size() - 1) + 21_333L;
        }
    }

    public static AacResult transcode(Context ctx, int rawResId) throws Exception {
        // 1. Copy raw resource → temp file (MediaExtractor cần file path)
        File tmp = copyRaw(ctx, rawResId);
        MediaExtractor ex = new MediaExtractor();
        ex.setDataSource(tmp.getAbsolutePath());

        // 2. Tìm audio track
        int srcIdx = -1; MediaFormat srcFmt = null;
        for (int i = 0; i < ex.getTrackCount(); i++) {
            MediaFormat f = ex.getTrackFormat(i);
            if (f.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                srcIdx = i; srcFmt = f; break;
            }
        }
        if (srcIdx < 0) throw new Exception("Không tìm thấy track audio");
        ex.selectTrack(srcIdx);

        int sampleRate = srcFmt.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        int channels   = srcFmt.getInteger(MediaFormat.KEY_CHANNEL_COUNT);

        // 3. Decoder (MP3/OGG → PCM)
        MediaCodec dec = MediaCodec.createDecoderByType(srcFmt.getString(MediaFormat.KEY_MIME));
        dec.configure(srcFmt, null, null, 0);
        dec.start();

        // 4. AAC Encoder (PCM → AAC)
        MediaFormat aacFmt = MediaFormat.createAudioFormat("audio/mp4a-latm", sampleRate, channels);
        aacFmt.setInteger(MediaFormat.KEY_BIT_RATE, 128_000);
        aacFmt.setInteger(MediaFormat.KEY_AAC_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        MediaCodec enc = MediaCodec.createEncoderByType("audio/mp4a-latm");
        enc.configure(aacFmt, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        enc.start();

        // 5. Transcode loop
        List<byte[]> aacSamples = new ArrayList<>();
        List<Long>   aacPts     = new ArrayList<>();
        MediaFormat  aacOutFmt  = null;

        MediaCodec.BufferInfo decInfo = new MediaCodec.BufferInfo();
        MediaCodec.BufferInfo encInfo = new MediaCodec.BufferInfo();
        boolean extractDone = false, decodeDone = false;

        while (!decodeDone) {
            // Feed extractor → decoder
            if (!extractDone) {
                int inId = dec.dequeueInputBuffer(10_000);
                if (inId >= 0) {
                    ByteBuffer buf = dec.getInputBuffer(inId);
                    buf.clear();
                    int sz = ex.readSampleData(buf, 0);
                    if (sz < 0) {
                        dec.queueInputBuffer(inId, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        extractDone = true;
                    } else {
                        dec.queueInputBuffer(inId, 0, sz, ex.getSampleTime(), 0);
                        ex.advance();
                    }
                }
            }

            // Drain decoder PCM → feed encoder
            int outId = dec.dequeueOutputBuffer(decInfo, 10_000);
            if (outId >= 0) {
                boolean eos = (decInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
                ByteBuffer pcm = dec.getOutputBuffer(outId);
                if (decInfo.size > 0 && pcm != null) {
                    byte[] pcmBytes = new byte[decInfo.size];
                    pcm.position(decInfo.offset); pcm.get(pcmBytes);

                    int offset = 0;
                    while (offset < pcmBytes.length) {
                        int encIn = enc.dequeueInputBuffer(10_000);
                        if (encIn >= 0) {
                            ByteBuffer eb = enc.getInputBuffer(encIn);
                            if (eb != null) {
                                eb.clear();
                                int chunkSize = Math.min(eb.remaining(), pcmBytes.length - offset);
                                eb.put(pcmBytes, offset, chunkSize);
                                offset += chunkSize;
                                
                                boolean isEosChunk = (offset == pcmBytes.length) && eos;
                                enc.queueInputBuffer(encIn, 0, chunkSize,
                                        decInfo.presentationTimeUs,
                                        isEosChunk ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);
                            }
                        }
                        // Drain encoder liên tục để giải phóng input buffer
                        aacOutFmt = drainEncoder(enc, encInfo, aacSamples, aacPts, aacOutFmt);
                    }
                } else if (eos) {
                    // Xử lý cờ EOS khi không có data
                    int encIn = enc.dequeueInputBuffer(10_000);
                    if (encIn >= 0) {
                        enc.queueInputBuffer(encIn, 0, 0, decInfo.presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    }
                }
                dec.releaseOutputBuffer(outId, false);
                if (eos) decodeDone = true;
            }

            // Drain encoder → AAC samples
            aacOutFmt = drainEncoder(enc, encInfo, aacSamples, aacPts, aacOutFmt);
        }

        // Drain encoder còn sót
        boolean encEos = false;
        while (!encEos) {
            int id = enc.dequeueOutputBuffer(encInfo, 100_000);
            if (id == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                aacOutFmt = enc.getOutputFormat();
            } else if (id >= 0) {
                encEos = (encInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
                ByteBuffer ob = enc.getOutputBuffer(id);
                if ((encInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0
                        && encInfo.size > 0 && ob != null)
                    collectSample(ob, encInfo, aacSamples, aacPts);
                enc.releaseOutputBuffer(id, false);
            } else break;
        }

        dec.stop(); dec.release();
        enc.stop(); enc.release();
        ex.release();
        tmp.delete();

        if (aacOutFmt == null) throw new Exception("AAC encoder không trả về output format - kiểm tra định dạng file nhạc");
        if (aacSamples.isEmpty()) throw new Exception("Không có sample AAC nào được encode");
        return new AacResult(aacOutFmt, aacSamples, aacPts);
    }

    private static MediaFormat drainEncoder(MediaCodec enc, MediaCodec.BufferInfo info,
                                             List<byte[]> samples, List<Long> pts, MediaFormat fmt) {
        while (true) {
            int id = enc.dequeueOutputBuffer(info, 0);
            if (id == MediaCodec.INFO_TRY_AGAIN_LATER) break;
            if (id == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) { fmt = enc.getOutputFormat(); continue; }
            if (id >= 0) {
                ByteBuffer ob = enc.getOutputBuffer(id);
                if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0 && info.size > 0 && ob != null)
                    collectSample(ob, info, samples, pts);
                enc.releaseOutputBuffer(id, false);
            }
        }
        return fmt;
    }

    private static void collectSample(ByteBuffer buf, MediaCodec.BufferInfo info,
                                       List<byte[]> samples, List<Long> pts) {
        byte[] copy = new byte[info.size];
        buf.position(info.offset); buf.limit(info.offset + info.size);
        buf.get(copy);
        samples.add(copy);
        pts.add(info.presentationTimeUs);
    }

    private static File copyRaw(Context ctx, int resId) throws Exception {
        File f = new File(ctx.getCacheDir(), "raw_audio_" + System.currentTimeMillis());
        try (InputStream in = ctx.getResources().openRawResource(resId);
             FileOutputStream out = new FileOutputStream(f)) {
            byte[] buf = new byte[8192]; int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        }
        return f;
    }
}
