package com.example.diary_app.repository;

import com.example.diary_app.core.PetConstants;
import com.example.diary_app.data.model.PetInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PetRepository {
    private FirebaseFirestore db;

    public PetRepository(){
        db = FirebaseFirestore.getInstance();
    }

    private String getTodayDateString(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

    public void addExpForPet(String userId, PetInfo petInfo, OnExpUpdateListener listener){
        String today = getTodayDateString();

        int newDailyExp = petInfo.getDailyExp();
        int newCurrentExp = petInfo.getCurrentExp();
        int newStreak = petInfo.getStreakDays();

        // 1. Kiểm tra qua ngày mới chưa
        if (!today.equals(petInfo.getLastExpDate())) {
            newDailyExp = 0; // Reset EXP ngày
            petInfo.setLastExpDate(today);
            newStreak += 1; // Tăng streak
        }

        // 2. Kiểm tra giới hạn EXP
        if (newDailyExp >= PetConstants.MAX_DAILY_EXP) {
            listener.onDailyCapReached("Bạn đã đạt giới hạn EXP hôm nay!");
            return; // Dừng lại, không cộng nữa
        }

        // 3. Tính toán EXP mới
        newDailyExp += PetConstants.EXP_PER_POST;
        newCurrentExp += PetConstants.EXP_PER_POST;

        // kiểm tra lên Level
        int newLevel = PetConstants.calculateLevel(newCurrentExp);

        // TẠO MỘT DANH SÁCH CÁC THAY ĐỔI ĐỂ UPDATE
        Map<String, Object> updates = new HashMap<>();
        updates.put("petInfo.currentExp", newCurrentExp);
        updates.put("petInfo.dailyExp", newDailyExp);
        updates.put("petInfo.streakDays", newStreak);
        updates.put("petInfo.level", newLevel);
        updates.put("petInfo.lastExpDate", today);

        // Nếu cấp độ mới lớn hơn cấp độ cũ trong petInfo
        if (newLevel > petInfo.getLevel()) {
            // Giả sử cứ lên cấp tặng 1 item có ID tương ứng
            // (Ví dụ: Level 2 tặng "bg_level_2")
            String rewardItemId = "bg_level_" + newLevel;

            // Dùng FieldValue.arrayUnion để thêm phần thưởng vào mảng unlockedItems trên Firebase
            updates.put("petInfo.unlockedItems", com.google.firebase.firestore.FieldValue.arrayUnion(rewardItemId));
        }

        // tạo biến final để truyền vào lambda
        final int finalCurrentExp = newCurrentExp;

        // 4. Update lên Firestore
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.update(updates)
          .addOnSuccessListener(aVoid ->{
            listener.onSuccess(finalCurrentExp);
        }).addOnFailureListener(e -> {
            listener.onError("Lỗi cập nhật EXP: " + e.getMessage());
        });
    }

    // Interface Callbacks
    public interface OnExpUpdateListener {
        void onSuccess(int newTotalExp);
        void onDailyCapReached(String message);
        void onError(String error);
    }

    // HÀM LẤY THÔNG TIN PET CỦA USER
    public void getPetInfo(String userId, OnPetInfoFetchedListener listener){
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Lấy riêng trường "petInfo" và ép kiểu thẳng về class PetInfo
                    PetInfo petInfo = documentSnapshot.get("petInfo", PetInfo.class);

                    if(petInfo != null){
                        listener.onSuccess(petInfo);
                    } else {
                        listener.onError("Không tìm thấy dữ liệu về Pet!");
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onError("Lỗi kết nối: " + e.getMessage());
                });
    }

    public interface OnPetInfoFetchedListener {
        void onSuccess(PetInfo petInfo);
        void onError(String error);
    }

    // Hàm cập nhật hình nền đang trang bị của Pet lên Firestore
    public void equipBackground(String userId, String backgroundId, OnActionCompleteListener listener){
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.update("petInfo.equippedItems.background", backgroundId)
                .addOnSuccessListener(aVoid -> {
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    listener.onError("Lỗi cập nhật hình nền: " + e.getMessage());
                });
    }

    public interface OnActionCompleteListener{
        void onSuccess();
        void onError(String error);
    }
}
