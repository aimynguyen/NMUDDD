package com.example.diary_app.ui.pages.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.diary_app.R;
import com.example.diary_app.adapter.ChatAdapter;
import com.example.diary_app.data.model.User;
import com.example.diary_app.repository.UserRepository;
import com.example.diary_app.viewmodel.ChatViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText editText;
    private ImageView sendButton;
    private TextView tvName;
    private ImageView imgAvatar;
    private ImageView btnBack;

    private ChatViewModel chatViewModel;
    private ChatAdapter chatAdapter;

    private String chatId;   
    private String userId;   
    private String friendId; 

    public ChatFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        if (getArguments() != null) {
            chatId = getArguments().getString("CHAT_ID");
            userId = getArguments().getString("MY_UID");
            friendId = getArguments().getString("RECEIVER_UID");
        }

        // Đảm bảo luôn có chatId để không bị crash khi Firestore truy cập đường dẫn null
        if (TextUtils.isEmpty(chatId) && !TextUtils.isEmpty(userId) && !TextUtils.isEmpty(friendId)) {
            chatId = generateChatId(userId, friendId);
        }

        // Ánh xạ View
        recyclerView = view.findViewById(R.id.rvMessages);
        editText = view.findViewById(R.id.etMessage);
        sendButton = view.findViewById(R.id.btnSend);
        tvName = view.findViewById(R.id.tvName);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        btnBack = view.findViewById(R.id.btnBack);

        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        setupRecyclerView();
        observeViewModel();
        loadFriendInfo();

        if (!TextUtils.isEmpty(chatId)) {
            chatViewModel.loadMessagesInRoom(chatId);
        }

        sendButton.setOnClickListener(v -> handleSendMessage());

        // SỬA LỖI CRASH: Sử dụng OnBackPressedDispatcher để thoát màn hình an toàn
        // Tìm đến dòng xử lý btnBack trong phương thức onCreateView
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                // Sử dụng Navigation Controller để quay lại màn hình trước đó
                androidx.navigation.Navigation.findNavController(v).popBackStack();
            });
        }

        return view;
    }

    private void loadFriendInfo() {
        if (TextUtils.isEmpty(friendId)) return;

        UserRepository userRepository = new UserRepository();
        userRepository.getUserProfile(friendId).addOnSuccessListener(documentSnapshot -> {
            // Kiểm tra isAdded() để tránh crash nếu Fragment đã bị hủy trước khi data trả về
            if (isAdded() && documentSnapshot.exists()) {
                User friend = documentSnapshot.toObject(User.class);
                if (friend != null) {
                    if (tvName != null) tvName.setText(friend.getUserName());
                    if (imgAvatar != null && !TextUtils.isEmpty(friend.getAvatarUrl())) {
                        Glide.with(this)
                                .load(friend.getAvatarUrl())
                                .placeholder(R.drawable.avatar_circle)
                                .circleCrop()
                                .into(imgAvatar);
                    }
                }
            }
        });
    }

    private String generateChatId(String uid1, String uid2) {
        return (uid1.compareTo(uid2) < 0) ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(new ArrayList<>(), userId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true); 

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(chatAdapter);
    }

    private void observeViewModel() {
        chatViewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            if (messages != null) {
                chatAdapter.updateList(messages);
                if (!messages.isEmpty()) {
                    recyclerView.smoothScrollToPosition(messages.size() - 1);
                }
            }
        });

        chatViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMsg -> {
            if (!TextUtils.isEmpty(errorMsg) && isAdded()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSendMessage() {
        String text = editText.getText().toString().trim();
        
        if (TextUtils.isEmpty(chatId)) {
            Toast.makeText(getContext(), "Lỗi: Không xác định được phòng chat", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!text.isEmpty() && !TextUtils.isEmpty(userId)) {
            List<String> participants = Arrays.asList(userId, friendId != null ? friendId : "");
            chatViewModel.sendMessage(chatId, userId, text, participants);
            editText.setText("");
        }
    }
}
