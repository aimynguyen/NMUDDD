package com.example.diary_app.Helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
public class imageHelper {
    /**
     * Hàm chuyển từ Uri (đường dẫn ảnh) sang Bitmap để hiển thị
     */
    public static Bitmap uriToBitmap(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Hàm nén ảnh (Compress) để lưu vào Database hoặc gửi đi
     * Trả về mảng byte[] để dễ dàng lưu vào kiểu dữ liệu BLOB trong DB
     */
    public static byte[] compressBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // Nén ảnh theo định dạng JPEG với chất lượng (0-100)
        //quality ~60-80
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

    /**
     * Hàm thay đổi kích thước ảnh (Resize)
     * Giúp app không bị lag khi load danh sách nhiều ảnh nhỏ (thumbnails)
     */
    public static Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float ratioBitmap = (float) width / (float) height;
        float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;

        if (ratioMax > ratioBitmap) {
            finalWidth = (int) ((float)maxHeight * ratioBitmap);
        } else {
            finalHeight = (int) ((float)maxWidth / ratioBitmap);
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true);
    }
}
