package com.example.diary_app.utils;

//import android.content.Context;
//import com.arthenica.ffmpegkit.FFmpegKit;
//import com.arthenica.ffmpegkit.ReturnCode;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Locale;
//
public class VideoUtils {
//
//    // 1. Hàm copy file nhạc từ thư mục raw ra thư mục Cache để FFmpeg đọc được
//    public static String copyRawAudioToCache(Context context, int rawResourceId, String fileName) {
//        File audioFile = new File(context.getCacheDir(), fileName);
//        if (audioFile.exists()) {
//            return audioFile.getAbsolutePath();
//        }
//        try (InputStream in = context.getResources().openRawResource(rawResourceId);
//             FileOutputStream out = new FileOutputStream(audioFile)) {
//            byte[] buffer = new byte[1024];
//            int read;
//            while ((read = in.read(buffer)) != -1) {
//                out.write(buffer, 0, read);
//            }
//            return audioFile.getAbsolutePath();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    // 2. Hàm chạy FFmpeg xuất video (Đã tối ưu CPU/RAM bằng preset veryfast)
//    public static void createVideoSlideshow(File imageFolder, String audioPath, String outputPath, float frameRate, VideoCallback callback) {
//        // Định dạng tên ảnh đầu vào (ví dụ: img001.jpg, img002.jpg...)
//        String imagePattern = new File(imageFolder, "img%03d.jpg").getAbsolutePath();
//
//        // Câu lệnh FFmpeg tối ưu tốc độ
//        // Thêm Locale.US để tránh lỗi định dạng số ở các ngôn ngữ khác nhau
//        String ffmpegCommand = String.format(Locale.US,
//                "-r %f -i %s -i %s -c:v libx264 -preset veryfast -threads 4 -pix_fmt yuv420p -c:a aac -shortest %s",
//                frameRate, imagePattern, audioPath, outputPath
//        );
//
//        FFmpegKit.executeAsync(ffmpegCommand, session -> {
//            ReturnCode returnCode = session.getReturnCode();
//            if (ReturnCode.isSuccess(returnCode)) {
//                callback.onSuccess(outputPath);
//            } else {
//                // Sử dụng getOutput() để lấy log lỗi (phương thức đúng của FFmpegSession)
//                callback.onFailure(session.getOutput());
//            }
//        });
//    }
//
//    // Interface để nhận kết quả trả về từ FFmpeg
//    public interface VideoCallback {
//        void onSuccess(String videoPath);
//        void onFailure(String errorLog);
//    }
}
