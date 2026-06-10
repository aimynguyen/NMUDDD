package com.example.diary_app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.diary_app.data.model.User;
import com.example.diary_app.repository.AuthRepository;
import com.example.diary_app.repository.UserRepository;
public class SignupViewModel extends ViewModel {

    private AuthRepository authRepository;
    private UserRepository userRepository;

    private MutableLiveData<Boolean> signupSuccess = new MutableLiveData<>();

    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public SignupViewModel() {
        authRepository = new AuthRepository();
        userRepository = new UserRepository();
    }

    public LiveData<Boolean> getSignupSuccess() {
        return signupSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void signup(String email, String password, String username, String dob) {
        isLoading.setValue(true);

        authRepository.register(email, password)
                        .addOnSuccessListener(authResult -> {

                            authRepository.sendEmailVerification();

                            String uid = authResult.getUser().getUid();

                            User newUser = new User();
                            newUser.setUid(uid);
                            newUser.setEmail(email);
                            newUser.setUserName(username);

                            newUser.setBirthday(dob);
                            newUser.setRole("user");

                            userRepository.createUserProfile(newUser)
                                    .addOnSuccessListener(aVoid -> {
                                        isLoading.setValue(false);
                                        signupSuccess.setValue(true);
                                    })
                                    .addOnFailureListener(e -> {
                                        isLoading.setValue(false);
                                        errorMessage.setValue("Lỗi lưu dữ liệu: " + e.getMessage());
                                    });
                        })
                        .addOnFailureListener(e -> {
                            isLoading.setValue(false);
                            errorMessage.setValue("Lỗi đăng ký: " + e.getMessage());
                        });

    }
}