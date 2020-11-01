package com.com.technoparkproject.view.activities;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.com.technoparkproject.R;
import com.com.technoparkproject.view.fragments.HomeFragment;
import com.com.technoparkproject.view.fragments.PersonalPageFragment;
import com.com.technoparkproject.view.fragments.PlaylistFragment;
import com.com.technoparkproject.view.fragments.RecordFragment;
import com.com.technoparkproject.view.fragments.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private View viewContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new HomeFragment()).commit();

        Toolbar toolbar = findViewById(R.id.toolbar);

        TextView txtView = findViewById(R.id.toolbar_title);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "font/Roboto-Medium.ttf");
        txtView.setTypeface(typeface);
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navigationListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;

            switch (item.getItemId()) {
                case R.id.nav_home:
                    selectedFragment = new HomeFragment();
                    break;
                case R.id.nav_playlist:
                    selectedFragment = new PlaylistFragment();
                    break;
                case R.id.nav_record:
                    selectedFragment = new RecordFragment();
                    break;
                case R.id.nav_settings:
                    selectedFragment = new SettingsFragment();
                    break;
                case R.id.nav_personal_page:
                    selectedFragment = new PersonalPageFragment();
                    break;
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    selectedFragment).commit();

            return true;
        }
    };
}
