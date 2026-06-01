package com.example.diary_app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileViewModel extends ViewModel {

    // username
    private MutableLiveData<String> userName =
            new MutableLiveData<>();

    // avatar url
    private MutableLiveData<String> avatarUrl =
            new MutableLiveData<>();

    private FirebaseAuth mAuth;

    private FirebaseFirestore db;

    public ProfileViewModel() {

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();

    }

    // getter username
    public LiveData<String> getUserName() {

        return userName;

    }

    // getter avatar
    public LiveData<String> getAvatarUrl() {

        return avatarUrl;

    }

    public void loadProfile() {

        FirebaseUser currentUser =
                mAuth.getCurrentUser();

        if (currentUser != null) {

            String uid = currentUser.getUid();

            db.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {

                        if (documentSnapshot.exists()) {

                            // username
                            String username =
                                    documentSnapshot.getString("userName");

                            userName.setValue(username);

                            // avatar url
                            String avatar =
                                    documentSnapshot.getString("avatarUrl");

                            avatarUrl.setValue(avatar);

                        }

                    });

        }

    }
}