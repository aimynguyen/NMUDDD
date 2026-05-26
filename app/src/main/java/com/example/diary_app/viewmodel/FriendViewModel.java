package com.example.diary_app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.diary_app.model.UserModel;
import com.example.diary_app.model.FriendRequestModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class FriendViewModel extends ViewModel {

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Friend requests
    private MutableLiveData<ArrayList<FriendRequestModel>>
            requestList = new MutableLiveData<>();

    // Friends
    private MutableLiveData<ArrayList<UserModel>>
            friendList = new MutableLiveData<>();

    // Error
    private MutableLiveData<String>
            errorMessage = new MutableLiveData<>();


    // Constructor
    public FriendViewModel() {

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();

    }



    //getter
    public LiveData<ArrayList<FriendRequestModel>>
    getRequestList() {

        return requestList;

    }

    public LiveData<ArrayList<UserModel>>
    getFriendList() {

        return friendList;

    }

    public LiveData<String>
    getErrorMessage() {

        return errorMessage;

    }



    //load friend request
    public void loadFriendRequests() {

        FirebaseUser currentUser =
                mAuth.getCurrentUser();

        if (currentUser == null) {

            errorMessage.setValue(
                    "User not logged in"
            );

            return;
        }

        String currentUid =
                currentUser.getUid();

        db.collection("friend_requests")
                .whereEqualTo(
                        "receiverId",
                        currentUid
                )
                .whereEqualTo(
                        "status",
                        "pending"
                )
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    ArrayList<FriendRequestModel> list =
                            new ArrayList<>();

                    for (DocumentSnapshot doc :
                            queryDocumentSnapshots
                                    .getDocuments()) {

                        FriendRequestModel request =
                                doc.toObject(
                                        FriendRequestModel.class
                                );

                        list.add(request);

                    }

                    requestList.setValue(list);

                })
                .addOnFailureListener(e -> {

                    errorMessage.setValue(
                            e.getMessage()
                    );

                });

    }


    // load friends

    public void loadFriends() {

        FirebaseUser currentUser =
                mAuth.getCurrentUser();

        if (currentUser == null) {

            errorMessage.setValue(
                    "User not logged in"
            );

            return;
        }

        String currentUid =
                currentUser.getUid();

        db.collection("users")
                .document(currentUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    ArrayList<String> friendIds =
                            (ArrayList<String>)
                                    documentSnapshot.get(
                                            "friendIds"
                                    );

                    ArrayList<UserModel> friends =
                            new ArrayList<>();

                    // no friends
                    if (friendIds == null
                            || friendIds.isEmpty()) {

                        friendList.setValue(friends);

                        return;
                    }

                    // loop all friend ids
                    for (String uid : friendIds) {

                        db.collection("users")
                                .document(uid)
                                .get()
                                .addOnSuccessListener(friendDoc -> {

                                    UserModel friend =
                                            friendDoc.toObject(
                                                    UserModel.class
                                            );

                                    if (friend != null) {

                                        friends.add(friend);

                                        // update live data
                                        friendList.setValue(
                                                friends
                                        );

                                    }

                                });

                    }

                })
                .addOnFailureListener(e -> {

                    errorMessage.setValue(
                            e.getMessage()
                    );

                });

    }

}