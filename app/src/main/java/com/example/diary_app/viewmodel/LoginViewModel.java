package com.example.diary_app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.diary_app.data.model.User;
import com.example.diary_app.repository.AuthRepository;
import com.example.diary_app.repository.UserRepository;
public class LoginViewModel extends ViewModel {
    private AuthRepository authRepository;
    private UserRepository userRepository;

    private MutableLiveData<String> loginSuccess = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    private MutableLiveData<String> forgotPasswordSuccess = new MutableLiveData<>();
    private MutableLiveData<String> forgotPasswordError = new MutableLiveData<>();
    private MutableLiveData<Boolean> isForgotPasswordLoading = new MutableLiveData<>();

    public LoginViewModel() {
        authRepository = new AuthRepository();
        userRepository = new UserRepository();
    }

    public LiveData<String> getLoginSuccess() {
        return loginSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public LiveData<String> getForgotPasswordSuccess() {
        return forgotPasswordSuccess;
    }

    public LiveData<String> getForgotPasswordError() {
        return forgotPasswordError;
    }

    public LiveData<Boolean> getIsForgotPasswordLoading() {
        return isForgotPasswordLoading;
    }

    public void checkExistingSession() {
        String uid = authRepository.getCurrentUserId();
        if (uid != null) {
            isLoading.setValue(true);
            userRepository.getUserProfile(uid)
                    .addOnSuccessListener(documentSnapshot -> {
                        isLoading.setValue(false);
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null && "admin".equals(user.getRole())) {
                                loginSuccess.setValue("admin");
                            } else {
                                loginSuccess.setValue("user");
                            }
                        } else {
                            loginSuccess.setValue("user");
                        }
                    })
                    .addOnFailureListener(e -> {
                        isLoading.setValue(false);
                        // Optional: clear session or handle error
                    });
        }
    }

    public void forgotPassword(String email) {
        isForgotPasswordLoading.setValue(true);

        // Kiểm tra xem email có tồn tại không
        userRepository.checkEmailExists(email)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        authRepository.resetPassword(email)
                                .addOnSuccessListener(aVoid -> {
                                    isForgotPasswordLoading.setValue(false);
                                    forgotPasswordSuccess.setValue("Email khôi phục mật khẩu đã được gửi!");
                                })
                                .addOnFailureListener(e -> {
                                    isForgotPasswordLoading.setValue(false);
                                    forgotPasswordError.setValue("Lỗi: Không thể gửi email. Vui lòng thử lại!");
                                });
                    } else {
                        isForgotPasswordLoading.setValue(false);
                        forgotPasswordError.setValue("Email không tồn tại trong hệ thống!");
                    }
                })
                .addOnFailureListener(e -> {
                    isForgotPasswordLoading.setValue(false);
                    forgotPasswordError.setValue("Lỗi kiểm tra hệ thống: " + e.getMessage());
                });
    }

    public void clearForgotPasswordStatus() {
        forgotPasswordSuccess.setValue(null);
        forgotPasswordError.setValue(null);
    }

    public void login(String email, String password) {
        isLoading.setValue(true);

        authRepository.login(email, password)
                .addOnSuccessListener(authResult -> {

                    if (authRepository.isEmailVerified()) {

                        // Đã xác thực email
                        String uid = authResult.getUser().getUid();

                        userRepository.getUserProfile(uid)
                                .addOnSuccessListener(documentSnapshot -> {
                                    isLoading.setValue(false);

                                    if(documentSnapshot.exists()){
                                        User user = documentSnapshot.toObject(User.class);
                                        // Kiểm tra xem có phải Admin không
                                        if (user != null && "admin".equals(user.getRole())) {
                                            loginSuccess.setValue("admin");
                                        } else {
                                            loginSuccess.setValue("user");
                                        }
                                    } else{
                                        loginSuccess.setValue("user");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    isLoading.setValue(false);
                                    errorMessage.setValue("Lỗi tải thông tin người dùng: " + e.getMessage());
                                });

                    } else {
                        // CHƯA XÁC THỰC EMAIL
                        isLoading.setValue(false);
                        authRepository.logout(); // logout ngay để xóa phiên đăng nhập ngầm
                        errorMessage.setValue("Vui lòng xác thực email trước khi đăng nhập!");
                    }
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("Sai email hoặc mật khẩu!");
                });
    }
}
