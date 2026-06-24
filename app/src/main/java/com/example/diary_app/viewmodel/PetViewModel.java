package com.example.diary_app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.diary_app.core.PetConstants;
import com.example.diary_app.data.model.PetInfo;
import com.example.diary_app.repository.PetRepository;
import com.example.diary_app.repository.PostRepository;
import com.example.diary_app.data.model.Post;

import java.util.Calendar;
import java.util.Date;

public class PetViewModel extends ViewModel {
    private PetRepository petRepository;
    private PostRepository postRepository;

    private MutableLiveData<PetInfo> petInfoLiveData = new MutableLiveData<>();
    private MutableLiveData<String> petEmotionLiveData = new MutableLiveData<>();
    private MutableLiveData<String> petQuoteLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    // gửi thông báo ra màn hình
    private MutableLiveData<String> toastMessageLiveData = new MutableLiveData<>();

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
    public LiveData<String> getToastMessage() {return toastMessageLiveData; }

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
                toastMessageLiveData.setValue(error);
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
                toastMessageLiveData.setValue("Đã đổi hình nền thành công!");
                isLoadingLiveData.setValue(false);
            }

            @Override
            public void onError(String error) {
                toastMessageLiveData.setValue(error);
                isLoadingLiveData.setValue(false);
            }
        });
    }

    // cập nhật cảm xúc dựa trên hoạt động trong ngày
    public void updatePetEmotion(String newEmotion){
        petEmotionLiveData.setValue(newEmotion);
        petQuoteLiveData.setValue(PetConstants.getRandomQuote(newEmotion));
    }

    public void checkTodayEmotion(String userId) {
        Calendar calendar = java.util.Calendar.getInstance();
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
                    toastMessageLiveData.setValue("Lỗi tải thông tin pet: " + e.getMessage());
                });
    }

    // tăng EXP (gọi ở viewmodel của Post)
    public void addExp(String userId){
        PetInfo currentPet = petInfoLiveData.getValue();
        if(currentPet == null)
            return;

        isLoadingLiveData.setValue(true);
        petRepository.addExpForPet(userId, currentPet, new PetRepository.OnExpUpdateListener(){
            @Override
            public void onSuccess(int newTotalExp) {
                toastMessageLiveData.setValue("Nhận EXP thành công!");
                // Gọi lại hàm lấy dữ liệu để UI cập nhật thanh Progress Bar và Level mới nhất
                loadPetData(userId);
            }

            @Override
            public void onDailyCapReached(String message) {
                toastMessageLiveData.setValue(message);
                isLoadingLiveData.setValue(false);
            }

            @Override
            public void onError(String error) {
                toastMessageLiveData.setValue(error);
                isLoadingLiveData.setValue(false);
            }
        });
    }
}
