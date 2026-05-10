package com.example.agri_app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.agri_app.fragments.AnalyticsFragment;
import com.example.agri_app.fragments.ControlFragment;
import com.example.agri_app.fragments.DashboardFragment;
import com.example.agri_app.fragments.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private final DashboardFragment dashboardFragment = new DashboardFragment();
    private final ControlFragment controlFragment = new ControlFragment();
    private final AnalyticsFragment analyticsFragment = new AnalyticsFragment();
    private final SettingsFragment settingsFragment = new SettingsFragment();
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);

        // Add all fragments but hide non-default ones
        activeFragment = dashboardFragment;
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentContainer, settingsFragment, "settings").hide(settingsFragment)
                .add(R.id.fragmentContainer, analyticsFragment, "analytics").hide(analyticsFragment)
                .add(R.id.fragmentContainer, controlFragment, "controls").hide(controlFragment)
                .add(R.id.fragmentContainer, dashboardFragment, "dashboard")
                .commit();

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {
                selectedFragment = dashboardFragment;
            } else if (itemId == R.id.nav_controls) {
                selectedFragment = controlFragment;
            } else if (itemId == R.id.nav_analytics) {
                selectedFragment = analyticsFragment;
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = settingsFragment;
            } else {
                return false;
            }

            getSupportFragmentManager().beginTransaction()
                    .hide(activeFragment)
                    .show(selectedFragment)
                    .commit();
            activeFragment = selectedFragment;
            return true;
        });
    }
}