package com.example.diary_app.repository;

import com.example.diary_app.data.model.ChatMessage;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRepository {
    private FirebaseFirestore db;

    public ChatRepository(){
        db = FirebaseFirestore.getInstance();
    }

    // 1. Gửi tin nhắn
    // Vừa lưu tin nhắn vào subcollection, vừa update phòng chat ở ngoài
    public Task<Void> sendMessage(String chatId, ChatMessage message, List<String> participants){
        WriteBatch batch = db.batch();

        DocumentReference messageRef = db.collection("chats")
                .document(chatId)
                .collection("messages")
                .document();

        message.setMessageId(messageRef.getId());
        batch.set(messageRef, message);

        DocumentReference chatRoomRef = db.collection("chats").document(chatId);
        Map<String, Object> roomUpdates = new HashMap<>();
        roomUpdates.put("lastMessage", message.getContent());
        roomUpdates.put("lastUpdated", message.getCreatedAt());
        roomUpdates.put("participants", participants);

        // Dùng SetOptions.merge() vì nếu phòng chat này CHƯA TỒN TẠI (nhắn lần đầu),
        // nó sẽ tự động tạo mới phòng chat luôn. Nếu có rồi thì nó chỉ update những trường ở trên.
        batch.set(chatRoomRef, roomUpdates, SetOptions.merge());

        return batch.commit();
    }

    /**
     * 2. Lắng nghe TẤT CẢ các phòng chat của mình (Màn hình Danh sách tin nhắn ngoài cùng)
     * Lưu ý: Trả về ListenerRegistration (bên ViewModel) để HỦY lắng nghe khi thoát app, tránh tốn RAM
     */
    public void listenToMyChatRoom(String myUid, EventListener<QuerySnapshot> listener){
        db.collection("chats")
                .whereArrayContains("participants", myUid)
                .orderBy("lastUpdated", Query.Direction.DESCENDING)
                .addSnapshotListener(listener);
    }

    // 3. Lắng nghe tin nhắn TRONG 1 PHÒNG CHAT CỤ THỂ
    public void listenToMessageInRoom(String chatId, EventListener<QuerySnapshot> listener){
        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener(listener);
    }
}
