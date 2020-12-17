package com.com.technoparkproject.view.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.com.technoparkproject.R;
import com.com.technoparkproject.view.fragments.LanguageFragment;
import com.com.technoparkproject.view.fragments.LoginFragment;
import com.com.technoparkproject.view.fragments.MainListOfRecordsFragment;
import com.com.technoparkproject.view.fragments.PasswordFragment;
import com.com.technoparkproject.view.fragments.PersonalPageFragment;
import com.com.technoparkproject.view.fragments.PlaylistFragment;
import com.com.technoparkproject.view.fragments.RecordFragment;
import com.com.technoparkproject.view.fragments.RegistrationFragment;
import com.com.technoparkproject.view.fragments.SettingsFragment;
import com.com.technoparkproject.view.fragments.StartFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.technopark.recorder.service.RecordIntentConstants;

public class MainActivity extends AppCompatActivity {

    private String currentFragment = null;
    private String userName = null;

    private static final String CURRENT_FRAGMENT = "Current fragment";

    private TextView toolbarTitleView;

    private ImageButton toolbarBackButton;
    private ImageButton toolbarLogoutButton;
    private ImageButton toolbarCancelButton;
    private ImageButton toolbarTickButton;

    private EditText editTextEmailToRegister;
    private EditText editTextPasswordToRegister;

    private EditText editTextEmailToLogin;
    private EditText editTextPasswordToLogin;

    private Button buttonRegisterUser;
    private Button buttonLoginUser;

    private static final int TOOLBAR_HOME_TEXT = R.string.toolbar_home_text;
    private static final int TOOLBAR_PLAYLIST_TEXT = R.string.toolbar_playlist_text;
    private static final int TOOLBAR_RECORD_TEXT = R.string.toolbar_record_text;
    private static final int TOOLBAR_SETTINGS_TEXT = R.string.toolbar_settings_text;
    private static final int TOOLBAR_PASSWORD_TEXT = R.string.toolbar_password_text;
    private static final int TOOLBAR_LANGUAGE_TEXT = R.string.toolbar_language_text;
    private static final int TOOLBAR_REGISTRATION_TEXT = R.string.toolbar_register_text;
    private static final int TOOLBAR_LOGIN_TEXT = R.string.toolbar_login_text;

    private static final int FRAGMENT_HOME_NAME = R.string.fragment_home_name;
    private static final int FRAGMENT_PLAYLIST_NAME = R.string.fragment_playlist_name;
    private static final int FRAGMENT_RECORD_NAME = R.string.fragment_record_name;
    private static final int FRAGMENT_SETTINGS_NAME = R.string.fragment_settings_name;
    private static final int FRAGMENT_PERSONAL_PAGE_NAME = R.string.fragment_personal_page_name;
    private static final int FRAGMENT_PASSWORD_NAME = R.string.fragment_password_name;
    private static final int FRAGMENT_LANGUAGE_NAME = R.string.fragment_language_name;
    private static final int FRAGMENT_REGISTRATION_NAME = R.string.fragment_registration_name;
    private static final int FRAGMENT_START_NAME = R.string.fragment_start_name;
    private static final int FRAGMENT_LOGIN_NAME = R.string.fragment_login_name;

    private PasswordFragment changePasswordFragment;
    private LanguageFragment changeLanguageFragment;
    private StartFragment startFragment;
    private LoginFragment loginFragment;
    private RegistrationFragment registrationFragment;

