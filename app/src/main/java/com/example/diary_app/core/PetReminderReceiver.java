package com.example.diary_app.core;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.diary_app.MainActivity;
import com.example.diary_app.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;
import android.app.AlarmManager;

public class PetReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "PET_STREAK_CHANNEL";

    @Override
    public void onReceive(Context context, Intent intent) {
        // lên lịch cho ngày mai ngay khi Receiver thức dậy
        Calendar nextCalendar = Calendar.getInstance();
        nextCalendar.setTimeInMillis(System.currentTimeMillis());
        nextCalendar.set(Calendar.HOUR_OF_DAY, 20); // 20 giờ tối ngày mai
        nextCalendar.set(Calendar.MINUTE, 0);
        nextCalendar.set(Calendar.SECOND, 0);
        nextCalendar.add(Calendar.DAY_OF_MONTH, 1);
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent nextIntent = new Intent(context, PetReminderReceiver.class);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(
                context, 102, nextIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextCalendar.getTimeInMillis(), nextPendingIntent);
                } catch (SecurityException e) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextCalendar.getTimeInMillis(), nextPendingIntent);
                }
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextCalendar.getTimeInMillis(), nextPendingIntent);
            }
        }

        // Kiểm tra xem hôm nay đã nhận EXP chưa
        SharedPreferences sharedPref = context.getSharedPreferences("PetPrefs", Context.MODE_PRIVATE);
        String lastExpDate = sharedPref.getString("LAST_PET_EXP_DATE", "");
        String todayStr = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        
        if (todayStr.equals(lastExpDate)) {
            // Hôm nay đã nhận EXP rồi, dừng lại không gửi thông báo nữa
            return;
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Tạo Channel cho Android 8.0 trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Nhắc nhở nuôi Pet", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Thông báo nhắc nhở giữ streak nuôi thú cưng");
            notificationManager.createNotificationChannel(channel);
        }

        // Định nghĩa hành động khi User bấm vào thông báo (Mở app)
        Intent openAppIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE);

        // Xây dựng nội dung thông báo
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_pet)
                .setContentTitle("AuraLog: Người bạn nhỏ đang đợi! 🐾")
                .setContentText("Đừng quên ghi chép nhật ký hôm nay để giữ streak với Mochi nhé!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true); // Bấm vào là tự mất thông báo

        // Hiển thị lên màn hình
        notificationManager.notify(2026, builder.build());
    }
}