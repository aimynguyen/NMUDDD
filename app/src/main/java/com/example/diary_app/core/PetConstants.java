package com.example.diary_app.core;

public class PetConstants {
    private PetConstants(){}

    // 1. CẤU HÌNH KINH NGHIỆM (EXP) & HOẠT ĐỘNG
    public static final int MAX_DAILY_EXP = 100; // Giới hạn EXP tối đa nhận trong 1 ngày
    public static final int EXP_PER_POST = 20;   // Nhận 20 EXP khi đăng bài
    public static final int EXP_PER_FEELING = 5; // Nhận 5 EXP khi chỉ cập nhật cảm xúc (nếu có)


    // 2. CẤU HÌNH CẤP ĐỘ (LEVEL)
    public static final int MAX_LEVEL = 10;

    /**
     * Mảng lưu mốc EXP tổng cộng cần thiết để ĐẠT ĐƯỢC level tương ứng.
     * Cách đọc: Index của mảng chính là Level.
     * Index 0: Bỏ qua (không có Level 0).
     * Index 1: Level 1 cần 0 EXP.
     * Index 2: Level 2 cần đạt tổng 50 EXP.
     * Index 3: Level 3 cần đạt tổng 120 EXP...
     */
    public static final int[] EXP_THRESHOLDS = {
            0,      // Index 0: Không dùng
            0,      // Level 1
            50,     // Level 2
            120,    // Level 3
            220,    // Level 4
            350,    // Level 5
            500,    // Level 6
            700,    // Level 7
            950,    // Level 8
            1250,   // Level 9
            1600    // Level 10
    };

    // Hàm tiện ích: Kiểm tra xem với tổng EXP hiện tại thì đang ở Level mấy
    public static int calculateLevel(int totalExp) {
        int currentLevel = 1;
        for (int i = 1; i <= MAX_LEVEL; i++) {
            if (totalExp >= EXP_THRESHOLDS[i]) {
                currentLevel = i;
            } else {
                break; // Nếu tổng EXP chưa đạt mốc tiếp theo thì dừng
            }
        }
        return currentLevel;
    }

    // 3. CẤU HÌNH TRANG BỊ / CẢM XÚC MẶC ĐỊNH
    public static final String ITEM_TYPE_BACKGROUND = "background";
    public static final String DEFAULT_BACKGROUND_ID = "bg_default";

    // Các trạng thái cảm xúc của Pet (khớp với tên file ảnh đuôi png/webp)
    public static final String EMOTION_HAPPY = "happy";
    public static final String EMOTION_SAD = "sad";
    public static final String EMOTION_NEUTRAL = "neutral";

    // Kho dữ liệu câu thoại mẫu
    private static final String[] QUOTES_HAPPY = {
            "Hôm nay là một ngày tuyệt vời đúng không!",
            "Năng lượng tích cực tràn đầy luôn nè!",
            "Cứ vui vẻ như thế này mỗi ngày bạn nhé!"
    };

    private static final String[] QUOTES_SAD = {
            "Đừng buồn nữa, có tớ ở đây ôm bạn nè...",
            "Ngày mai trời lại sáng thôi, cố lên bạn ơi.",
            "Nếu mệt mỏi quá, hãy nghỉ ngơi một chút nhé."
    };

    private static final String[] QUOTES_NEUTRAL = {
            "Hôm nay của bạn thế nào? Kể tớ nghe đi.",
            "Tớ vẫn đang lắng nghe bạn đây.",
            "Viết một chút nhật ký cho nhẹ lòng nào."
    };

    /**
     * Hàm lấy ngẫu nhiên 1 câu thoại dựa trên cảm xúc đầu vào
     */
    public static String getRandomQuote(String emotion) {
        java.util.Random random = new java.util.Random();
        switch (emotion) {
            case EMOTION_HAPPY:
                return QUOTES_HAPPY[random.nextInt(QUOTES_HAPPY.length)];
            case EMOTION_SAD:
                return QUOTES_SAD[random.nextInt(QUOTES_SAD.length)];
            default:
                return QUOTES_NEUTRAL[random.nextInt(QUOTES_NEUTRAL.length)];
        }
    }
}
