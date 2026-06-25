package com.example.diary_app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.diary_app.data.model.User;
import com.example.diary_app.repository.AuthRepository;
import com.example.diary_app.repository.PostRepository;
import com.example.diary_app.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

import java.util.HashMap;
import java.util.Map;

public class EditProfileViewModel extends ViewModel {

    private AuthRepository authRepository;
    private UserRepository userRepository;
    private PostRepository postRepository;

    private MutableLiveData<User> userLiveData = new MutableLiveData<>();

    private MutableLiveData<String> message = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    private MutableLiveData<String> changePasswordSuccess = new MutableLiveData<>();
    private MutableLiveData<String> changePasswordError = new MutableLiveData<>();

    public EditProfileViewModel() {

        authRepository = new AuthRepository();
        userRepository = new UserRepository();
        postRepository = new PostRepository();

    }

    public LiveData<User> getUser() { return userLiveData; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getChangePasswordSuccess() { return changePasswordSuccess; }
    public LiveData<String> getChangePasswordError() { return changePasswordError; }

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

    // upload avatar
    public void uploadAvatar(byte[] imageBytes, String oldAvatarUrl) {
        isLoading.setValue(true);
        userRepository.uploadImageToStorage(imageBytes)
                .addOnSuccessListener(taskSnapshot -> {
                    userRepository.getDownloadUrl(taskSnapshot.getStorage())
                            .addOnSuccessListener(uri -> {
                                String newAvatarUrl = uri.toString();
                                updateAvatarUrl(newAvatarUrl, oldAvatarUrl);
                            })
                            .addOnFailureListener(e -> {
                                isLoading.setValue(false);
                                message.setValue("Lỗi lấy link ảnh: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    message.setValue("Lỗi tải ảnh lên: " + e.getMessage());
                });
    }

    private void updateAvatarUrl(String newAvatarUrl, String oldAvatarUrl) {
        String uid = authRepository.getCurrentUserId();
        if (uid == null) return;
        userRepository.updateUserField(uid, "avatarUrl", newAvatarUrl)
                .addOnSuccessListener(unused -> {
                    postRepository.updateUserAvatarInPosts(
                            uid,
                            newAvatarUrl
                    );
                    // Xóa ảnh cũ
                    if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
                        userRepository.deleteImageFromStorage(oldAvatarUrl);
                    }
                    isLoading.setValue(false);
                    message.setValue("Cập nhật ảnh đại diện thành công!");
                    User currentUser = userLiveData.getValue();
                    if(currentUser != null) {
                        currentUser.setAvatarUrl(newAvatarUrl);
                        userLiveData.setValue(currentUser);
                    }
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    message.setValue("Lỗi lưu ảnh: " + e.getMessage());
                });
    }

    // update profile
    public void updateProfile(String oldName, String oldBirthday, String newName, String newBirthday) {

        String uid = authRepository.getCurrentUserId();
        if (uid == null) return;

        Map<String, Object> updates = new HashMap<>();

        // So sánh để xem có thay đổi mới cập nhật
        if (!newName.equals(oldName)) {
            updates.put("userName", newName);
        }

        if (newBirthday != null && !newBirthday.equals(oldBirthday)) {
            updates.put("birthday", newBirthday);
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

    public void changePassword(String currentPassword, String newPassword) {
        isLoading.setValue(true);
        authRepository.changePassword(currentPassword, newPassword)
                .addOnSuccessListener(aVoid -> {
                    isLoading.setValue(false);
                    changePasswordSuccess.setValue("Đổi mật khẩu thành công!");
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        changePasswordError.setValue("Mật khẩu hiện tại không đúng.");
                    } else if (e.getMessage() != null && e.getMessage().contains("Re-authentication failed")) {
                        changePasswordError.setValue("Mật khẩu hiện tại không đúng.");
                    } else {
                        changePasswordError.setValue("Lỗi đổi mật khẩu: " + e.getMessage());
                    }
                });
    }

}