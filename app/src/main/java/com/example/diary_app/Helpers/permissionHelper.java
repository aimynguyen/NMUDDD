package com.example.diary_app.Helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
public class permissionHelper {
    public static final int REQUEST_CODE_CAMERA = 101;
    public static final int REQUEST_CODE_GALLERY = 102;

    // Kiểm tra quyền Camera
    public static boolean isCameraPermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    // Kiểm tra quyền Thư viện ảnh (Xử lý cho cả bản Android cũ và mới)
    public static boolean isGalleryPermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 (API 33) trở lên dùng quyền này
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android cũ hơn dùng quyền này
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    // Hàm yêu cầu cấp quyền Camera
    public static void requestCameraPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
    }

    // Hàm yêu cầu cấp quyền Thư viện ảnh
    public static void requestGalleryPermission(Activity activity) {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        ActivityCompat.requestPermissions(activity, new String[]{permission}, REQUEST_CODE_GALLERY);
    }
}
