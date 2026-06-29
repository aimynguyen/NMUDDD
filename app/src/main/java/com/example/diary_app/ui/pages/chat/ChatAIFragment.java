package com.example.diary_app.ui.pages.chat;

import android.os.Bundle;
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
import com.example.diary_app.adapter.ChatAIAdapter;
import com.example.diary_app.data.model.ChatMessage;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ChatAIFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText editText;
    private ImageButton sendButton;
    private ChatAIAdapter chatAdapter;
    private List<ChatMessage> messageList;
    
    private ChatAIViewModel viewModel;

    // BỘ NHỚ ĐỆM TẠM THỜI (CACHE)
    private String cachedTodayMood = "Hôm nay người dùng chưa ghi lại cảm xúc hay nhật ký nào.";

    public ChatAIFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_ai, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        editText = view.findViewById(R.id.edtMessage);
        sendButton = view.findViewById(R.id.btnSend);
        ImageButton btnClose = view.findViewById(R.id.btnClose);

        btnClose.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) currentUserId = "user_123";

        messageList = new ArrayList<>();
        chatAdapter = new ChatAIAdapter(messageList, currentUserId);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(chatAdapter);
        
        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(ChatAIViewModel.class);
        
        // Quan sát dữ liệu tin nhắn
        viewModel.getMessageListLiveData().observe(getViewLifecycleOwner(), messages -> {
            int previousSize = messageList.size();
            messageList.clear();
            messageList.addAll(messages);
            chatAdapter.notifyDataSetChanged();
            
            // Cuộn xuống dòng cuối cùng nếu có tin nhắn mới hoặc list thay đổi
            if (messageList.size() > 0 && messageList.size() != previousSize) {
                recyclerView.scrollToPosition(messageList.size() - 1);
            }
        });
        
        // Quan sát lỗi
        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Quan sát trạng thái gửi để khóa/mở khóa nút gửi
        viewModel.getIsSendingLiveData().observe(getViewLifecycleOwner(), isSending -> {
            sendButton.setEnabled(!isSending);
        });

        loadTodayMoodFromFirebase();

        sendButton.setOnClickListener(v -> {
            String message = editText.getText().toString().trim();
            if (!message.isEmpty()) {
                String userName = "bạn";
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                    if (name != null && !name.trim().isEmpty()) {
                        userName = name;
                    }
                }
                
                // Gọi ViewModel để xử lý gửi tin nhắn
                viewModel.sendMessage(message, cachedTodayMood, userName);
                
                // Xóa ô nhập ngay lập tức
                editText.setText("");
            }
        });

        return view;
    }

    private void loadTodayMoodFromFirebase() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0);
        Date startOfDay = cal.getTime();

        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59);
        Date endOfDay = cal.getTime();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("diaries")
                .whereGreaterThanOrEqualTo("timestamp", new Timestamp(startOfDay))
                .whereLessThanOrEqualTo("timestamp", new Timestamp(endOfDay))
                .get()
                .addOnCompleteListener(task -> {
                    if (isAdded() && task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Dữ liệu nhật ký hôm nay của người dùng: ");
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String content = document.getString("content");
                            if (content != null) {
                                sb.append("- ").append(content).append(". ");
                            }
                        }
                        cachedTodayMood = sb.toString();
                    }
                });
    }
}
