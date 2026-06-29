package com.example.diary_app.ui.pages.chat;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.diary_app.BuildConfig;
import com.example.diary_app.data.model.AiMessage;
import com.example.diary_app.data.model.ChatMessage;
import com.example.diary_app.repository.AiChatRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import org.json.JSONArray;
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

public class ChatAIViewModel extends ViewModel {
    private final AiChatRepository repository;
    private final MutableLiveData<List<ChatMessage>> messageListLiveData;
    private final MutableLiveData<String> errorLiveData;
    private final MutableLiveData<Boolean> isSendingLiveData;
    
    private ListenerRegistration chatListener;
    private final OkHttpClient client;
    private final String myUid;
    private final String API_KEY = BuildConfig.GEMINI_API_KEY;

    public ChatAIViewModel() {
        repository = new AiChatRepository();
        messageListLiveData = new MutableLiveData<>(new ArrayList<>());
        errorLiveData = new MutableLiveData<>();
        isSendingLiveData = new MutableLiveData<>(false);
        client = new OkHttpClient();
        
        String uid = FirebaseAuth.getInstance().getUid();
        myUid = (uid == null) ? "user_123" : uid;
        
        listenToChat();
    }

    private void listenToChat() {
        chatListener = repository.listenToAiChatHistory(myUid, (value, error) -> {
            if (error != null) {
                errorLiveData.postValue("Lỗi tải lịch sử chat: " + error.getMessage());
                return;
            }
            if (value != null) {
                List<ChatMessage> list = new ArrayList<>();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    AiMessage aiMsg = doc.toObject(AiMessage.class);
                    if (aiMsg != null) {
                        // Xác định xem tin nhắn là của user hay của gemini
                        String senderId = "user".equals(aiMsg.getRole()) ? myUid : "gemini_bot";
                        ChatMessage chatMsg = new ChatMessage(
                                aiMsg.getMessageId(),
                                senderId,
                                aiMsg.getContent(),
                                aiMsg.getCreatedAt()
                        );
                        list.add(chatMsg);
                    }
                }
                messageListLiveData.postValue(list);
            }
        });
    }

    public void sendMessage(String userMessage, String cachedTodayMood, String userName) {
        isSendingLiveData.postValue(true);

        // 1. Lưu tin nhắn của User vào Firebase
        AiMessage userAiMsg = new AiMessage("", "user", userMessage, Timestamp.now());
        repository.saveAiMessage(myUid, userAiMsg);

        // 2. Gửi gọi AI
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent?key=" + API_KEY;
        String systemInstructionText = "Act as a loving friend. Comfort " + userName + " warmly by name. Reply in Vietnamese, very short like a text message (1-3 sentences). Today's diary: [" + cachedTodayMood + "].";

        try {
            JSONObject jsonBody = new JSONObject();
            JSONArray contentsArray = new JSONArray();

            List<ChatMessage> currentHistory = messageListLiveData.getValue();
            if (currentHistory == null) currentHistory = new ArrayList<>();

            // Đưa 10 tin nhắn gần nhất vào làm context
            int startIdx = Math.max(0, currentHistory.size() - 10);
            for (int i = startIdx; i < currentHistory.size(); i++) {
                ChatMessage msg = currentHistory.get(i);
                JSONObject historyContentObj = new JSONObject();
                historyContentObj.put("role", msg.getSenderId().equals(myUid) ? "user" : "model");
                
                JSONArray historyPartsArray = new JSONArray();
                JSONObject historyPartObj = new JSONObject();
                historyPartObj.put("text", msg.getContent());
                historyPartsArray.put(historyPartObj);
                
                historyContentObj.put("parts", historyPartsArray);
                contentsArray.put(historyContentObj);
            }
            
            // Thêm câu hỏi hiện tại vào cuối cùng (vì currentHistory lúc này có thể chưa kịp nhận từ Firebase Listener)
            JSONObject currentContentObj = new JSONObject();
            currentContentObj.put("role", "user");
            JSONArray currentPartsArray = new JSONArray();
            JSONObject currentPartObj = new JSONObject();
            currentPartObj.put("text", userMessage);
            currentPartsArray.put(currentPartObj);
            currentContentObj.put("parts", currentPartsArray);
            contentsArray.put(currentContentObj);

            jsonBody.put("contents", contentsArray);

            JSONObject systemInstructionObj = new JSONObject();
            JSONArray siPartsArray = new JSONArray();
            JSONObject siPartObj = new JSONObject();
            siPartObj.put("text", systemInstructionText);
            siPartsArray.put(siPartObj);
            systemInstructionObj.put("parts", siPartsArray);
            jsonBody.put("system_instruction", systemInstructionObj);

            RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(url).post(body).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    isSendingLiveData.postValue(false);
                    errorLiveData.postValue("Lỗi mạng: " + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String rawResponse = response.body() != null ? response.body().string() : "";
                    
                    if (response.code() == 429) {
                        isSendingLiveData.postValue(false);
                        errorLiveData.postValue("Lỗi 429! Quá nhiều yêu cầu.");
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

                            // 3. Lưu phản hồi của AI vào Firebase. 
                            // Lưu xong Listener sẽ tự gọi onEvent và cập nhật UI.
                            AiMessage botAiMsg = new AiMessage("", "model", botReply, Timestamp.now());
                            repository.saveAiMessage(myUid, botAiMsg);

                        } catch (Exception e) {
                            Log.e("GEMINI_ERROR", "Lỗi Parse JSON: " + e.getMessage());
                            errorLiveData.postValue("AI phản hồi không đúng định dạng.");
                        }
                    } else {
                        errorLiveData.postValue("Lỗi API: " + response.code());
                    }
                    isSendingLiveData.postValue(false);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            isSendingLiveData.postValue(false);
        }
    }

    public LiveData<List<ChatMessage>> getMessageListLiveData() { return messageListLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }
    public LiveData<Boolean> getIsSendingLiveData() { return isSendingLiveData; }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (chatListener != null) {
            chatListener.remove(); // Hủy lắng nghe Firebase khi ViewModel bị hủy
        }
    }
}
