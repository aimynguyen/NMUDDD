package com.example.diary_app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.diary_app.data.model.User;
import com.example.diary_app.repository.AuthRepository;
import com.example.diary_app.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

public class EditProfileViewModel extends ViewModel {

    private AuthRepository authRepository;
    private UserRepository userRepository;

    private MutableLiveData<User> userLiveData = new MutableLiveData<>();

    private MutableLiveData<String> message = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public EditProfileViewModel() {

        authRepository = new AuthRepository();
        userRepository = new UserRepository();

    }

    public LiveData<User> getUser() { return userLiveData; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    // load profile
    public void loadProfile() {

        String uid = authRepository.getCurrentUserId();
        if (uid == null) return;

        isLoading.setValue(true);
        userRepository.getUserProfile(uid)
                .addOnSuccessListener(documentSnapshot -> {
                    isLoading.setValue(false);
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        userLiveData.setValue(user);
                    }
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    message.setValue("Lỗi tải thông tin: " + e.getMessage());
                });
    }

    // update profile
    public void updateProfile(String oldName, String oldAvatar, String newName, String newAvatar) {

        String uid = authRepository.getCurrentUserId();
        if (uid == null) return;

        Map<String, Object> updates = new HashMap<>();

        // So sánh để xem có thay đổi mới cập nhật
        if (!newName.equals(oldName)) {
            updates.put("userName", newName);
        }

        if (!newAvatar.equals(oldAvatar)) {
            updates.put("avatarUrl", newAvatar);
        }

        // NOTHING CHANGED
        if (updates.isEmpty()) {
            message.setValue("Nothing changed");
            return;
        }

        isLoading.setValue(true);
        userRepository.updateUserProfile(uid, updates)
                .addOnSuccessListener(unused -> {
                    isLoading.setValue(false);
                    message.setValue("Cập nhật thành công!");
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    message.setValue("Lỗi cập nhật: " + e.getMessage());
                });

    }

}