//region code cũ
//package com.example.diary_app.ui.pages.chat;
//
//import android.os.Bundle;
//import android.widget.EditText;
//import android.widget.ImageButton;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.diary_app.BuildConfig;
//import com.example.diary_app.R;
//import com.example.diary_app.data.model.ChatMessage;
//import com.google.firebase.Timestamp;
//
//import org.json.JSONObject;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.MediaType;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;
//
//public class ChatAIActivity extends AppCompatActivity {
//
//    private RecyclerView recyclerView;
//    private EditText editText;
//    private ImageButton sendButton;
//    private ChatAIAdapter chatAdapter;
//    private List<ChatMessage> messageList;
//
//    private final String API_KEY = BuildConfig.GEMINI_API_KEY; // Thay bằng Key thật của bạn
//    private OkHttpClient client;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.fragment_chat_ai);
//
//        client = new OkHttpClient();
//        recyclerView = findViewById(R.id.recyclerView);
//        editText = findViewById(R.id.edtMessage);
//        sendButton = findViewById(R.id.btnSend);
//
//        messageList = new ArrayList<>();
//        chatAdapter = new ChatAIAdapter(messageList);
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setAdapter(chatAdapter);
//
//        sendButton.setOnClickListener(v -> {
//            String message = editText.getText().toString().trim();
//            if (!message.isEmpty()) {
//                String promt = "Role: Act as an empathetic psychologist or a close, supportive friend.\n" +
//                        "\n" +
//                        "Task: Your task is to comfort, encourage, and motivate the user.\n" +
//                        "\n" +
//                        "Context: \n" +
//                        "- Topic: Providing emotional support and a safe space to vent.\n" +
//                        "- Target Audience: Someone who is feeling vulnerable, needs emotional care, and wants to confide in someone.\n" +
//                        "- Goal: To uplift their spirit, offer reassurance, and make them feel deeply heard and comforted.\n" +
//                        "\n" +
//                        "Constraints:\n" +
//                        "- Tone of voice: Gentle, warm, empathetic, and slightly humorous to cheer them up.\n" +
//                        "- Length: Under 100 words.\n" +
//                        "- Do NOT: Do not judge, do not sound overly clinical, and strictly do NOT create any pressure, guilt, or burden for the user.\n" +
//                        "\n" +
//                        "Format: Output the result as a cohesive paragraph.\n" +
//                        "\n" +
//                        "Language: Please output the final result in Vietnamese.";
//                String finalPromt = promt + "\n\nUser: " + message;
//                sendChatToGemini(finalPromt);
//            }
//        });
//    }
//
//    private void sendChatToGemini(String userMessage) {
//        // 1. Thêm tin nhắn User vào list
//        ChatMessage userChat = new ChatMessage(
//                String.valueOf(System.currentTimeMillis()),
//                "user_123", // senderId của người dùng
//                userMessage,
//                Timestamp.now()
//        );
//        updateUI(userChat);
//        editText.setText("");
//
//        // 2. Chuẩn bị Request gửi lên Google API v1 (Stable)
//        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent?key=" + API_KEY;
//
//        String jsonBody = "{\"contents\":[{\"parts\":[{\"text\":\"" + userMessage.replace("\"", "\\\"") + "\"}]}]}";
//        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
//
//        Request request = new Request.Builder()
//                .url(url)
//                .post(body)
//                .build();
//
//        System.out.println("DEBUG_URL: " + url);
//
//        // 3. Gọi API
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                showToast("Lỗi mạng: " + e.getMessage());
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                // Lưu body vào biến string ngay lập tức
//                String rawResponse = response.body() != null ? response.body().string() : "";
//
//                if (response.isSuccessful()) {
//                    try {
//                        JSONObject jsonObject = new JSONObject(rawResponse);
//                        String botReply = jsonObject.getJSONArray("candidates")
//                                .getJSONObject(0)
//                                .getJSONObject("content")
//                                .getJSONArray("parts")
//                                .getJSONObject(0)
//                                .getString("text");
//
//                        runOnUiThread(() -> {
//                            ChatMessage botChat = new ChatMessage(
//                                    String.valueOf(System.currentTimeMillis()),
//                                    "gemini_bot",
//                                    botReply,
//                                    Timestamp.now()
//                            );
//                            updateUI(botChat);
//                        });
//                    } catch (Exception e) {
//                        showToast("Lỗi xử lý dữ liệu AI");
//                    }
//                } else {
//                    // Log chi tiết lỗi thật sự từ Google để debug
//                    android.util.Log.e("GEMINI_ERROR", "Code: " + response.code() + " | Body: " + rawResponse);
//
//                    if (response.code() == 429) {
//                        showToast("Hệ thống đang quá tải hoặc sai Model ID. Thử lại sau nhé!");
//                    } else {
//                        showToast("Lỗi từ máy chủ: " + response.code());
//                    }
//                }
//            }
//        });
//    }
//
//    private void updateUI(ChatMessage message) {
//        runOnUiThread(() -> {
//            messageList.add(message);
//            chatAdapter.notifyItemInserted(messageList.size() - 1);
//            recyclerView.scrollToPosition(messageList.size() - 1);
//        });
//    }
//
//    private void showToast(String msg) {
//        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
//    }
//}
//endregion

