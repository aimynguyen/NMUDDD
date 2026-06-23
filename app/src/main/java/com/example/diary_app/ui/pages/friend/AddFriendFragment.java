package com.example.diary_app.ui.pages.friend;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diary_app.R;
import com.example.diary_app.adapter.SearchUserAdapter;
import com.example.diary_app.core.NotiType;
import com.example.diary_app.data.model.User;
import com.example.diary_app.repository.NotificationRepository;
import com.example.diary_app.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddFriendFragment extends Fragment {

    private EditText edtSearch;
    private ImageView iconClear, iconSearch;
    private RecyclerView recyclerView;

    private SearchUserAdapter adapter;
    private List<User> searchResults;
    private UserRepository userRepository;
    private NotificationRepository notificationRepository;
    private String currentUserId;
    private String currentUserName = "Ai đó"; // Tên mặc định

    // Thêm biến này ở đầu Fragment cùng các biến khác
    private List<String> sentRequestIds = new ArrayList<>();

    // Yêu cầu một constructor rỗng theo chuẩn của Fragment
    public AddFriendFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // "Bơm" layout của bạn vào Fragment.
        // Đảm bảo tên file layout đúng với tên bạn đã lưu (ví dụ: fragment_add_friend)
        View view = inflater.inflate(R.layout.fragment_addfriend, container, false);

        initData();
        initViews(view);
        setupSearch();

        return view;
    }

    private void initData() {
        userRepository = new UserRepository();
        notificationRepository = new NotificationRepository();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            // Lấy thêm tên user hiện tại để gắn vào thông báo
            userRepository.getUserProfile(currentUserId).addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists() && documentSnapshot.getString("userName") != null) {
                    currentUserName = documentSnapshot.getString("userName");
                }
            });
        } else {
            Toast.makeText(requireContext(), "Lỗi: Chưa đăng nhập!", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews(View view) {
        // Trong Fragment, phải gọi findViewById thông qua view
        edtSearch = view.findViewById(R.id.edtSearch);
        iconClear = view.findViewById(R.id.iconClear);
        iconSearch = view.findViewById(R.id.iconSearch);

        // Nhớ đổi id trong file XML của bạn thành recyclerViewSuggestFriend
        recyclerView = view.findViewById(R.id.recyclerViewSuggestFriend);

        searchResults = new ArrayList<>();
        adapter = new SearchUserAdapter(searchResults, (targetUser, position) -> {
            if (currentUserId != null) {
                sendFriendRequest(targetUser);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        iconClear.setOnClickListener(v -> edtSearch.setText(""));
    }

    private void setupSearch() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    iconClear.setVisibility(View.VISIBLE);
                } else {
                    iconClear.setVisibility(View.GONE);
                    searchResults.clear();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if(query.length() > 0){
                    performSearch(query);
                }
            }
        });
    }

    private void performSearch(String emailQuery) {
        String queryLowerCase = emailQuery.toLowerCase();

        // 1. Lấy danh sách lời mời mà BẠN đã gửi và đang chờ (pending)
        FirebaseFirestore.getInstance().collection("friend_requests")
                .whereEqualTo("senderId", currentUserId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(requestSnapshots -> {
                    sentRequestIds.clear();
                    for (QueryDocumentSnapshot doc : requestSnapshots) {
                        sentRequestIds.add(doc.getString("receiverId"));
                    }

                    // 2. Sau khi có danh sách đã gửi, tiến hành tìm kiếm User
                    userRepository.searchUserByEmail(queryLowerCase).addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            searchResults.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                User user = document.toObject(User.class);
                                if (currentUserId != null && !user.getUid().equals(currentUserId)) {
                                    searchResults.add(user);
                                }
                            }
                            // Gửi danh sách search và danh sách đã gửi lời mời sang Adapter
                            adapter.updateList(searchResults, sentRequestIds);
                        }
                    });
                });
    }

    private void sendFriendRequest(User targetUser) {
        userRepository.sendFriendRequest(currentUserId, targetUser.getUid())
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(requireContext(), "Đã gửi lời mời tới " + targetUser.getEmail(), Toast.LENGTH_SHORT).show();
                    // Gửi thông báo cho người nhận
                    notificationRepository.sendNotification(
                            targetUser.getUid(),
                            currentUserId,
                            NotiType.FRIEND_REQUEST,
                            currentUserId,
                            currentUserName + " đã gửi cho bạn một lời mời kết bạn"
                    );
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}