package com.example.diary_app.ui.pages.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
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
    private ImageView sendButton;

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

        // Sửa lại ID cho đúng với fragment_chat.xml và chat_input.xml
        recyclerView = view.findViewById(R.id.rvMessages);
        editText = view.findViewById(R.id.etMessage);
        sendButton = view.findViewById(R.id.btnSend);

        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        setupRecyclerView();
        observeViewModel();

        if (!TextUtils.isEmpty(chatId)) {
            chatViewModel.loadMessagesInRoom(chatId);
        }

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSendMessage();
            }
        });

        return view;
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
            if (!TextUtils.isEmpty(errorMsg)) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSendMessage() {
        String text = editText.getText().toString().trim();
        if (!text.isEmpty() && !TextUtils.isEmpty(userId)) {
            List<String> participants = Arrays.asList(userId, friendId);
            chatViewModel.sendMessage(chatId, userId, text, participants);
            editText.setText("");
        }
    }
}