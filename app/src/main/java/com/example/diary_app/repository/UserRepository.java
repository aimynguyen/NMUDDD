package com.example.diary_app.repository;

import com.example.diary_app.data.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public Task<List<User>> getPendingFriendRequestsAsUsers(String userId) {
        return db.collection("friend_requests")
                .whereEqualTo("receiverId", userId)
                .whereEqualTo("status", "pending")
                .get()
                .continueWithTask(task -> {
                    QuerySnapshot querySnapshot = task.getResult();
                    List<String> senderIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String senderId = doc.getString("senderId");
                        if (senderId != null) senderIds.add(senderId);
                    }
                    return getUsersByIds(senderIds);
                });
    }

    public Task<List<User>> getUsersByIds(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Tasks.forResult(new ArrayList<>());
        }

        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String id : userIds) {
            tasks.add(db.collection("users").document(id).get());
        }

        return Tasks.whenAllSuccess(tasks).continueWith(task -> {
            List<User> users = new ArrayList<>();
            for (Object obj : task.getResult()) {
                DocumentSnapshot doc = (DocumentSnapshot) obj;
                if (doc.exists()) {
                    User user = doc.toObject(User.class);
                    if (user != null) {
                        users.add(user);
                    }
                }
            }
            return users;
        });
    }

    // Cập nhật thông tin cá nhân
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

    // CHẤP NHẬN kết bạn
    public Task<Void> acceptFriendRequestBySender(String myUid, String senderUid) {
        return db.collection("friend_requests")
                .whereEqualTo("senderId", senderUid)
                .whereEqualTo("receiverId", myUid)
                .whereEqualTo("status", "pending")
                .get()
                .continueWithTask(task -> {
                    QuerySnapshot qs = task.getResult();
                    if (!qs.isEmpty()) {
                        String requestId = qs.getDocuments().get(0).getId();
                        return acceptFriendRequest(requestId, myUid, senderUid);
                    }
                    return Tasks.forResult(null);
                });
    }

    public Task<Void> acceptFriendRequest(String requestId, String myUid, String senderUid){
        WriteBatch batch = db.batch();
        DocumentReference requestRef = db.collection("friend_requests").document(requestId);
        batch.update(requestRef, "status", "accepted");

        DocumentReference myRef = db.collection("users").document(myUid);
        batch.update(myRef, "friendIds", FieldValue.arrayUnion(senderUid));

        DocumentReference senderRef = db.collection("users").document(senderUid);
        batch.update(senderRef, "friendIds", FieldValue.arrayUnion(myUid));
        return batch.commit();
    }

    // 8. Từ chối kết bạn / Hủy kết bạn
    public Task<Void> deleteFriendRequestBySender(String myUid, String senderUid) {
        return db.collection("friend_requests")
                .whereEqualTo("senderId", senderUid)
                .whereEqualTo("receiverId", myUid)
                .whereEqualTo("status", "pending")
                .get()
                .continueWithTask(task -> {
                    QuerySnapshot qs = task.getResult();
                    if (!qs.isEmpty()) {
                        return qs.getDocuments().get(0).getReference().delete();
                    }
                    return Tasks.forResult(null);
                });
    }

    public Task<Void> deleteFriendRequest(String requestId) {
        return db.collection("friend_requests").document(requestId).delete();
    }
}
