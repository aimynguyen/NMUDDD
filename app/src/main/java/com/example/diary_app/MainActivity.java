package com.example.diary_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper; // Nhớ thêm import này
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MainActivity extends AppCompatActivity {

    public TextView tvName;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Ánh xạ View
        View headerView = findViewById(R.id.header_view);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        if(headerView != null) {
            tvName = headerView.findViewById(R.id.header_user_name);
            View avatar = headerView.findViewById(R.id.header_avatar);
            if (avatar != null) {
                avatar.setOnClickListener(this::showAvatarMenu);
            }
        }

        updateHeaderFromStorage();

        // KÍCH HOẠT LẮNG NGHE THÔNG BÁO: Kiểm tra nếu đã đăng nhập thì bắt đầu lắng nghe lời mời kết bạn
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            listenForNotifications();
        }

        // 2. Lấy NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            if (bottomNavigationView != null) {
                NavigationUI.setupWithNavController(bottomNavigationView, navController);
            }

            // 3. TỰ ĐỘNG ẨN/HIỆN THEO ID FRAGMENT
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int id = destination.getId();

                // Các màn hình chính: Hiện cả Header và Bottom Navigation
                if (id == R.id.nav_dashboard || id == R.id.nav_profile || id == R.id.nav_home || id == R.id.nav_chatroom || id == R.id.nav_search || id == R.id.nav_pet) {
                    if (headerView != null) headerView.setVisibility(View.VISIBLE);
                    if (bottomNavigationView != null) bottomNavigationView.setVisibility(View.VISIBLE);

                    updateHeaderFromStorage();
                }
                // Màn hình Chat AI: Ẩn Header cho rộng chỗ, vẫn hiện Bottom Navigation
                else if (id == R.id.nav_chatAI || id == R.id.nav_chat) {
                    if (headerView != null) headerView.setVisibility(View.GONE);
                    if (bottomNavigationView != null) bottomNavigationView.setVisibility(View.VISIBLE);
                }
                // Các màn hình phụ (Login, Signup, Edit Profile): Ẩn toàn bộ thanh điều hướng
                else {
                    if (headerView != null) headerView.setVisibility(View.GONE);
                    if (bottomNavigationView != null) bottomNavigationView.setVisibility(View.GONE);
                }
            });
        }
    }

    // ĐÃ CẬP NHẬT: Hiện icon và bo góc cho Menu Avatar
    private void showAvatarMenu(View v) {
        // 1. Áp dụng style custom bo góc thông qua ContextThemeWrapper
        ContextThemeWrapper wrapper = new ContextThemeWrapper(this, R.style.CustomPopupMenu);
        PopupMenu popupMenu = new PopupMenu(wrapper, v);

        popupMenu.getMenuInflater().inflate(R.menu.header_avatar_menu, popupMenu.getMenu());

        // 2. Ép hiển thị cả Icon ra menu
        try {
            java.lang.reflect.Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    java.lang.reflect.Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_profile) {
                if (navController != null) {
                    navController.navigate(R.id.nav_profile);
                }
                return true;
            } else if (id == R.id.menu_logout) {
                performLogout();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void performLogout() {
        // 1. Đăng xuất Firebase
        FirebaseAuth.getInstance().signOut();

        // 2. Xóa SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("USER_ID");
        editor.apply();

        // 3. Điều hướng về màn hình Login
        if (navController != null) {
            navController.navigate(R.id.nav_login);
        }
        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
    }

    public void saveUserIdAndFetchName(String userId) {
        if (userId == null || userId.isEmpty()) return;

        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("USER_ID", userId);
        editor.apply();

        fetchAndDisplayUserName(userId);
    }

    private void updateHeaderFromStorage() {
        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String savedId = sharedPref.getString("USER_ID", "");

        fetchAndDisplayUserName(savedId);
    }

    private void fetchAndDisplayUserName(String userId) {
        if (userId == null || userId.isEmpty()) {
            if (tvName != null) {
                tvName.setText("Guest!");
            }
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists()) {
                            String fullName = doc.getString("userName"); // lấy field userName
                            if (tvName != null) tvName.setText(fullName);
                        }
                    }
                });
    }

    // ĐÃ CẬP NHẬT: Thêm điều kiện check null an toàn khi chạy
    private void listenForNotifications() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("notifications")
                .whereEqualTo("toUid", currentUserId)
                .whereEqualTo("isRead", false) // Chỉ lấy thông báo chưa đọc
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null || snapshots.isEmpty()) return;

                    for (QueryDocumentSnapshot doc : snapshots) {
                        String title = doc.getString("title");
                        String body = doc.getString("body");

                        // Hiển thị một Toast thông báo cho User biết
                        Toast.makeText(MainActivity.this, title + ": " + body, Toast.LENGTH_LONG).show();

                        // Đánh dấu thông báo này là đã đọc/đã xử lý để lần sau không hiện lại nữa
                        doc.getReference().update("isRead", true);
                    }
                });
    }
}