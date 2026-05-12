package com.example.diary_app.ui.pages.chat;

import android.os.Bundle;
import android.os.Message;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diary_app.R;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;

public class ChatAIActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText editText;
    private ImageButton sendButton;

    private ChatAIAdapter chatAdapter;
    private List<Message> messageList;

    private GenerativeModelFutures modelFutures;
    private ChatFutures chatContext;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_chatAI);

        recyclerView = findViewById(R.id.recyclerView);
        editText = findViewById(R.id.edtMessage);
        sendButton = findViewById(R.id.btnSend);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAIAdapter(messageList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        GenerativeModel model = new GenerativeModel("gemini-1.5-flash", "AIzaSyASW_qgTgbCWqboIFOD8RZ7l6juzLG0zo8");
        model = GenerativeModelFutures.from(model).getGenerativeModel();

        chatContext= model.startChat(this);

        sendButton.setOnClickListener(v -> {
            String message = editText.getText().toString().trim();
            if (!message.isEmpty()) {
                sendChatToGemini(message);
            }
        });

    }
    private void sendChatToGemini(String userMessage) {
        // Thêm tin nhắn của người dùng vào giao diện
        messageList.add(new MessageModel(userMessage, MessageModel.SENT_BY_USER));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
        editText.setText("");

        // Gửi đến Gemini
        Content content = new Content.Builder().addText(userMessage).build();
        ListenableFuture<GenerateContentResponse> response = chatContext.sendMessage(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String botResponse = result.getText();
                runOnUiThread(() -> {
                    // Thêm phản hồi của Bot vào giao diện
                    messageList.add(new MessageModel(botResponse, MessageModel.SENT_BY_BOT));
                    chatAdapter.notifyItemInserted(messageList.size() - 1);
                    recyclerView.scrollToPosition(messageList.size() - 1);
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> Toast.makeText(ChatAIActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }, ContextCompat.getMainExecutor(this));
    }

}
