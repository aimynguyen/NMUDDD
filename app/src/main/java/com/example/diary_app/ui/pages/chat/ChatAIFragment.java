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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diary_app.BuildConfig;
import com.example.diary_app.R;
import com.example.diary_app.adapter.ChatAIAdapter;
import com.example.diary_app.data.model.ChatMessage;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatAIFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText editText;
    private ImageButton sendButton;
    private ChatAIAdapter chatAdapter;
    private List<ChatMessage> messageList;

    private final String API_KEY = BuildConfig.GEMINI_API_KEY;
    private OkHttpClient client;
    private boolean isSending = false; // Biến cờ ngăn gửi tin nhắn liên tục

    // BỘ NHỚ ĐỆM TẠM THỜI (CACHE)
    private String cachedTodayMood = "Hôm nay người dùng chưa ghi lại cảm xúc hay nhật ký nào.";

    public ChatAIFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_ai, container, false);

        client = new OkHttpClient();

        recyclerView = view.findViewById(R.id.recyclerView);
        editText = view.findViewById(R.id.edtMessage);
        sendButton = view.findViewById(R.id.btnSend);

        messageList = new ArrayList<>();

        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) currentUserId = "user_123";

        chatAdapter = new ChatAIAdapter(messageList, currentUserId);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(chatAdapter);

        loadTodayMoodFromFirebase();

        sendButton.setOnClickListener(v -> {
            String message = editText.getText().toString().trim();
            if (!message.isEmpty() && !isSending) {
                sendChatToGemini(message);
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

    private void sendChatToGemini(String userMessage) {
        isSending = true;
        sendButton.setEnabled(false);

        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) currentUserId = "user_123";

        ChatMessage userChat = new ChatMessage(
                String.valueOf(System.currentTimeMillis()),
                currentUserId,
                userMessage,
                Timestamp.now()
        );
        updateUI(userChat);
        editText.setText("");

        // SỬA LẠI ĐÚNG TÊN MODEL TRÊN DASHBOARD CỦA BẠN ĐỂ HẾT LỖI 404
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent?key=" + API_KEY;

        String userName = "bạn";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            if (name != null && !name.trim().isEmpty()) {
                userName = name;
            }
        }

        String systemInstructionText = "Act as a loving friend. Comfort " + userName + " warmly by name. Reply in Vietnamese, very short like a text message (1-3 sentences). Today's diary: [" + cachedTodayMood + "].";

        String jsonString = "";
        try {
            JSONObject jsonBody = new JSONObject();
            JSONArray contentsArray = new JSONArray();

            String currentUId = FirebaseAuth.getInstance().getUid();
            if (currentUId == null) currentUId = "user_123";

            // 1. DUYỆT QUA LỊCH SỬ CHAT TRONG messageList ĐỂ ĐÓNG GÓI GỬI ĐI
            // (Giới hạn khoảng 6-10 câu gần nhất để không bị quá tải số lượng chữ - TPM)
            int startIdx = Math.max(0, messageList.size() - 10);
            for (int i = startIdx; i < messageList.size(); i++) {
                ChatMessage msg = messageList.get(i);

                JSONObject historyContentObj = new JSONObject();
                // Xác định ai là người nói dựa trên ID
                if (msg.getSenderId().equals(currentUserId)) {
                    historyContentObj.put("role", "user");
                } else {
                    historyContentObj.put("role", "model");
                }

                JSONArray historyPartsArray = new JSONArray();
                JSONObject historyPartObj = new JSONObject();
                historyPartObj.put("text", msg.getContent());
                historyPartsArray.put(historyPartObj);

                historyContentObj.put("parts", historyPartsArray);
                contentsArray.put(historyContentObj);
            }

            // 2. THÊM CHÍNH CÂU HỎI HIỆN TẠI VÀO CUỐI DANH SÁCH (Vì câu này đã được updateUI trước đó)
            // Lưu ý: Do hàm updateUI(userChat) của bạn chạy TRƯỚC khi tạo JSON,
            // nên vòng lặp FOR ở trên đã tự động lấy luôn cả câu hỏi hiện tại này rồi.
            // Bạn không cần phải add thủ công userMessage thêm lần nữa để tránh bị lặp đúp.

            jsonBody.put("contents", contentsArray);

            // 3. GIỮ NGUYÊN SYSTEM INSTRUCTION CỦA BẠN
            JSONObject systemInstructionObj = new JSONObject();
            JSONArray siPartsArray = new JSONArray();
            JSONObject siPartObj = new JSONObject();
            siPartObj.put("text", systemInstructionText);
            siPartsArray.put(siPartObj);
            systemInstructionObj.put("parts", siPartsArray);
            jsonBody.put("system_instruction", systemInstructionObj);

            jsonString = jsonBody.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                resetSendingStatus();
                showToast("Lỗi mạng: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // ĐỌC NỘI DUNG PHẢN HỒI NGAY TỪ ĐẦU (Chỉ được đọc 1 lần duy nhất)
                String rawResponse = response.body() != null ? response.body().string() : "";
                
                android.util.Log.e("GEMINI_ERROR", "HTTP Code: " + response.code() + " | Body: " + rawResponse);

                if (response.code() == 429) {
                    resetSendingStatus();
                    showToast("Lỗi 429! Xem Logcat (GEMINI_ERROR) để biết lý do.");
                    return;
                }

                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(rawResponse);
                        String botReply = jsonObject.getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text");

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                ChatMessage botChat = new ChatMessage(
                                        String.valueOf(System.currentTimeMillis()),
                                        "gemini_bot",
                                        botReply,
                                        Timestamp.now()
                                );
                                updateUI(botChat);
                                resetSendingStatus();
                            });
                        }
                    } catch (Exception e) {
                        resetSendingStatus();
                        android.util.Log.e("GEMINI_ERROR", "Lỗi Parse JSON: " + e.getMessage());
                        showToast("AI phản hồi không đúng định dạng.");
                    }
                } else {
                    resetSendingStatus();
                    showToast("Lỗi: " + response.code());
                }
            }
        });
    }

    private void resetSendingStatus() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                isSending = false;
                sendButton.setEnabled(true);
            });
        }
    }

    private void updateUI(ChatMessage message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                messageList.add(message);
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                recyclerView.scrollToPosition(messageList.size() - 1);
            });
        }
    }

    private void showToast(String msg) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show());
        }
    }
}
