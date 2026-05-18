package com.example.diary_app.Helpers;

public class ValidationHelper {
    // Kiểm tra tiêu đề có trống không
    public static boolean isTitleValid(String title) {
        return title != null && !title.trim().isEmpty();
    }

    // Tự động viết hoa chữ cái đầu cho tiêu đề (Để nhật ký trông đẹp hơn)
    public static String formatTitle(String title) {
        if (title == null || title.isEmpty()) return "";
        return title.substring(0, 1).toUpperCase() + title.substring(1).toLowerCase();
    }

    // Giới hạn độ dài tiêu đề hiển thị ở màn hình danh sách (Tránh bị tràn giao diện)
    public static String shortenTitle(String title, int limit) {
        if (title.length() <= limit) return title;
        return title.substring(0, limit) + "...";
    }
}
