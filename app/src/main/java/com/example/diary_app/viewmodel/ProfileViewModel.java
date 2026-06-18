package com.example.diary_app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.diary_app.data.model.User;
import com.example.diary_app.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class ProfileViewModel extends ViewModel {

    private FirebaseAuth auth;
    private UserRepository userRepository;

    private MutableLiveData<String> userName = new MutableLiveData<>();
    private MutableLiveData<String> avatarUrl = new MutableLiveData<>();
    private MutableLiveData<List<User>> friendsLiveData = new MutableLiveData<>();
    private MutableLiveData<List<User>> requestsLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public ProfileViewModel() {
        auth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();
    }

    public LiveData<String> getUserName() { return userName; }
    public LiveData<String> getAvatarUrl() { return avatarUrl; }
    public LiveData<List<User>> getFriendsLiveData() { return friendsLiveData; }
    public LiveData<List<User>> getRequestsLiveData() { return requestsLiveData; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void loadProfile() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        isLoading.setValue(true);
        String uid = currentUser.getUid();

        // Load User Profile & Friends
        userRepository.getUserProfile(uid).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                userName.setValue(documentSnapshot.getString("userName"));
                avatarUrl.setValue(documentSnapshot.getString("avatarUrl"));

                List<String> friendIds = (List<String>) documentSnapshot.get("friendIds");
                if (friendIds != null && !friendIds.isEmpty()) {
                    userRepository.getUsersByIds(friendIds).addOnSuccessListener(friends -> {
                        friendsLiveData.setValue(friends);
                        checkLoadingState();
                    });
                } else {
                    friendsLiveData.setValue(new ArrayList<>());
                    checkLoadingState();
                }
            }
        });

        // Load Friend Requests
        userRepository.getPendingFriendRequestsAsUsers(uid).addOnSuccessListener(requests -> {
            requestsLiveData.setValue(requests);
            checkLoadingState();
        });
    }

    private void checkLoadingState() {
        if (friendsLiveData.getValue() != null && requestsLiveData.getValue() != null) {
            isLoading.setValue(false);
        }
    }

    public void acceptRequestBySender(String senderId) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        userRepository.acceptFriendRequestBySender(currentUser.getUid(), senderId)
                .addOnSuccessListener(aVoid -> loadProfile());
    }

    public void rejectRequestBySender(String senderId) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        userRepository.deleteFriendRequestBySender(currentUser.getUid(), senderId)
                .addOnSuccessListener(aVoid -> loadProfile());
    }
}