    private Toolbar toolbar;
    private BottomNavigationView bottomNavigation;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationListener);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);

        mAuth = FirebaseAuth.getInstance();

        if (savedInstanceState != null) {
            currentFragment = savedInstanceState.getString(CURRENT_FRAGMENT);
        } else {
            checkRecordIntent(getIntent());
        }

        if (mAuth.getCurrentUser() == null) {
            firstEnterInApp();
        } else {
            userName = getUsername();
            if (currentFragment == null) {
                enterToApp();
            }
        }
    }

    private void firstEnterInApp() {
        toolbar.setVisibility(View.GONE);
        bottomNavigation.setVisibility(View.GONE);
        startFragment = new StartFragment();
        currentFragment = getString(FRAGMENT_START_NAME);
        loadFragment(startFragment, currentFragment);
    }

    private void enterToApp() {
        toolbar.setVisibility(View.VISIBLE);
        bottomNavigation.setVisibility(View.VISIBLE);
        bottomNavigation.setSelectedItemId(R.id.nav_home);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkRecordIntent(intent);
    }

    private void checkRecordIntent(Intent intent) {
        String nextFragment = intent.getStringExtra(RecordIntentConstants.NAME);
        if (nextFragment != null) {
            //if activity received intent from record notification go to record fragment
            if (nextFragment.equals(RecordIntentConstants.VALUE)) {
                BottomNavigationView btmNav = findViewById(R.id.bottom_navigation);
                btmNav.setSelectedItemId(R.id.nav_record);
            }
        }
    }

    @SuppressLint("NonConstantResourceId")
    public void onClickCreateAccountOrLoginButton(View view) {
        switch (view.getId()) {
            case R.id.btn_create_account:
                progressDialog = new ProgressDialog(this);
                buttonRegisterUser = findViewById(R.id.btn_create_account);
                editTextEmailToRegister = findViewById(R.id.registration_email);
                editTextPasswordToRegister = findViewById(R.id.registration_password);
                registerUser();
                break;

            case R.id.btn_login:
                progressDialog = new ProgressDialog(this);
                buttonLoginUser = findViewById(R.id.btn_login);
                editTextEmailToLogin = findViewById(R.id.login_email);
                editTextPasswordToLogin = findViewById(R.id.login_password);
                loginUser();
                break;
        }
    }

    private void loginUser() {
        String email = editTextEmailToLogin.getText().toString().trim();
        String password = editTextPasswordToLogin.getText().toString().trim();

        if (email.isEmpty()) {
            editTextEmailToLogin.setError("Enter your email!");
            editTextEmailToLogin.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextPasswordToLogin.setError("Enter your password!");
            editTextPasswordToLogin.requestFocus();
            return;
        }

        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    userName = getUsername();
                    Toast.makeText(MainActivity.this, "Successfully sign in", Toast.LENGTH_LONG).show();
                    clearBackStack();
                    enterToApp();
                } else {
                    Toast.makeText(MainActivity.this, "Sign in fail!", Toast.LENGTH_LONG).show();
                }
                progressDialog.dismiss();
            }
        });
    }

    private String getUsername() {
        String username = mAuth.getCurrentUser().getEmail();
        for (int i = 0; i < username.length(); i++) {
            if (username.charAt(i) == '@') {
                username = username.substring(0, i);
            }
        }
        return username;
    }

    private void registerUser() {
        String email = editTextEmailToRegister.getText().toString().trim();
        String password = editTextPasswordToRegister.getText().toString().trim();

        if (email.isEmpty()) {
            editTextEmailToRegister.setError("Email is required!");
            editTextEmailToRegister.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextPasswordToRegister.setError("Password is required!");
            editTextPasswordToRegister.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmailToRegister.setError("Please provide valid email!");
            editTextEmailToRegister.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editTextPasswordToRegister.setError("Min password length should be 6 characters!");
            editTextPasswordToRegister.requestFocus();
            return;
        }

        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    userName = getUsername();
                    Toast.makeText(MainActivity.this, "Successfully registered", Toast.LENGTH_LONG).show();
                    clearBackStack();
                    enterToApp();

                } else {
                    Toast.makeText(MainActivity.this, "Sign up fail!", Toast.LENGTH_LONG).show();
                }
                progressDialog.dismiss();
            }
        });
    }

    private void logoutUser() {
        Toast.makeText(MainActivity.this, "Successfully logout", Toast.LENGTH_LONG).show();
        mAuth.signOut();
        clearBackStack();
        firstEnterInApp();
    }

    public void onClickLogoutButton(View view) {
        if (view.getId() == R.id.toolbar_logout_button) {
            logoutUser();
        }
    }

    @SuppressLint("NonConstantResourceId")
    public void onClickRegistrationOrLoginButton(View view) {
        switch (view.getId()) {
            case R.id.btn_registration:
                registrationFragment = new RegistrationFragment();
                currentFragment = getString(FRAGMENT_REGISTRATION_NAME);
                loadFragment(registrationFragment, currentFragment);
                break;
            case R.id.btn_login:
                loginFragment = new LoginFragment();
                currentFragment = getString(FRAGMENT_LOGIN_NAME);
                loadFragment(loginFragment, currentFragment);
                break;
        }
        toolbar.setVisibility(View.VISIBLE);
    }

    @SuppressLint("NonConstantResourceId")
    public void onClickChangePasswordOrLanguageButton(View view) {
        switch (view.getId()) {
            case R.id.btn_change_password:
                changePasswordFragment = new PasswordFragment();
                currentFragment = getString(FRAGMENT_PASSWORD_NAME);
                loadFragment(changePasswordFragment, currentFragment);
                break;
            case R.id.btn_change_language:
                changeLanguageFragment = new LanguageFragment();
                currentFragment = getString(FRAGMENT_LANGUAGE_NAME);
                loadFragment(changeLanguageFragment, currentFragment);
                break;
        }
    }

    public void onClickBackButton(View view) {
        if (currentFragment.equals(getString(FRAGMENT_REGISTRATION_NAME)) || currentFragment.equals(getString(FRAGMENT_LOGIN_NAME))) {
            currentFragment = getString(FRAGMENT_START_NAME);
            onBackPressed();
            toolbar.setVisibility(View.GONE);
        }
    }

    public void onClickTickOrCancelButton(View view) {
        if (currentFragment.equals(getString(FRAGMENT_PASSWORD_NAME)) || currentFragment.equals(getString(FRAGMENT_LANGUAGE_NAME))) {
            currentFragment = getString(FRAGMENT_SETTINGS_NAME);
            undoFragment();
        }
    }

    public void setToolbar(String nameSelectedFragment) {
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
                toolbarTitleView.setText(userName);
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
            case "registration":
                toolbarTitleView.setText(TOOLBAR_REGISTRATION_TEXT);
                toolbarBackButton.setVisibility(View.VISIBLE);
                break;
            case "login":
                toolbarTitleView.setText(TOOLBAR_LOGIN_TEXT);
                toolbarBackButton.setVisibility(View.VISIBLE);
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
                            selectedFragment = new MainListOfRecordsFragment();
                            nameSelectedFragment = getResources().getString(FRAGMENT_HOME_NAME);
                            break;
                        case R.id.nav_playlist:
                            selectedFragment = new PlaylistFragment();
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
                        loadFragment(selectedFragment, currentFragment);
                        currentFragment = nameSelectedFragment;
                    }
                    return true;
                }
            };

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            undoFragment();
        }
        if (currentFragment.equals(getString(FRAGMENT_REGISTRATION_NAME)) || currentFragment.equals(getString(FRAGMENT_LOGIN_NAME))) {
            toolbar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENT_FRAGMENT, currentFragment);
    }

    public void loadFragment(Fragment fragment, String currentFragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(currentFragment)
                .commit();
    }

    public void undoFragment() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }
    }

    private void clearBackStack() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = manager.getBackStackEntryAt(0);
            manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }
}
