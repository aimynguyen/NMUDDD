package com.example.diary_app.repository;

import com.example.diary_app.data.model.ChatMessage;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
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

        // Trỏ tới sub-collection "messages" bên trong document của phòng chat đó
        DocumentReference messageRef = db.collection("chats")
                .document(chatId)
                .collection("messages")
                .document();

        message.setMessageId(messageRef.getId());
        batch.set(messageRef, message);

        // Trỏ tới chính document phòng chat để cập nhật thông tin hiển thị ở màn hình Dashboard ngoài cùng
        DocumentReference chatRoomRef = db.collection("chats").document(chatId);

        Map<String, Object> roomUpdates = new HashMap<>();
        roomUpdates.put("lastMessage", message.getContent());

        // SỬ DỤNG serverTimestamp để đảm bảo thời gian đồng bộ tuyệt đối trên hệ thống Firebase,
        // giúp tính năng "nhảy phòng chat lên đầu" hoạt động chính xác không sợ lệch giờ thiết bị.
        roomUpdates.put("lastUpdated", FieldValue.serverTimestamp());

        // THÊM TRƯỜNG NÀY: Để Fragment biết ai là người nhắn cuối cùng -> Hiển thị "Bạn: ..." hoặc "Tên bạn bè: ..."
        roomUpdates.put("lastSenderId", message.getSenderId());

        if (participants != null && !participants.isEmpty()) {
            roomUpdates.put("participants", participants);
        }

        // Dùng SetOptions.merge() vì nếu phòng chat này CHƯA TỒN TẠI (nhắn lần đầu),
        // nó sẽ tự động tạo mới phòng chat luôn. Nếu có rồi thì nó chỉ update những trường ở trên.
        batch.set(chatRoomRef, roomUpdates, SetOptions.merge());

        return batch.commit();
    }

    /**
     * 2. Lắng nghe TẤT CẢ các phòng chat của mình (Màn hình Danh sách tin nhắn ngoài cùng)
     * Lưu ý: Trả về ListenerRegistration (bên ViewModel) để HỦY lắng nghe khi thoát app, tránh tốn RAM
     */
    public ListenerRegistration listenToMyChatRoom(String myUid, EventListener<QuerySnapshot> listener){
        return db.collection("chats")
                .whereArrayContains("participants", myUid)
                .orderBy("lastUpdated", Query.Direction.DESCENDING) // Firebase tự xếp phòng mới lên đầu
                .addSnapshotListener(listener);
    }

    // 3. Lắng nghe tin nhắn TRONG 1 PHÒNG CHAT CỤ THỂ
    public ListenerRegistration listenToMessageInRoom(String chatId, EventListener<QuerySnapshot> listener){
        return db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener(listener);
    }
}