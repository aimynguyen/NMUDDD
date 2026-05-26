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

        // 1. Ánh xạ các View cần ẩn/hiện
        View headerView = findViewById(R.id.header_view);
        BottomNavigationView bottomNavigationView = findViewById(R.id.navbar_view);

        // 2. Lấy NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNavigationView, navController);

            // 3. TỰ ĐỘNG ẨN/HIỆN HEADER VÀ NAVBAR KHI CHUYỂN TRANG
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int id = destination.getId();

                // Cả Header cả Navbar
                if (id == R.id.nav_home || id == R.id.nav_dashboard || id == R.id.nav_profile ) {
                    headerView.setVisibility(View.VISIBLE);
                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
                //Chỉ Nav
                else if (id == R.id.nav_search) {
                    headerView.setVisibility(View.GONE); // GONE là ẩn hoàn toàn và co kích thước lại
                    bottomNavigationView.setVisibility(View.GONE);
                }
                //chỉ Header
                else{
                    headerView.setVisibility(View.VISIBLE);
                    bottomNavigationView.setVisibility(View.GONE);
                }
            });
        }
    }
}