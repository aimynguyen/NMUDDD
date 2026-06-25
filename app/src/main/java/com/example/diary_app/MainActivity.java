package com.example.diary_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.widget.PopupMenu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MainActivity extends AppCompatActivity {

    public TextView tvName;
    private ImageView ivAvatar;
    private NavController navController;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Ánh xạ View
        View headerView = findViewById(R.id.header_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        if(headerView != null) {
            tvName = headerView.findViewById(R.id.header_user_name);
            ivAvatar = headerView.findViewById(R.id.header_avatar);
            if (ivAvatar != null) {
                ivAvatar.setOnClickListener(this::showAvatarMenu);
            }
            View notiIcon = headerView.findViewById(R.id.header_noti);
            if (notiIcon != null) {
                notiIcon.setOnClickListener(v -> {
                    if (navController != null) {
                        navController.navigate(R.id.nav_notification);
                    }
                });
            }
        }

        updateHeaderFromStorage();

        // KÍCH HOẠT LẮNG NGHE THÔNG BÁO
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

                // Các màn hình chính (bao gồm cả Admin): Hiện cả Header và Bottom Navigation
                if (id == R.id.nav_dashboard || id == R.id.nav_profile || id == R.id.nav_home 
                    || id == R.id.nav_chatroom || id == R.id.nav_search || id == R.id.nav_pet
                    || id == R.id.nav_addfriend || id == R.id.nav_admin) {
                    if (headerView != null) headerView.setVisibility(View.VISIBLE);
                    if (bottomNavigationView != null) bottomNavigationView.setVisibility(View.VISIBLE);

                    updateHeaderFromStorage();
                }
                // Màn hình Chat: Ẩn Header, hiện Bottom Navigation
                else if (id == R.id.nav_chatAI || id == R.id.nav_chat) {
                    if (headerView != null) headerView.setVisibility(View.GONE);
                    if (bottomNavigationView != null) bottomNavigationView.setVisibility(View.VISIBLE);
                }
                // Các màn hình khác: Ẩn toàn bộ
                else {
                    if (headerView != null) headerView.setVisibility(View.GONE);
                    if (bottomNavigationView != null) bottomNavigationView.setVisibility(View.GONE);
                }
            });
        }
    }

    private void showAvatarMenu(View v) {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(this, R.style.CustomPopupMenu);
        PopupMenu popupMenu = new PopupMenu(wrapper, v);
        popupMenu.getMenuInflater().inflate(R.menu.header_avatar_menu, popupMenu.getMenu());

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
                if (navController != null) navController.navigate(R.id.nav_profile);
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
        FirebaseAuth.getInstance().signOut();
        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        sharedPref.edit().remove("USER_ID").apply();

        if (navController != null) {
            navController.navigate(R.id.nav_login);
        }
        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
    }

    public void saveUserIdAndFetchName(String userId) {
        if (userId == null || userId.isEmpty()) return;
        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        sharedPref.edit().putString("USER_ID", userId).apply();
        fetchAndDisplayUserName(userId);
    }

    private void updateHeaderFromStorage() {
        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String savedId = sharedPref.getString("USER_ID", "");
        fetchAndDisplayUserName(savedId);
    }

    private void fetchAndDisplayUserName(String userId) {
        if (userId == null || userId.isEmpty()) {
            if (tvName != null) tvName.setText("Guest!");
            if (ivAvatar != null) ivAvatar.setImageResource(R.drawable.human_human);
            updateBottomMenu(null);
            return;
        }

        FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists()) {
                            String fullName = doc.getString("userName");
                            String avatarUrl = doc.getString("avatarUrl");
                            String role = doc.getString("role");
                            
                            if (tvName != null) tvName.setText(fullName);
                            if (ivAvatar != null) {
                                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                    Glide.with(MainActivity.this).load(avatarUrl).placeholder(R.drawable.human_human).circleCrop().into(ivAvatar);
                                } else {
                                    ivAvatar.setImageResource(R.drawable.human_human);
                                }
                            }
                            
                            updateBottomMenu(role);
                        }
                    }
                });
    }

    private void updateBottomMenu(String role) {
        if (bottomNavigationView == null) return;
        Menu menu = bottomNavigationView.getMenu();
        MenuItem adminItem = menu.findItem(R.id.nav_admin);
        if (adminItem != null) {
            adminItem.setVisible("admin".equals(role));
        }
    }

    private void listenForNotifications() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("notifications")
                .whereEqualTo("toUid", currentUserId)
                .whereEqualTo("isRead", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null || snapshots.isEmpty()) return;
                    for (QueryDocumentSnapshot doc : snapshots) {
                        String title = doc.getString("title");
                        String body = doc.getString("body");
                        Toast.makeText(MainActivity.this, title + ": " + body, Toast.LENGTH_LONG).show();
                        doc.getReference().update("isRead", true);
                    }
                });
    }
}
