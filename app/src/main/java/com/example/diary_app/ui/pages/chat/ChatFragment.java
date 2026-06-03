package com.example.diary_app.ui.pages.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diary_app.R;
import com.example.diary_app.adapter.ChatAdapter;
import com.example.diary_app.viewmodel.ChatViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText editText;
    private ImageButton sendButton;

    private ChatViewModel chatViewModel;
    private ChatAdapter chatAdapter;

    private String chatId;   // ID phòng chat (Ví dụ: "uid_2_uid_3")
    private String userId;   // UID của chính bạn
    private String friendId; // UID của đối phương

    public ChatFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout cho Fragment này
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        // 1. Lấy thông tin truyền từ màn hình/Activity trước sang qua Arguments (Bundle)
        if (getArguments() != null) {
            chatId = getArguments().getString("CHAT_ID");
            userId = getArguments().getString("MY_UID");
            friendId = getArguments().getString("RECEIVER_UID");
        }

        // Ánh xạ các View từ XML layout của Fragment
        recyclerView = view.findViewById(R.id.recyclerView);
        editText = view.findViewById(R.id.edtMessage);
        sendButton = view.findViewById(R.id.btnSend);

        // 2. Khởi tạo ViewModel (Lưu ý truyền 'this' của Fragment vào Provider)
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        // 3. Setup RecyclerView và Adapter
        setupRecyclerView();

        // 4. Lắng nghe (Observe) dữ liệu real-time từ ViewModel
        observeViewModel();

        // 5. Bắt đầu tải và bắt sự kiện tin nhắn mới từ Firebase
        if (!TextUtils.isEmpty(chatId)) {
            chatViewModel.loadMessagesInRoom(chatId);
        }

        // 6. Xử lý sự kiện khi ấn nút Gửi
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSendMessage();
            }
        });

        return view;
    }

    private void setupRecyclerView() {
        // Khởi tạo Adapter với list trống ban đầu, truyền kèm userId để phân biệt trái/phải
        chatAdapter = new ChatAdapter(new ArrayList<>(), userId);

        // Thay 'this' bằng 'requireContext()' khi ở trong Fragment
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true); // Đẩy tin nhắn mới nhất nằm dưới cùng màn hình

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(chatAdapter);
    }

    private void observeViewModel() {
        // Sử dụng 'getViewLifecycleOwner()' thay vì 'this' để tối ưu vòng đời LiveData trong Fragment
        chatViewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            if (messages != null) {
                chatAdapter.updateList(messages);
                if (!messages.isEmpty()) {
                    recyclerView.smoothScrollToPosition(messages.size() - 1); // Cuộn xuống cuối
                }
            }
        });

        // Bắt lỗi nếu chẳng may mất mạng hoặc lỗi bảo mật Firebase
        chatViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMsg -> {
            if (!TextUtils.isEmpty(errorMsg)) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSendMessage() {
        String text = editText.getText().toString().trim();

        if (!text.isEmpty()) {
            // Danh sách 2 người tham gia chat đôi (gồm tôi và bạn tôi)
            List<String> participants = Arrays.asList(userId, friendId);

            // Gửi thẳng qua hàm sendMessage của ViewModel
            chatViewModel.sendMessage(chatId, userId, text, participants);

            // Xóa trống ô nhập chữ sau khi gửi
            editText.setText("");
        }
    }
}