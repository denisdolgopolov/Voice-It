package com.com.technoparkproject.view.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {
    private String currentFragment;

    private static final int TOOLBAR_HOME_TEXT = R.string.toolbar_home_text;
    private static final int TOOLBAR_PLAYLIST_TEXT = R.string.toolbar_playlist_text;
    private static final int TOOLBAR_RECORD_TEXT = R.string.toolbar_record_text;
    private static final int TOOLBAR_SETTINGS_TEXT = R.string.toolbar_settings_text;
    private static final int TOOLBAR_PERSONAL_PAGE_TEXT = R.string.toolbar_personal_page_text;

    private static final int TOOLBAR_HOME_NAME = R.string.toolbar_home_name;
    private static final int TOOLBAR_PLAYLIST_NAME = R.string.toolbar_playlist_name;
    private static final int TOOLBAR_RECORD_NAME = R.string.toolbar_record_name;
    private static final int TOOLBAR_SETTINGS_NAME = R.string.toolbar_settings_name;
    private static final int TOOLBAR_PERSONAL_PAGE_NAME = R.string.toolbar_personal_page_name;


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
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);
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

            toolbarBackButton.setVisibility(View.GONE);
            toolbarLogoutButton.setVisibility(View.GONE);
            toolbarCancelButton.setVisibility(View.GONE);
            toolbarTickButton.setVisibility(View.GONE);

            switch (item.getItemId()) {
                case R.id.nav_home:
                    selectedFragment = new HomeFragment();
                    nameSelectedFragment = getResources().getString(TOOLBAR_HOME_NAME);
                    toolbarTitleView.setText(TOOLBAR_HOME_TEXT);
                    break;
                case R.id.nav_playlist:
                    selectedFragment = new PlaylistFragment();
                    nameSelectedFragment = getResources().getString(TOOLBAR_PLAYLIST_NAME);
                    toolbarTitleView.setText(TOOLBAR_PLAYLIST_TEXT);
                    break;
                case R.id.nav_record:
                    selectedFragment = new RecordFragment();
                    nameSelectedFragment = getResources().getString(TOOLBAR_RECORD_NAME);
                    toolbarTitleView.setText(TOOLBAR_RECORD_TEXT);
                    toolbarBackButton.setVisibility(View.VISIBLE);
                    break;
                case R.id.nav_settings:
                    selectedFragment = new SettingsFragment();
                    nameSelectedFragment = getResources().getString(TOOLBAR_SETTINGS_NAME);
                    toolbarTitleView.setText(TOOLBAR_SETTINGS_TEXT);
                    toolbarCancelButton.setVisibility(View.VISIBLE);
                    toolbarTickButton.setVisibility(View.VISIBLE);
                    break;
                case R.id.nav_personal_page:
                    selectedFragment = new PersonalPageFragment();
                    nameSelectedFragment = getResources().getString(TOOLBAR_PERSONAL_PAGE_NAME);
                    toolbarTitleView.setText(TOOLBAR_PERSONAL_PAGE_TEXT);
                    toolbarLogoutButton.setVisibility(View.VISIBLE);
                    break;
            }

            if (currentFragment == null || !currentFragment.equals(nameSelectedFragment)) {
                assert selectedFragment != null;
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        selectedFragment).commit();
                currentFragment = nameSelectedFragment;
            }
            return true;
        }
    };
}
