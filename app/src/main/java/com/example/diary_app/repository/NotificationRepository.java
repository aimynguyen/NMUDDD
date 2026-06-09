package com.example.diary_app.repository;

import com.example.diary_app.core.NotiType;
import com.example.diary_app.data.model.Notification;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class NotificationRepository {
    private FirebaseFirestore db;

    public NotificationRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Hàm này dùng để BẮN THÔNG BÁO.
     * Gọi hàm này khi Admin xóa bài, hoặc khi User bấm Like
     */
    public Task<Void> sendNotification(String receiverId, String senderId, NotiType type, String targetId, String message)
    {
        DocumentReference newNotifRef = db.collection("notifications").document();

        // Tạo object Notification
        Notification notification = new Notification(receiverId, senderId, type, targetId, message);
        notification.setNotificationId(newNotifRef.getId()); // Gán ID vừa tạo vào object

        // Lưu lên Firestore
        return newNotifRef.set(notification);
    }

    /**
     * Hàm này dùng ở màn hình "Cái chuông", tải danh sách thông báo của User đang đăng nhập.
     */
    public Task<QuerySnapshot> getNotificationsForUser(String userId){
        return db.collection("notifications")
                .whereEqualTo("receiverId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                        .get();
    }

    /**
     * Hàm đánh dấu đã đọc khi User click vào thông báo
     */
    public Task<Void> markAsRead(String notificationId) {
        return db.collection("notifications")
                .document(notificationId)
                .update("isRead", true);
    }
}
