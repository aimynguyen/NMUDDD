package com.example.diary_app.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.Tasks;

public class AuthRepository {
    private FirebaseAuth mAuth;

    public AuthRepository() {
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * 1. Đăng nhập bằng email và password
     * Dungf Task để trả kết quả về cho ViewModel xử lý (success/ fail)
     */
    public Task<AuthResult> login(String email, String password){
        return mAuth.signInWithEmailAndPassword(email, password);
    }

    // 2. Đăng ký bằng email và password
    public Task<AuthResult> register(String email, String password){
        return mAuth.createUserWithEmailAndPassword(email, password);
    }


    // 3. Gửi email xác nhận
    public Task<Void> sendEmailVerification(){
        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null)
            return user.sendEmailVerification();
        else
            return com.google.android.gms.tasks.Tasks.forException(new Exception("No user logged in"));
        // return task thất bại nêu chưa có user nào đăng nhập
    }

    // 4. Kiểm tra xem Email đã được xác nhận chưa
    public boolean isEmailVerified(){
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            user.reload();
            return user.isEmailVerified();
        }
        return false;
    }

    // 5. Lấy ID của user đang đăng nhập hiện tại
    public String getCurrentUserId() {
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null)
            return user.getUid();
        return null;
    }

    // 6. Đăng xuất
    public void logout(){
        mAuth.signOut();
    }

    // 7. Quên mật khẩu (Gửi email reset password)
    public Task<Void> resetPassword(String email){
       return mAuth.sendPasswordResetEmail(email);
    }

    // 8. Đổi mật khẩu
    public Task<Void> changePassword(String currentPassword, String newPassword) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            return user.reauthenticate(credential).continueWithTask(task -> {
                if (task.isSuccessful()) {
                    return user.updatePassword(newPassword);
                } else {
                    throw task.getException() != null ? task.getException() : new Exception("Re-authentication failed");
                }
            });
        }
        return Tasks.forException(new Exception("No user logged in"));
    }
}
