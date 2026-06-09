package com.example.diary_app.repository;

import com.example.diary_app.data.model.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private FirebaseFirestore db;

    public UserRepository(){
        db = FirebaseFirestore.getInstance();
    }

    /**
     * 1. Lưu thông tin User mới vào bảng "users"
     * (Gọi hàm này ngay sau khi Đăng ký và Xác minh email thành công)
     */
    public Task<Void> createUserProfile(User user){
        return db.collection("users").document(user.getUid()).set(user);
    }

    // 2. Lấy thông tin Profile của một User bất kỳ dựa vào UID
    public Task<DocumentSnapshot> getUserProfile(String uid){
        return db.collection("users").document(uid).get();
    }

    // 3. Tìm kiếm người dùng (Tìm theo Email)
    public Task<QuerySnapshot> searchUserByEmail(String email){
        return db.collection("users")
                .whereEqualTo("email", email).get();
    }

    // 4. Cập nhật thông tin cá nhân
    public Task<Void> updateUserProfile(String uid, Map<String, Object> updates) {
        return db.collection("users").document(uid).update(updates);
    }

    public Task<Void> updateUserField(String uid, String field, Object value){
        return db.collection("users").document(uid).update(field, value);
    }

    // 5. Gửi lời mời kết bạn
    public Task<DocumentReference> sendFriendRequest(String myUid, String targetUid){
        Map<String, Object> request = new HashMap<>();
        request.put("senderId", myUid);
        request.put("receiverId", targetUid);
        request.put("status", "pending");
        request.put("createAt", Timestamp.now());
        return db.collection("friend_requests").add(request);
    }

    // 6. Lấy danh sách Lời mời kết bạn
    public Task<QuerySnapshot> getPendingRequest(String myUid){
        return db.collection("friend_request")
                .whereEqualTo("receiverId", myUid)
                .whereEqualTo("status", "pending")
                .orderBy("createAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get();
    }

    // 7. CHẤP NHẬN kết bạn
    public Task<Void> acceptFriendRequest(String requestId, String myUid, String senderUid){
        WriteBatch batch = db.batch();

        DocumentReference requestRef = db.collection("friend_request").document(requestId);
        batch.update(requestRef, "status", "accepted");

        DocumentReference myRef = db.collection("users").document(myUid);
        batch.update(myRef, "friendIds", FieldValue.arrayUnion(senderUid));

        DocumentReference senderRef = db.collection("users").document(senderUid);
        batch.update(senderRef, "friendIds", FieldValue.arrayUnion(myUid));
        return batch.commit();
    }

    // 8. Từ chối kết bạn / Hủy kết bạn
    public Task<Void> deleteFriendRequest(String requestId){
        return db.collection("friend_request")
                .document(requestId)
                .delete();
    }
}
