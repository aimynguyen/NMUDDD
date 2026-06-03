package com.example.diary_app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.diary_app.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileViewModel extends ViewModel {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private MutableLiveData<UserModel> userLiveData =
            new MutableLiveData<>();

    private MutableLiveData<String> message =
            new MutableLiveData<>();

    public EditProfileViewModel() {

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

    }

    public LiveData<UserModel> getUser() {
        return userLiveData;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    // load profile
    public void loadProfile() {

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    UserModel user =
                            documentSnapshot.toObject(UserModel.class);

                    userLiveData.setValue(user);

                });

    }

    // update profile
    public void updateProfile(
            String oldName,
            String oldAvatar,
            String newName,
            String newAvatar
    ) {

        String uid =
                auth.getCurrentUser().getUid();

        Map<String, Object> updates =
                new HashMap<>();

        // check name
        if (!newName.equals(oldName)) {

            updates.put("userName", newName);

        }

        // check avatar
        if (!newAvatar.equals(oldAvatar)) {

            updates.put("avatarUrl", newAvatar);

        }

        // NOTHING CHANGED
        if (updates.isEmpty()) {

            message.setValue("Nothing changed");
            return;

        }

        db.collection("users")
                .document(uid)
                .update(updates)
                .addOnSuccessListener(unused -> {

                    message.setValue("Profile updated");

                })
                .addOnFailureListener(e -> {

                    message.setValue(
                            e.getMessage()
                    );

                });

    }

}