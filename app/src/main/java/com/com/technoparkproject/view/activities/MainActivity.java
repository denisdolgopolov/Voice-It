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
import androidx.fragment.app.FragmentManager;

import com.com.technoparkproject.R;
import com.com.technoparkproject.view.fragments.LanguageFragment;
import com.com.technoparkproject.view.fragments.PasswordFragment;
import com.com.technoparkproject.view.fragments.HomeFragment;
import com.com.technoparkproject.view.fragments.MainListOfRecordsFragment;
import com.com.technoparkproject.view.fragments.PersonalPageFragment;
import com.com.technoparkproject.view.fragments.RecordFragment;
import com.com.technoparkproject.view.fragments.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private String currentFragment = null;

    private TextView toolbarTitleView;
    private ImageButton toolbarBackButton;
    private ImageButton toolbarLogoutButton;
    private ImageButton toolbarCancelButton;
    private ImageButton toolbarTickButton;

    private static final int TOOLBAR_HOME_TEXT = R.string.toolbar_home_text;
    private static final int TOOLBAR_PLAYLIST_TEXT = R.string.toolbar_playlist_text;
    private static final int TOOLBAR_RECORD_TEXT = R.string.toolbar_record_text;
    private static final int TOOLBAR_SETTINGS_TEXT = R.string.toolbar_settings_text;
    private static final int TOOLBAR_PERSONAL_PAGE_TEXT = R.string.toolbar_personal_page_text;
    private static final int TOOLBAR_PASSWORD_TEXT = R.string.toolbar_password_text;
    private static final int TOOLBAR_LANGUAGE_TEXT = R.string.toolbar_language_text;

    private static final int FRAGMENT_HOME_NAME = R.string.fragment_home_name;
    private static final int FRAGMENT_PLAYLIST_NAME = R.string.fragment_playlist_name;
    private static final int FRAGMENT_RECORD_NAME = R.string.fragment_record_name;
    private static final int FRAGMENT_SETTINGS_NAME = R.string.fragment_settings_name;
    private static final int FRAGMENT_PERSONAL_PAGE_NAME = R.string.fragment_personal_page_name;
    private static final int FRAGMENT_PASSWORD_NAME = R.string.fragment_password_name;
    private static final int FRAGMENT_LANGUAGE_NAME = R.string.fragment_language_name;

    private SettingsFragment settingsFragment;
    private PasswordFragment changePasswordFragment;
    private LanguageFragment changeLanguageFragment;

    private FragmentManager manager;

    private static final String CURRENT_FRAGMENT = "Current fragment";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationListener);

        manager = getSupportFragmentManager();

        if (savedInstanceState != null) {
            currentFragment = savedInstanceState.getString(CURRENT_FRAGMENT);
        }

        if (currentFragment == null) {
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        } else {
            setToolbar(currentFragment);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    public void onClickChangePasswordFragment(View view) {
        if (view.getId() == R.id.btn_change_password) {
            changePasswordFragment = new PasswordFragment();
            currentFragment = getString(FRAGMENT_PASSWORD_NAME);
            manager.beginTransaction().replace(R.id.fragment_container, changePasswordFragment).addToBackStack(currentFragment).commit();
            setToolbar(currentFragment);
        }
    }

    public void onClickChangeLanguageFragment(View view) {
        if (view.getId() == R.id.btn_change_language) {
            changeLanguageFragment = new LanguageFragment();
            currentFragment = getString(FRAGMENT_LANGUAGE_NAME);
            manager.beginTransaction().replace(R.id.fragment_container, changeLanguageFragment).addToBackStack(currentFragment).commit();
            setToolbar(currentFragment);
        }
    }

    public void onClickCancelButton(View view) {
        if (currentFragment.equals(getString(FRAGMENT_PASSWORD_NAME)) || currentFragment.equals(getString(FRAGMENT_LANGUAGE_NAME))) {
            settingsFragment = new SettingsFragment();
            currentFragment = getString(FRAGMENT_SETTINGS_NAME);
            manager.beginTransaction().replace(R.id.fragment_container, settingsFragment).commit();
            setToolbar(currentFragment);
        }
    }

    public void onClickTickButton(View view) {
        if (currentFragment.equals(getString(FRAGMENT_PASSWORD_NAME)) || currentFragment.equals(getString(FRAGMENT_LANGUAGE_NAME))) {
            settingsFragment = new SettingsFragment();
            currentFragment = getString(FRAGMENT_SETTINGS_NAME);
            manager.beginTransaction().replace(R.id.fragment_container, settingsFragment).commit();
            setToolbar(currentFragment);
        }
    }

    private void setToolbar(String nameSelectedFragment) {
        toolbarTitleView = findViewById(R.id.toolbar_title);
        toolbarBackButton = findViewById(R.id.toolbar_back_button);
        toolbarLogoutButton = findViewById(R.id.toolbar_logout_button);
        toolbarCancelButton = findViewById(R.id.toolbar_cancel_button);
        toolbarTickButton = findViewById(R.id.toolbar_tick_button);

        toolbarBackButton.setVisibility(View.GONE);
        toolbarLogoutButton.setVisibility(View.GONE);
        toolbarCancelButton.setVisibility(View.GONE);
        toolbarTickButton.setVisibility(View.GONE);

        switch (nameSelectedFragment) {
            case "home":
                toolbarTitleView.setText(TOOLBAR_HOME_TEXT);
                break;
            case "playlist":
                toolbarTitleView.setText(TOOLBAR_PLAYLIST_TEXT);
                break;
            case "record":
                toolbarTitleView.setText(TOOLBAR_RECORD_TEXT);
                toolbarBackButton.setVisibility(View.VISIBLE);
                break;
            case "settings":
                toolbarTitleView.setText(TOOLBAR_SETTINGS_TEXT);
                toolbarTickButton.setVisibility(View.VISIBLE);
                break;
            case "personal_page":
                toolbarTitleView.setText(TOOLBAR_PERSONAL_PAGE_TEXT);
                toolbarLogoutButton.setVisibility(View.VISIBLE);
                break;
            case "language":
                toolbarTitleView.setText(TOOLBAR_LANGUAGE_TEXT);
                toolbarCancelButton.setVisibility(View.VISIBLE);
                toolbarTickButton.setVisibility(View.VISIBLE);
                break;
            case "password":
                toolbarTitleView.setText(TOOLBAR_PASSWORD_TEXT);
                toolbarCancelButton.setVisibility(View.VISIBLE);
                toolbarTickButton.setVisibility(View.VISIBLE);
                break;
        }
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navigationListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @SuppressLint("NonConstantResourceId")
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    String nameSelectedFragment = null;

                    switch (item.getItemId()) {
                        case R.id.nav_home:
                            selectedFragment = new HomeFragment();
                            nameSelectedFragment = getResources().getString(FRAGMENT_HOME_NAME);
                            break;
                        case R.id.nav_playlist:
                            selectedFragment = new MainListOfRecordsFragment();
                            nameSelectedFragment = getResources().getString(FRAGMENT_PLAYLIST_NAME);
                            break;
                        case R.id.nav_record:
                            selectedFragment = new RecordFragment();
                            nameSelectedFragment = getResources().getString(FRAGMENT_RECORD_NAME);
                            break;
                        case R.id.nav_settings:
                            selectedFragment = new SettingsFragment();
                            nameSelectedFragment = getResources().getString(FRAGMENT_SETTINGS_NAME);
                            break;
                        case R.id.nav_personal_page:
                            selectedFragment = new PersonalPageFragment();
                            nameSelectedFragment = getResources().getString(FRAGMENT_PERSONAL_PAGE_NAME);
                            break;
                    }

                    if (currentFragment == null || !currentFragment.equals(nameSelectedFragment)) {
                        assert selectedFragment != null;
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                selectedFragment).addToBackStack(currentFragment).commit();
                        currentFragment = nameSelectedFragment;
                        setToolbar(nameSelectedFragment);
                    }
                    return true;
                }
            };

    @Override
    public void onBackPressed() {
        if (currentFragment.equals(getString(FRAGMENT_PASSWORD_NAME)) || currentFragment.equals(getString(FRAGMENT_LANGUAGE_NAME))) {
            setToolbar(getString(FRAGMENT_SETTINGS_NAME));
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        OnBackPressedListener backPressedListener = null;
        for (Fragment fragment : fragmentManager.getFragments()) {
            if (fragment instanceof OnBackPressedListener) {
                backPressedListener = (OnBackPressedListener) fragment;
                break;
            }
        }

        if (backPressedListener != null) {
            backPressedListener.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    public interface OnBackPressedListener {
        void onBackPressed();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENT_FRAGMENT, currentFragment);
    }
}
