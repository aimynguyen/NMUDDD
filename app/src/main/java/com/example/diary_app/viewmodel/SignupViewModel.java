package com.example.diary_app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;

public class SignupViewModel extends ViewModel {

    private FirebaseAuth mAuth;

    private MutableLiveData<Boolean> signupSuccess =
            new MutableLiveData<>();

    private MutableLiveData<String> errorMessage =
            new MutableLiveData<>();

    public SignupViewModel() {
        mAuth = FirebaseAuth.getInstance();
    }

    public LiveData<Boolean> getSignupSuccess() {
        return signupSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void signup(String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        signupSuccess.setValue(true);
                    } else {
                        errorMessage.setValue(
                                task.getException().getMessage()
                        );
                    }

                });
    }
}