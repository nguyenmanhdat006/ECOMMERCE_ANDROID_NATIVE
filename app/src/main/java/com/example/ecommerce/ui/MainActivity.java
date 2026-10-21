package com.example.ecommerce.ui;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.ecommerce.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * C4 - Khung chính: một Activity duy nhất chứa NavHostFragment (các màn hình là Fragment)
 * và thanh điều hướng dưới. Thanh dưới chỉ hiện ở 4 tab chính, ẩn ở các màn hình con (chi tiết...).
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Chừa chỗ cho thanh trạng thái và thanh điều hướng của hệ thống
        View root = findViewById(R.id.root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host);
        NavController navController = navHost.getNavController();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();
            boolean isTab = id == R.id.homeFragment || id == R.id.cartFragment
                    || id == R.id.ordersFragment || id == R.id.accountFragment;
            bottomNav.setVisibility(isTab ? View.VISIBLE : View.GONE);
        });
    }
}
