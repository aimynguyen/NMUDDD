package com.example.diary_app.utils;

public class ChatUtils {

    // Hàm tạo ID phòng chat duy nhất cho 2 người dùng
    // return Chuỗi ID phòng chat (Ví dụ: "uidA_uidB")
    public static String generateChatId(String uid1, String uid2) {
        // Tránh lỗi nếu vô tình truyền null
        if (uid1 == null || uid2 == null) {
            return null;
        }

        // So sánh 2 chuỗi (trả về số âm nếu uid1 đứng trước uid2 trong bảng chữ cái)
        if (uid1.compareTo(uid2) < 0) {
            return uid1 + "_" + uid2;
        } else {
            return uid2 + "_" + uid1;
        }
    }
}
