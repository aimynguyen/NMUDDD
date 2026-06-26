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

    private long getDaysDifference(String date1Str, String date2Str) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date date1 = sdf.parse(date1Str);
            Date date2 = sdf.parse(date2Str);
            long diffInMillis = date2.getTime() - date1.getTime();
            return diffInMillis / (1000 * 60 * 60 * 24);
        } catch (Exception e) {
            return 0;
        }
    }

    public void addExpForPet(String userId, PetInfo petInfo, int expAmount, OnExpUpdateListener listener){
        String today = getTodayDateString();

        int newDailyExp = petInfo.getDailyExp();
        int newCurrentExp = petInfo.getCurrentExp();
        int newStreak = petInfo.getStreakDays();

        // 1. Kiểm tra qua ngày mới chưa
        if (!today.equals(petInfo.getLastExpDate())) {
            newDailyExp = 0; // Reset EXP ngày
            
            long daysDiff = getDaysDifference(petInfo.getLastExpDate(), today);
            if (daysDiff == 1) {
                newStreak += 1; // Hoạt động ở ngày tiếp theo
            } else if (daysDiff > 1) {
                newStreak = 1; // Bỏ lỡ ngày, reset streak
            }
            
            petInfo.setLastExpDate(today);
        }

        // Cập nhật streak = 1 nếu là lần đầu hoạt động (account mới tạo có streak = 0)
        if (newStreak == 0) {
            newStreak = 1;
        }

        // 2. Kiểm tra giới hạn EXP
        if (newDailyExp >= PetConstants.MAX_DAILY_EXP) {
            listener.onDailyCapReached("Bạn đã đạt giới hạn EXP hôm nay!");
            return; // Dừng lại, không cộng nữa
        }

        // 3. Tính toán EXP mới
        int actualExpAdded = expAmount;
        if (newDailyExp + expAmount > PetConstants.MAX_DAILY_EXP) {
            actualExpAdded = PetConstants.MAX_DAILY_EXP - newDailyExp;
        }

        newDailyExp += actualExpAdded;
        newCurrentExp += actualExpAdded;

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
        boolean isLevelUp = newLevel > petInfo.getLevel();
        if (isLevelUp) {
            // Cứ lên cấp tặng 1 item có ID tương ứng
            String rewardItemId = String.format(Locale.getDefault(), "bg_%02d", newLevel);

            // Dùng FieldValue.arrayUnion để thêm phần thưởng vào mảng unlockedItems trên Firebase
            updates.put("petInfo.unlockedItems", com.google.firebase.firestore.FieldValue.arrayUnion(rewardItemId));
        }

        // tạo biến final để truyền vào lambda
        final int finalCurrentExp = newCurrentExp;

        final int finalActualExpAdded = actualExpAdded;

        // 4. Update lên Firestore
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.update(updates)
          .addOnSuccessListener(aVoid ->{
            listener.onSuccess(finalCurrentExp, isLevelUp, newLevel, finalActualExpAdded);
        }).addOnFailureListener(e -> {
            listener.onError("Lỗi cập nhật EXP: " + e.getMessage());
        });
    }

    // Interface Callbacks
    public interface OnExpUpdateListener {
        void onSuccess(int newTotalExp, boolean isLevelUp, int newLevel, int actualExpAdded);
        void onDailyCapReached(String message);
        void onError(String error);
    }

    // HÀM LẤY THÔNG TIN PET CỦA USER
    public void getPetInfo(String userId, OnPetInfoFetchedListener listener){
        android.util.Log.d("PetRepository", "getPetInfo called for userId: " + userId);
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    android.util.Log.d("PetRepository", "getPetInfo document exists: " + documentSnapshot.exists());
                    
                    // Lấy riêng trường "petInfo" và ép kiểu thẳng về class PetInfo
                    PetInfo petInfo = documentSnapshot.get("petInfo", PetInfo.class);
                    android.util.Log.d("PetRepository", "getPetInfo deserialized petInfo: " + (petInfo != null ? "OK level=" + petInfo.getLevel() : "NULL"));

                    if(petInfo != null){
                        // Kiểm tra nếu đứt streak (cách >= 2 ngày)
                        String today = getTodayDateString();
                        long daysDiff = getDaysDifference(petInfo.getLastExpDate(), today);
                        if (daysDiff >= 2 && petInfo.getStreakDays() > 0) {
                            petInfo.setStreakDays(0);
                            // Cập nhật DB ngầm
                            db.collection("users").document(userId).update("petInfo.streakDays", 0);
                        }
                        
                        listener.onSuccess(petInfo);
                    } else {
                        // Tạo PetInfo mặc định nếu chưa có
                        android.util.Log.w("PetRepository", "getPetInfo is null, creating default PetInfo");
                        String today = getTodayDateString();
                        PetInfo defaultPetInfo = new PetInfo(today);
                        listener.onSuccess(defaultPetInfo);
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("PetRepository", "getPetInfo failure: " + e.getMessage());
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