//region code mới
package com.example.diary_app.ui.pages.chat;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diary_app.BuildConfig;
import com.example.diary_app.R;
import com.example.diary_app.data.model.ChatMessage;
import com.google.firebase.Timestamp;
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

public class ChatAIActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText editText;
    private ImageButton sendButton;
    private ChatAIAdapter chatAdapter;
    private List<ChatMessage> messageList;

    private final String API_KEY = BuildConfig.GEMINI_API_KEY;
    private OkHttpClient client;

    // BỘ NHỚ ĐỆM TẠM THỜI (CACHE): Lưu dữ liệu tâm trạng từ Firebase để tránh đọc đi đọc lại nhiều lần
    private String cachedTodayMood = "Hôm nay người dùng chưa ghi lại cảm xúc hay nhật ký nào.";

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

        // [TỐI ƯU FIREBASE]: Chỉ gọi Firebase đọc ĐÚNG 1 LẦN khi vừa mở màn hình chat lên
        loadTodayMoodFromFirebase();

        sendButton.setOnClickListener(v -> {
            String message = editText.getText().toString().trim();
            if (!message.isEmpty()) {
                // [FIX LỖI UI]: Chỉ truyền tin nhắn THUẦN của người dùng, không dính Prompt tiếng Anh
                sendChatToGemini(message);
            }
        });
    }

    /**
     * Hàm âm thầm đọc dữ liệu từ Cloud Firestore lên RAM khi mở màn hình.
     * Hãy chắc chắn các tên "diaries", "timestamp", "content" trùng với Firebase Console của bạn.
     */
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
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Dữ liệu nhật ký hôm nay của người dùng: ");
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String content = document.getString("content");
                            if (content != null) {
                                sb.append("- ").append(content).append(". ");
                            }
                        }
                        cachedTodayMood = sb.toString(); // Gán vào RAM để dùng nhiều lần
                    }
                });
    }

    private void sendChatToGemini(String userMessage) {
        // 1. UI hiển thị tin nhắn sạch sẽ của User
        ChatMessage userChat = new ChatMessage(
                String.valueOf(System.currentTimeMillis()),
                "user_123",
                userMessage, // <--- Bong bóng chat hiển thị cực đẹp, không chứa Prompt hệ thống
                Timestamp.now()
        );
        updateUI(userChat);
        editText.setText("");

        // 2. [SỬA LỖI 404]: Cập nhật URL model chuẩn nhất hiện tại
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent?key=" + API_KEY;

        // Chuẩn bị khung vai trò hệ thống kết hợp với dữ liệu đã lưu trong RAM
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

        // 3. Xây dựng cấu trúc JSON chuẩn của Google để giấu Prompt chạy ngầm và TỐI ƯU TOKEN
        String jsonString = "";
        try {
            JSONObject jsonBody = new JSONObject();

            // Phần nội dung chat hiện tại (Chỉ có tin nhắn gốc của user)
            JSONArray contentsArray = new JSONArray();
            JSONObject contentObj = new JSONObject();
            JSONArray partsArray = new JSONArray();
            JSONObject partObj = new JSONObject();
            partObj.put("text", userMessage);
            partsArray.put(partObj);
            contentObj.put("parts", partsArray);
            contentsArray.put(contentObj);
            jsonBody.put("contents", contentsArray);

            // [TỐI ƯU TOKEN]: Phần lệnh hệ thống, Google sẽ tự động Cache lại để giảm chi phí token khi chat nhiều câu
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

        // 4. Thực thi gọi API bằng OkHttp
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

                        runOnUiThread(() -> {
                            ChatMessage botChat = new ChatMessage(
                                    String.valueOf(System.currentTimeMillis()),
                                    "gemini_bot",
                                    botReply,
                                    Timestamp.now()
                            );
                            updateUI(botChat);
                        });
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
//endregion