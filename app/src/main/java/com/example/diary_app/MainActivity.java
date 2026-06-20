package com.example.diary_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Ánh xạ View
        View headerView = findViewById(R.id.header_view);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        if(headerView != null)
            tvName = headerView.findViewById(R.id.header_user_name);

        updateHeaderFromStorage();

        // 2. Lấy NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null && bottomNavigationView != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNavigationView, navController);

            // 3. TỰ ĐỘNG ẨN/HIỆN THEO ID FRAGMENT
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int id = destination.getId();

                // Các màn hình chính: Hiện cả Header và Bottom Navigation
                if (id == R.id.nav_dashboard || id == R.id.nav_profile || id == R.id.nav_home) {
                    if (headerView != null) headerView.setVisibility(View.VISIBLE);
                    bottomNavigationView.setVisibility(View.VISIBLE);

                    updateHeaderFromStorage();
                }
                // Màn hình Chat AI: Ẩn Header cho rộng chỗ, vẫn hiện Bottom Navigation
                else if (id == R.id.nav_chatAI || id == R.id.nav_chat) {
                    if (headerView != null) headerView.setVisibility(View.GONE);
                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
                // Các màn hình phụ (Login, Signup, Edit Profile): Ẩn toàn bộ thanh điều hướng
                else {
                    if (headerView != null) headerView.setVisibility(View.GONE);
                    bottomNavigationView.setVisibility(View.GONE);
                }
            });
        }
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
    private void listenForNotifications() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("notifications")
                .whereEqualTo("toUid", currentUserId)
                .whereEqualTo("isRead", false) // Chỉ lấy thông báo chưa đọc
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null || snapshots.isEmpty()) return;

                    for (QueryDocumentSnapshot doc : snapshots) {
                        String title = doc.getString("title");
                        String body = doc.getString("body");

                        // Hiển thị một Toast hoặc một Dialog thông báo cho User biết
                        Toast.makeText(MainActivity.this, title + ": " + body, Toast.LENGTH_LONG).show();

                        // Đánh dấu thông báo này là đã đọc/đã xử lý để lần sau không hiện lại nữa
                        doc.getReference().update("isRead", true);
                    }
                });
    }
}