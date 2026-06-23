package com.example.diary_app.ui.pages.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.diary_app.R;
import com.example.diary_app.adapter.FriendAdapter;
import com.example.diary_app.adapter.FriendRequestAdapter;
import com.example.diary_app.data.model.User;
import com.example.diary_app.viewmodel.ProfileViewModel;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    private TextView txtName;
    private TextView txtEditProfile;
    private ImageView imgAvatar;
    private ImageView btnSetting;
    private ProfileViewModel profileViewModel;
    private RecyclerView rvRequests;
    private RecyclerView rvFriends;
    private FriendAdapter friendAdapter;
    private FriendRequestAdapter friendRequestAdapter;
    private TextView emptyRequest;
    private TextView emptyFriend;

    public ProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. Inflate layout cho Fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // 2. Ánh xạ các View từ XML (sử dụng biến view)
        txtName = view.findViewById(R.id.txtName);
        txtEditProfile = view.findViewById(R.id.txtEditProfile);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        btnSetting = view.findViewById(R.id.btnSetting);
        rvRequests = view.findViewById(R.id.rvRequests);
        rvFriends = view.findViewById(R.id.rvFriends);
        emptyRequest = view.findViewById(R.id.txtEmptyRequest);
        emptyFriend = view.findViewById(R.id.txtEmptyFriends);

        // Setup RecyclerViews
        if (rvRequests != null) {
            rvRequests.setLayoutManager(new LinearLayoutManager(getContext()));
            friendRequestAdapter = new FriendRequestAdapter(new ArrayList<>(), new FriendRequestAdapter.OnRequestActionListener() {
                @Override
                public void onAccept(User user) {
                    profileViewModel.acceptRequestBySender(user.getUid());
                }

                @Override
                public void onReject(User user) {
                    profileViewModel.rejectRequestBySender(user.getUid());
                }
            });
            rvRequests.setAdapter(friendRequestAdapter);
        }

        if (rvFriends != null) {
            rvFriends.setLayoutManager(new LinearLayoutManager(getContext()));
            friendAdapter = new FriendAdapter(new ArrayList<>(), new FriendAdapter.OnFriendActionListener() {
                @Override
                public void onUnfriend(User user) {
                    new android.app.AlertDialog.Builder(getContext())
                            .setTitle("Hủy kết bạn")
                            .setMessage("Bạn chắc chắn muốn hủy kết bạn với " + user.getUserName() + " không?")
                            .setPositiveButton("Có", (dialog, which) -> {
                                profileViewModel.unfriendUser(user.getUid());
                            })
                            .setNegativeButton("Không", null)
                            .show();
                }
            });
            rvFriends.setAdapter(friendAdapter);
        }

        // Đảm bảo View có thể nhận sự kiện click
        if (btnSetting != null) {
            btnSetting.setClickable(true);
            btnSetting.setFocusable(true);
        }

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // 4. Load data
        profileViewModel.loadProfile();

        profileViewModel.getUserName().observe(getViewLifecycleOwner(), name -> {
            if (name != null) {
                txtName.setText(name);
            }
        });

        profileViewModel.getAvatarUrl().observe(getViewLifecycleOwner(), url -> {
            if (isAdded()) {
                Glide.with(this)
                        .load(url)
                        .placeholder(R.drawable.human_human)
                        .error(R.drawable.human_human)
                        .circleCrop()
                        .into(imgAvatar);
            }
        });

        profileViewModel.getFriendsLiveData().observe(getViewLifecycleOwner(), friends -> {
            if (friendAdapter != null) {
                friendAdapter.updateList(friends);
            }
            if (emptyFriend != null) {
                if (friends == null || friends.isEmpty()) {
                    emptyFriend.setVisibility(View.VISIBLE);
                } else {
                    emptyFriend.setVisibility(View.GONE);
                }
            }
        });

        profileViewModel.getRequestsLiveData().observe(getViewLifecycleOwner(), requests -> {
            if (friendRequestAdapter != null) {
                friendRequestAdapter.updateList(requests);
            }
            if (emptyRequest != null) {
                if (requests == null || requests.isEmpty()) {
                    emptyRequest.setVisibility(View.VISIBLE);
                } else {
                    emptyRequest.setVisibility(View.GONE);
                }
            }
        });

        txtEditProfile.setOnClickListener(v -> {
            NavHostFragment.findNavController(ProfileFragment.this)
                    .navigate(R.id.action_nav_profile_to_nav_edit_profile);
        });

        return view;
    }
}