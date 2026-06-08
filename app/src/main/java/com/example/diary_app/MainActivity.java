package com.example.diary_app;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Ánh xạ View
        View headerView = findViewById(R.id.header_view);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

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
}
