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

    // BỘ NHỚ ĐỆM TẠM THỜI (CACHE)
    private String cachedTodayMood = "Hôm nay người dùng chưa ghi lại cảm xúc hay nhật ký nào.";

    // Constructor rỗng bắt buộc phải có đối với Fragment
    public ChatAIFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. Inflate layout cho Fragment này
        View view = inflater.inflate(R.layout.fragment_chat_ai, container, false);

        client = new OkHttpClient();

        // 2. Ánh xạ các View thông qua đối tượng "view"
        recyclerView = view.findViewById(R.id.recyclerView);
        editText = view.findViewById(R.id.edtMessage);
        sendButton = view.findViewById(R.id.btnSend);

        messageList = new ArrayList<>();

        // Lấy userId hiện tại từ Firebase
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) currentUserId = "user_123";

        chatAdapter = new ChatAIAdapter(messageList, currentUserId);

        // Thay "this" bằng "getContext()"
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(chatAdapter);

        // Đọc dữ liệu từ Firebase
        loadTodayMoodFromFirebase();

        sendButton.setOnClickListener(v -> {
            String message = editText.getText().toString().trim();
            if (!message.isEmpty()) {
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
                    // Kiểm tra xem Fragment có còn gắn với Activity hay không trước khi xử lý UI/Data
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

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent?key=" + API_KEY;

        String systemInstructionText = "Role: Act as an empathetic psychologist or a close, supportive friend.\n" +
                "Task: Your task is to comfort, encourage, and motivate the user.\n" +
                "Context: Providing emotional support and a safe space to vent.\n" +
                "Database Context (Dữ liệu nhật ký hôm nay của người dùng lấy từ Firebase): [" + cachedTodayMood + "]. " +
                "Hãy ưu tiên sử dụng dữ liệu này nếu người dùng hỏi về cảm xúc, tâm trạng của họ ngày hôm nay.\n" +
                "Constraints:\n" +
                "- Tone of voice: Gentle, warm, empathetic, and slightly humorous to cheer them up.\n" +
                "- Length: Under 100 words.\n" +
                "- Do NOT: Do not judge, do not sound overly clinical, and strictly do NOT create any pressure.\n" +
                "Format: Output the result as a cohesive paragraph.\n" +
                "Language: Please output the final result in Vietnamese.";

        String jsonString = "";
        try {
            JSONObject jsonBody = new JSONObject();

            JSONArray contentsArray = new JSONArray();
            JSONObject contentObj = new JSONObject();
            JSONArray partsArray = new JSONArray();
            JSONObject partObj = new JSONObject();
            partObj.put("text", userMessage);
            partsArray.put(partObj);
            contentObj.put("parts", partsArray);
            contentsArray.put(contentObj);
            jsonBody.put("contents", contentsArray);

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

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showToast("Lỗi mạng: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String rawResponse = response.body() != null ? response.body().string() : "";

                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(rawResponse);
                        String botReply = jsonObject.getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text");

                        // Thay runOnUiThread trực tiếp bằng getActivity().runOnUiThread(...)
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                ChatMessage botChat = new ChatMessage(
                                        String.valueOf(System.currentTimeMillis()),
                                        "gemini_bot",
                                        botReply,
                                        Timestamp.now()
                                );
                                updateUI(botChat);
                            });
                        }
                    } catch (Exception e) {
                        showToast("Lỗi xử lý dữ liệu AI");
                    }
                } else {
                    android.util.Log.e("GEMINI_ERROR", "Code: " + response.code() + " | Body: " + rawResponse);
                    showToast("Lỗi từ máy chủ: " + response.code());
                }
            }
        });
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
            // Thay "this" trong Toast thành "getContext()"
            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show());
        }
    }
}
