package com.example.diary_app.repository;

import com.example.diary_app.data.model.AiMessage;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class AiChatRepository {
    private FirebaseFirestore db;

    public AiChatRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * 1. Lưu tin nhắn vào lịch sử AI Chat (Dùng cho cả tin của User và của Gemini)
     * @param myUid: ID của người dùng đang đăng nhập
     * @param message: Object chứa nội dung và role ("user" hoặc "model")
     */
    public Task<Void> saveAiMessage(String myUid, AiMessage message){
        WriteBatch batch = db.batch();

        // 1: Lưu vào subcollection "messages"
        DocumentReference messageRef = db.collection("ai_chats")
                .document(myUid) // Document ID chính là UID của user
                .collection("messages")
                .document();

        message.setMessageId(messageRef.getId());
        batch.set(messageRef, message);

        // 2: Cập nhật thông tin phòng chat ngoài cùng
        DocumentReference roomRef = db.collection("ai_chats").document(myUid);
        Map<String, Object> roomUpdates = new HashMap<>();
        roomUpdates.put("userId", myUid);
        roomUpdates.put("lastMessage", message.getContent());
        roomUpdates.put("lastUpdated", message.getCreatedAt());

        // Dùng SetOptions.merge() để nếu phòng chat chưa có thì tạo mới, có rồi thì update
        batch.set(roomRef, roomUpdates, SetOptions.merge());

        return batch.commit();
    }

    /**
     * 2. Lắng nghe lịch sử chat để hiển thị lên màn hình (Realtime)
     */
    public ListenerRegistration listenToAiChatHistory(String myUid, EventListener<QuerySnapshot> listener) {
        db.collection("ai_chats")
                .document(myUid)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener(listener);
    }

    /**
     * 3. Lấy toàn bộ lịch sử chat CŨ (Dành cho FE call API Gemini nạp context)
     * Dùng .get() thay vì lắng nghe realtime để truyền vào Gemini SDK
     */
    public Task<QuerySnapshot> getFullChatHistoryForGemini(String myUid) {
        return db.collection("ai_chats")
                .document(myUid)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get();
    }
}
