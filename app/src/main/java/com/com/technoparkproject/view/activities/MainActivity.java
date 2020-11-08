package com.com.technoparkproject.view.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
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

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private String currentFragment = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.currentFragment = null;
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationListener);
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navigationListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;
            String nameSelectedFragment = null;
            TextView toolbarTitleView = findViewById(R.id.toolbar_title);
            ImageButton toolbarBackButton = findViewById(R.id.toolbar_back_button);
            ImageButton toolbarLogoutButton = findViewById(R.id.toolbar_logout_button);
            ImageButton toolbarCancelButton = findViewById(R.id.toolbar_cancel_button);
            ImageButton toolbarTickButton = findViewById(R.id.toolbar_tick_button);

            switch (item.getItemId()) {
                case R.id.nav_home:
                    selectedFragment = new HomeFragment();
                    nameSelectedFragment = "home";
                    toolbarTitleView.setText("Лента");
                    toolbarBackButton.setVisibility(View.GONE);
                    toolbarLogoutButton.setVisibility(View.GONE);
                    toolbarCancelButton.setVisibility(View.GONE);
                    toolbarTickButton.setVisibility(View.GONE);
                    break;
                case R.id.nav_playlist:
                    selectedFragment = new PlaylistFragment();
                    nameSelectedFragment = "playlist";
                    toolbarTitleView.setText("Плейлист");
                    toolbarBackButton.setVisibility(View.GONE);
                    toolbarLogoutButton.setVisibility(View.GONE);
                    toolbarCancelButton.setVisibility(View.GONE);
                    toolbarTickButton.setVisibility(View.GONE);
                    break;
                case R.id.nav_record:
                    selectedFragment = new RecordFragment();
                    nameSelectedFragment = "record";
                    toolbarTitleView.setText("Запись");
                    toolbarBackButton.setVisibility(View.VISIBLE);
                    toolbarLogoutButton.setVisibility(View.VISIBLE);
                    toolbarCancelButton.setVisibility(View.GONE);
                    toolbarTickButton.setVisibility(View.GONE);
                    break;
                case R.id.nav_settings:
                    selectedFragment = new SettingsFragment();
                    nameSelectedFragment = "settings";
                    toolbarTitleView.setText("Настройки");
                    toolbarBackButton.setVisibility(View.GONE);
                    toolbarLogoutButton.setVisibility(View.GONE);
                    toolbarCancelButton.setVisibility(View.VISIBLE);
                    toolbarTickButton.setVisibility(View.VISIBLE);
                    break;
                case R.id.nav_personal_page:
                    selectedFragment = new PersonalPageFragment();
                    nameSelectedFragment = "personal_page";
                    toolbarTitleView.setText("User Name");
                    toolbarBackButton.setVisibility(View.GONE);
                    toolbarLogoutButton.setVisibility(View.VISIBLE);
                    toolbarCancelButton.setVisibility(View.GONE);
                    toolbarTickButton.setVisibility(View.GONE);

                    break;
            }

            if (currentFragment != nameSelectedFragment) {
                assert selectedFragment != null;
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        selectedFragment).commit();
                currentFragment = nameSelectedFragment;
            }
            return true;
        }
    };
}
