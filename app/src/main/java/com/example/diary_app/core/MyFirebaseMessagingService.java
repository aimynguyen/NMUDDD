package com.example.diary_app.core;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.diary_app.MainActivity;
import com.example.diary_app.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Hàm này tự động chạy khi có thông báo bắn về lúc app ĐANG MỞ
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            
            Log.d("FCM", "Tiêu đề: " + title);
            Log.d("FCM", "Nội dung: " + body);
            
            // Hiển thị popup thông báo thả xuống
            sendNotification(title, body);
        }
    }

    /**
     * Tạo và hiển thị Heads-up Notification rớt xuống từ màn hình
     */
    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        String channelId = "auralog_default_channel";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_noti)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_HIGH) // Ép buộc hiển thị rớt xuống ở Foreground
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Bắt buộc tạo Channel cho Android 8.0 (API 26) trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "AuraLog Notifications",
                    NotificationManager.IMPORTANCE_HIGH); // Tầm quan trọng cao để cho phép heads-up
            notificationManager.createNotificationChannel(channel);
        }

        // Bắn thông báo ra ngoài, ID ngẫu nhiên để không đè lên nhau
        notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
    }

    @Override
    public void onNewToken(String token) {
        // Hàm này chạy khi Google cấp cho điện thoại một Token mới
        Log.d("FCM", "Token mới: " + token);
        // Lưu token này lên Firestore
    }
}