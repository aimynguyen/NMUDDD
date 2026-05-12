package com.example.diary_app.ui.pages.chat;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diary_app.R;
import com.example.diary_app.data.model.ChatMessage;
import com.google.firebase.Timestamp;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatAIActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText editText;
    private ImageButton sendButton;
    private ChatAIAdapter chatAdapter;
    private List<ChatMessage> messageList;

    private final String API_KEY = "AIzaSyAZY9S6LbYej8YFL9TYOZt4ZAqSJr-3kyg"; // Thay bằng Key thật của bạn
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_chat_ai);

        client = new OkHttpClient();
        recyclerView = findViewById(R.id.recyclerView);
        editText = findViewById(R.id.edtMessage);
        sendButton = findViewById(R.id.btnSend);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAIAdapter(messageList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        sendButton.setOnClickListener(v -> {
            String message = editText.getText().toString().trim();
            if (!message.isEmpty()) {
                sendChatToGemini(message);
            }
        });
    }

    private void sendChatToGemini(String userMessage) {
        // 1. Thêm tin nhắn User vào list
        ChatMessage userChat = new ChatMessage(
                String.valueOf(System.currentTimeMillis()),
                "user_123", // senderId của người dùng
                userMessage,
                Timestamp.now()
        );
        updateUI(userChat);
        editText.setText("");

        // 2. Chuẩn bị Request gửi lên Google API v1 (Stable)
        // Dùng đường dẫn này để tránh lỗi v1beta 404
        // Đảm bảo dùng v1 (hoặc v1beta nếu v1 lỗi) và CÓ dấu hai chấm trước generateContent
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

        String jsonBody = "{\"contents\":[{\"parts\":[{\"text\":\"" + userMessage.replace("\"", "\\\"") + "\"}]}]}";
        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        System.out.println("DEBUG_URL: " + url);

        // 3. Gọi API
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showToast("Lỗi mạng: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        String botReply = jsonObject.getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text");

                        runOnUiThread(() -> {
                            ChatMessage botChat = new ChatMessage(
                                    String.valueOf(System.currentTimeMillis()),
                                    "gemini_bot", // senderId của Bot
                                    botReply,
                                    Timestamp.now()
                            );
                            updateUI(botChat);
                        });
                    } catch (Exception e) {
                        showToast("Lỗi xử lý JSON");
                    }
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "null";
                    android.util.Log.e("GEMINI_ERROR_DETAIL", errorBody);

                    runOnUiThread(() -> Toast.makeText(ChatAIActivity.this, "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show());
                }
                if (response.code() == 429) {
                    showToast("Bạn đã nhắn quá nhanh! Hãy đợi một chút rồi thử lại nha bạn yêu ơi.");
                    return;
                }
            }
        });
    }

    private void updateUI(ChatMessage message) {
        runOnUiThread(() -> {
            messageList.add(message);
            chatAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1);
        });
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }
}