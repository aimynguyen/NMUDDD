package com.example.diary_app.viewmodel;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.diary_app.DTOs.InventoryItem;
import com.example.diary_app.core.Event;
import com.example.diary_app.core.NotiType;
import com.example.diary_app.core.PetConstants;
import com.example.diary_app.data.model.PetInfo;
import com.example.diary_app.repository.NotificationRepository;
import com.example.diary_app.repository.PetRepository;
import com.example.diary_app.repository.PostRepository;
import com.example.diary_app.data.model.Post;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PetViewModel extends ViewModel {
    private PetRepository petRepository;
    private PostRepository postRepository;

    private MutableLiveData<PetInfo> petInfoLiveData = new MutableLiveData<>();
    private MutableLiveData<String> petEmotionLiveData = new MutableLiveData<>();
    private MutableLiveData<String> petQuoteLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    // gửi thông báo ra màn hình
    private MutableLiveData<Event<String>> toastMessageLiveData = new MutableLiveData<>();
    // Sự kiện lên cấp - dùng Event wrapper để đảm bảo chỉ xử lý MÓT LẦN, tránh Sticky LiveData
    private MutableLiveData<Event<Integer>> levelUpEvent = new MutableLiveData<>();

    public PetViewModel(){
        petRepository = new PetRepository();
        postRepository = new PostRepository();

        petEmotionLiveData.setValue(PetConstants.EMOTION_NEUTRAL);
        petQuoteLiveData.setValue(PetConstants.getRandomQuote(PetConstants.EMOTION_NEUTRAL));
    }

    public LiveData<PetInfo> getPetInfo() {return petInfoLiveData; }
    public LiveData<String> getPetEmotion() {return petEmotionLiveData; }
    public LiveData<String> getPetQuote() {return petQuoteLiveData; }
    public LiveData<Boolean> getIsLoading() {return isLoadingLiveData; }
    public LiveData<Event<String>> getToastMessage() {return toastMessageLiveData; }
    public LiveData<Event<Integer>> getLevelUpEvent() {return levelUpEvent; }
    public void resetLevelUpEvent() { levelUpEvent.setValue(null); }

    // lấy thông tin pet
    public void loadPetData(String userId){
        isLoadingLiveData.setValue(true);

        petRepository.getPetInfo(userId, new PetRepository.OnPetInfoFetchedListener() {
            @Override
            public void onSuccess(PetInfo petInfo) {
                // Đẩy dữ liệu vào LiveData -> Fragment sẽ tự động nhận được và vẽ UI
                petInfoLiveData.setValue(petInfo);
                isLoadingLiveData.setValue(false);
            }

            @Override
            public void onError(String error) {
                toastMessageLiveData.setValue(new Event<>(error));
                isLoadingLiveData.setValue(false);
            }
        });
    }

    // thay đổi hình nền
    public void changeBackground(String userId, String backgroundId){
        isLoadingLiveData.setValue(true);

        petRepository.equipBackground(userId, backgroundId, new PetRepository.OnActionCompleteListener() {
            @Override
            public void onSuccess() {
                PetInfo currentPet = petInfoLiveData.getValue();
                if (currentPet != null) {
                    currentPet.getEquippedItems().put(PetConstants.ITEM_TYPE_BACKGROUND, backgroundId);
                    petInfoLiveData.setValue(currentPet); // Kích hoạt UI vẽ lại ảnh nền
                }
                toastMessageLiveData.setValue(new Event<>("Đã đổi hình nền thành công!"));
                isLoadingLiveData.setValue(false);
            }

            @Override
            public void onError(String error) {
                toastMessageLiveData.setValue(new Event<>(error));
                isLoadingLiveData.setValue(false);
            }
        });
    }

    // cập nhật cảm xúc dựa trên hoạt động trong ngày
    public void updatePetEmotion(String newEmotion){
        petEmotionLiveData.setValue(newEmotion);
        petQuoteLiveData.setValue(PetConstants.getRandomQuote(newEmotion));
    }

    public void changeQuote() {
        String currentEmotion = petEmotionLiveData.getValue();
        if (currentEmotion == null) {
            currentEmotion = PetConstants.EMOTION_NEUTRAL;
        }
        petQuoteLiveData.setValue(PetConstants.getRandomQuote(currentEmotion));
    }

    public void checkTodayEmotion(String userId) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfDay = calendar.getTime();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date endOfDay = calendar.getTime();

        postRepository.getPostByTimeRange(userId, startOfDay, endOfDay)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Post latestPost = queryDocumentSnapshots.getDocuments().get(0).toObject(Post.class);
                        if (latestPost != null && latestPost.getEmotion() != null) {
                            updatePetEmotion(latestPost.getEmotion());
                        } else {
                            updatePetEmotion(PetConstants.EMOTION_SLEEP);
                        }
                    } else {
                        updatePetEmotion(PetConstants.EMOTION_SLEEP);
                    }
                })
                .addOnFailureListener(e -> {
                    updatePetEmotion(PetConstants.EMOTION_SLEEP);
                    toastMessageLiveData.setValue(new Event<>("Lỗi tải thông tin pet: " + e.getMessage()));
                });
    }

    // tăng EXP (gọi ở viewmodel của Post)
    public void addExp(String userId, int expAmount){
        PetInfo currentPet = petInfoLiveData.getValue();
        if (currentPet != null) {
            // Đã có data sẵn trong bộ nhớ
            doAddExp(userId, currentPet, expAmount);
        } else {
            // Chưa có data, fetch từ Firebase trước rồi mới cộng EXP
            petRepository.getPetInfo(userId, new PetRepository.OnPetInfoFetchedListener() {
                @Override
                public void onSuccess(PetInfo petInfo) {
                    petInfoLiveData.setValue(petInfo);
                    doAddExp(userId, petInfo, expAmount);
                }

                @Override
                public void onError(String error) {
                    // Log debug và hiển thị toast
                    Log.e("PetViewModel", "addExp - Lỗi fetch petInfo: " + error);
                    toastMessageLiveData.setValue(new Event<>("Lỗi tải Pet: " + error));
                }
            });
        }
    }

    private void doAddExp(String userId, PetInfo currentPet, int expAmount) {
        Log.d("PetViewModel", "doAddExp called - dailyExp=" + currentPet.getDailyExp() + ", currentExp=" + currentPet.getCurrentExp() + ", level=" + currentPet.getLevel());
        isLoadingLiveData.setValue(true);
        petRepository.addExpForPet(userId, currentPet, expAmount, new PetRepository.OnExpUpdateListener(){
            @Override
            public void onSuccess(int newTotalExp, boolean isLevelUp, int newLevel, int actualExpAdded) {
                Log.d("PetViewModel", "doAddExp onSuccess - newTotalExp=" + newTotalExp + ", isLevelUp=" + isLevelUp);
                NotificationRepository notiRepo = new NotificationRepository();
                notiRepo.sendNotification(userId, "system", NotiType.PET_FEED, "pet", "Mochi + " + actualExpAdded + " EXP");

                if (isLevelUp) {
                    notiRepo.sendNotification(userId, "system", NotiType.PET_LEVEL_UP, "pet", "Mochi đã đạt Level " + newLevel);
                    // Event wrapper - đảm bảo màn hình Level Up chỉ hiện đúng 1 lần
                    levelUpEvent.setValue(new Event<>(newLevel));
                } else {
                    toastMessageLiveData.setValue(new Event<>("Đã nhận EXP thành công!"));
                }
                
                // Gọi lại hàm lấy dữ liệu để UI cập nhật thanh Progress Bar và Level mới nhất
                loadPetData(userId);
            }

            @Override
            public void onDailyCapReached(String message) {
                android.util.Log.d("PetViewModel", "doAddExp - dailyCap reached: " + message);
                toastMessageLiveData.setValue(new Event<>(message));
                isLoadingLiveData.setValue(false);
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("PetViewModel", "doAddExp onError: " + error);
                toastMessageLiveData.setValue(new Event<>(error));
                isLoadingLiveData.setValue(false);
            }
        });
    }

    private MutableLiveData<List<InventoryItem>> inventoryItemsLiveData = new MutableLiveData<>();
    public LiveData<List<InventoryItem>> getInventoryItems() { return inventoryItemsLiveData; }

    public void generateInventoryList(Context context, List<String> unlockedItems) {
        java.util.List<InventoryItem> items = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            String id = String.format(Locale.getDefault(), "bg_%02d", i);
            String name = "Level " + i;
            
            int resId = context.getResources().getIdentifier(id, "drawable", context.getPackageName());
            
            boolean isUnlocked = unlockedItems.contains(id);
            items.add(new InventoryItem(id, resId, name, isUnlocked));
        }
        inventoryItemsLiveData.setValue(items);
    }
}
