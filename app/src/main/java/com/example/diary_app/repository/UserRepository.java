package com.example.diary_app.repository;

import com.example.diary_app.data.model.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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
    public Task<Void> updateUserField(String uid, String field, Object value){
        return db.collection("users").document(uid).update(field, value);

    }
}
