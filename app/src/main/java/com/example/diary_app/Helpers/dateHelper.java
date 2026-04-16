package com.example.diary_app.Helpers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class dateHelper {
        // Hàm chuyển đổi định dạng ngày tháng để hiển thị lên UI
        public static String getFormattedDate(long timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }

        // Hàm lấy thời gian hiện tại
        public static long getCurrentTimestamp() {
            return System.currentTimeMillis();
        }
    }