package com.com.technoparkproject.view.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Patterns;
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

import com.avatarfirst.avatargenlib.AvatarConstants;
import com.avatarfirst.avatargenlib.AvatarGenerator;
import com.com.technoparkproject.R;
import com.com.technoparkproject.view.fragments.AnotherAccountFragment;
import com.com.technoparkproject.view.fragments.EmailFragment;
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
import com.example.player.PlayerService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.technopark.recorder.service.RecordIntentConstants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import voice.it.firebaseloadermodule.FirebaseFileLoader;
import voice.it.firebaseloadermodule.FirebaseLoader;
import voice.it.firebaseloadermodule.cnst.FirebaseCollections;
import voice.it.firebaseloadermodule.cnst.FirebaseFileTypes;
import voice.it.firebaseloadermodule.listeners.FirebaseGetListener;
import voice.it.firebaseloadermodule.listeners.FirebaseListener;
import voice.it.firebaseloadermodule.model.FirebaseModel;


public class MainActivity extends AppCompatActivity {

    private String currentFragment = null;
    private String userName = null;
    private String anotherAccountName = null;

    private static final String CURRENT_FRAGMENT = "CURRENT FRAGMENT";

    private EditText editTextEmailToRegister;
    private EditText editTextPasswordToRegister;

    private EditText editTextEmailToLogin;
    private EditText editTextPasswordToLogin;

    private EditText editTextCurrentPassword;
    private EditText editTextNewPassword;
    private EditText editTextRepeatNewPassword;

    private EditText editTextNewUsername;
    private EditText editTextNewEmail;

    private static final int TOOLBAR_HOME_TEXT = R.string.toolbar_home_text;
    private static final int TOOLBAR_PLAYLIST_TEXT = R.string.toolbar_playlist_text;
    private static final int TOOLBAR_RECORD_TEXT = R.string.toolbar_record_text;
    private static final int TOOLBAR_SETTINGS_TEXT = R.string.toolbar_settings_text;
    private static final int TOOLBAR_PASSWORD_TEXT = R.string.toolbar_password_text;
    private static final int TOOLBAR_LANGUAGE_TEXT = R.string.toolbar_language_text;
    private static final int TOOLBAR_REGISTRATION_TEXT = R.string.toolbar_register_text;
    private static final int TOOLBAR_LOGIN_TEXT = R.string.toolbar_login_text;
    private static final int TOOLBAR_EMAIL_TEXT = R.string.toolbar_email_text;

    private static final int FRAGMENT_HOME_NAME = R.string.fragment_home_name;
    private static final int FRAGMENT_PLAYLIST_NAME = R.string.fragment_playlist_name;
    private static final int FRAGMENT_RECORD_NAME = R.string.fragment_record_name;
    private static final int FRAGMENT_SETTINGS_NAME = R.string.fragment_settings_name;
    private static final int FRAGMENT_PERSONAL_PAGE_NAME = R.string.fragment_personal_page_name;
    private static final int FRAGMENT_EMAIL_NAME = R.string.fragment_email_name;
    private static final int FRAGMENT_PASSWORD_NAME = R.string.fragment_password_name;
    private static final int FRAGMENT_LANGUAGE_NAME = R.string.fragment_language_name;
    private static final int FRAGMENT_REGISTRATION_NAME = R.string.fragment_registration_name;
    private static final int FRAGMENT_START_NAME = R.string.fragment_start_name;
    private static final int FRAGMENT_LOGIN_NAME = R.string.fragment_login_name;
    private static final int FRAGMENT_ANOTHER_ACCOUNT_NAME = R.string.fragment_another_account_name;

    public static final String PROFILE_IMAGE = "Profile image";

    BottomNavigationView bottomNavigation;


    private Toolbar toolbar;
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
            if (mAuth.getCurrentUser() == null) {
                firstEnterInApp();
            } else if (currentFragment == null) {
                enterToApp();
            }
        }
    }

    public void onClickGoToAccount(String userUUID) {
        currentFragment = getString(FRAGMENT_ANOTHER_ACCOUNT_NAME);
        AnotherAccountFragment anotherAccountFragment = new AnotherAccountFragment();
        anotherAccountFragment.setUserUUID(userUUID);
        loadFragment(anotherAccountFragment, currentFragment);
        getUserName(userUUID, listener);
    }

    private void getUserName(String userUUID, FirebaseGetListener<String> listener) {
        new FirebaseLoader().getByUUID(FirebaseCollections.Users, userUUID, new FirebaseGetListener() {
            @Override
            public void onFailure(String error) {
                listener.onFailure("No such document");
            }

            @Override
            public void onGet(Object item) {
                userName = ((voice.it.firebaseloadermodule.model.FirebaseUser) item).getUserName();
                listener.onGet(userName);
                }
            });
    }

    private final FirebaseGetListener<String> listener = new FirebaseGetListener<String>() {
        @Override
        public void onFailure(String error) {
            System.out.println(error);
        }

        @Override
        public void onGet(String item) {
            TextView toolbarTitleView = findViewById(R.id.toolbar_title);
            if (currentFragment.equals(getString(FRAGMENT_PERSONAL_PAGE_NAME))) {
                userName = item;
                toolbarTitleView.setText(userName);
            } else if (currentFragment.equals(getString(FRAGMENT_ANOTHER_ACCOUNT_NAME))) {
                anotherAccountName = item;
                toolbarTitleView.setText(anotherAccountName);
            }
        }
    };

    public String getCurrentUserId() {
        return mAuth.getCurrentUser().getUid();
    }

    private void firstEnterInApp() {
        toolbar.setVisibility(View.GONE);
        bottomNavigation.setVisibility(View.GONE);
        StartFragment startFragment = new StartFragment();
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
            if (nextFragment.equals(PlayerService.VALUE)) {
                BottomNavigationView btmNav = findViewById(R.id.bottom_navigation);
                btmNav.setSelectedItemId(R.id.nav_playlist);
            }
        }
    }

    private boolean updateEmailOfUser() {
        String currentPassword = editTextCurrentPassword.getText().toString().trim();
        String newEmail = editTextNewEmail.getText().toString().trim();

        if (newEmail.isEmpty()) {
            editTextNewEmail.setError(getString(R.string.enter_your_new_email));
            editTextNewEmail.requestFocus();
            return false;
        }

        if (currentPassword.isEmpty()) {
            editTextCurrentPassword.setError(getString(R.string.enter_your_current_password));
            editTextCurrentPassword.requestFocus();
            return false;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        String userEmail = user.getEmail();

        AuthCredential credential = EmailAuthProvider.getCredential(userEmail, currentPassword);

        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);

        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user.updateEmail(newEmail).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(MainActivity.this, getString(R.string.email_update), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.error_password), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.error_auth), Toast.LENGTH_LONG).show();
            }
            progressDialog.dismiss();
        });

        return true;
    }

    private boolean updatePasswordOfUser() {
        String currentPassword = editTextCurrentPassword.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString().trim();
        String repeatNewPassword = editTextRepeatNewPassword.getText().toString().trim();

        if (currentPassword.isEmpty()) {
            editTextCurrentPassword.setError(getString(R.string.enter_your_current_password));
            editTextCurrentPassword.requestFocus();
            return false;
        }

        if (newPassword.isEmpty()) {
            editTextNewPassword.setError(getString(R.string.enter_your_parrword));
            editTextNewPassword.requestFocus();
            return false;
        }

        if (repeatNewPassword.isEmpty()) {
            editTextRepeatNewPassword.setError(getString(R.string.repeat_answer));
            editTextRepeatNewPassword.requestFocus();
            return false;
        }

        if (!repeatNewPassword.equals(newPassword)) {
            editTextRepeatNewPassword.setError(getString(R.string.password_dont_match));
            editTextRepeatNewPassword.requestFocus();
            return false;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        String userEmail = user.getEmail();

        AuthCredential credential = EmailAuthProvider.getCredential(userEmail, currentPassword);

        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);

        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(MainActivity.this, getString(R.string.password_update), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.error_password), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.error_auth), Toast.LENGTH_LONG).show();
            }
            progressDialog.dismiss();
        });

        return true;
    }

    private void loginUser() {
        String email = editTextEmailToLogin.getText().toString().trim();
        String password = editTextPasswordToLogin.getText().toString().trim();

        if (email.isEmpty()) {
            editTextEmailToLogin.setError(getString(R.string.enter_your_email));
            editTextEmailToLogin.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextPasswordToLogin.setError(getString(R.string.enter_your_password));
            editTextPasswordToLogin.requestFocus();
            return;
        }

        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                getUserName(getCurrentUserId(), listener);
                Toast.makeText(MainActivity.this, getString(R.string.s1), Toast.LENGTH_LONG).show();
                clearBackStack();
                enterToApp();
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.s2), Toast.LENGTH_LONG).show();
            }
            progressDialog.dismiss();
        });
    }

    private void registerUser() {
        String email = editTextEmailToRegister.getText().toString().trim();
        String password = editTextPasswordToRegister.getText().toString().trim();

        if (email.isEmpty()) {
            editTextEmailToRegister.setError(getString(R.string.s3));
            editTextEmailToRegister.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextPasswordToRegister.setError(getString(R.string.s4));
            editTextPasswordToRegister.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmailToRegister.setError(getString(R.string.s5));
            editTextEmailToRegister.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editTextPasswordToRegister.setError(getString(R.string.s6));
            editTextPasswordToRegister.requestFocus();
            return;
        }

        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(MainActivity.this, R.string.s9, Toast.LENGTH_LONG).show();
                clearBackStack();
                enterToApp();
                userName = email;
                for (int i = 0; i < userName.length(); i++) {
                    if (userName.charAt(i) == '@') {
                        userName = userName.substring(0, i);
                    }
                }

                Map<String, Object> userData = new HashMap<>();
                userData.put("userName", userName);
                userData.put("userAuthUUID", getCurrentUserId());
                new FirebaseLoader().setByUUID(FirebaseCollections.Users, getCurrentUserId(),
                        userData, new FirebaseListener() {
                            @Override
                            public void onSuccess() {
                                //System.out.println("DocumentSnapshot successfully written!");
                            }

                            @Override
                            public void onFailure(String error) {
                                //System.out.println("Error writing document " + e);
                            }
                        });


                Bitmap userProfileImage = AvatarGenerator.
                        Companion.
                        avatarImage(MainActivity.this, 176, 176, userName, AvatarConstants.Companion.getCOLOR900()).
                        getBitmap();
                new FirebaseFileLoader(MainActivity.this).uploadFile(
                        bitmap2InputStream(userProfileImage),
                        FirebaseFileTypes.USER_PROFILE_IMAGES,
                        (long) userProfileImage.getAllocationByteCount(),
                        new FirebaseModel(FirebaseAuth.getInstance().getUid(), PROFILE_IMAGE),
                        new FirebaseListener() {

                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onFailure(String error) {

                            }
                        }
                );
                getUserName(getCurrentUserId(), listener);
            } else {
                Toast.makeText(MainActivity.this, R.string.s8, Toast.LENGTH_LONG).show();
            }
            progressDialog.dismiss();
        });

    }

    public static InputStream bitmap2InputStream(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        return is;
    }

    private void logoutUser() {
        Toast.makeText(MainActivity.this, getString(R.string.s7), Toast.LENGTH_LONG).show();
        mAuth.signOut();
        clearBackStack();
        firstEnterInApp();
    }

    @SuppressLint("NonConstantResourceId")
    public void onClickCreateAccountOrLoginButton(View view) {
        switch (view.getId()) {
            case R.id.btn_create_account:
                progressDialog = new ProgressDialog(this);
                Button buttonRegisterUser = findViewById(R.id.btn_create_account);
                editTextEmailToRegister = findViewById(R.id.registration_email);
                editTextPasswordToRegister = findViewById(R.id.registration_password);
                registerUser();
                break;

            case R.id.btn_login:
                progressDialog = new ProgressDialog(this);
                Button buttonLoginUser = findViewById(R.id.btn_login);
                editTextEmailToLogin = findViewById(R.id.login_email);
                editTextPasswordToLogin = findViewById(R.id.login_password);
                loginUser();
                break;
        }
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
                RegistrationFragment registrationFragment = new RegistrationFragment();
                currentFragment = getString(FRAGMENT_REGISTRATION_NAME);
                loadFragment(registrationFragment, currentFragment);
                break;

            case R.id.btn_login:
                LoginFragment loginFragment = new LoginFragment();
                currentFragment = getString(FRAGMENT_LOGIN_NAME);
                loadFragment(loginFragment, currentFragment);
                break;
        }
        toolbar.setVisibility(View.VISIBLE);
    }

    @SuppressLint("NonConstantResourceId")
    public void onClickChangePasswordOrLanguageOrEmailButton(View view) {
        switch (view.getId()) {
            case R.id.btn_change_password:
                PasswordFragment changePasswordFragment = new PasswordFragment();
                currentFragment = getString(FRAGMENT_PASSWORD_NAME);
                loadFragment(changePasswordFragment, currentFragment);
                break;

            case R.id.btn_change_language:
                LanguageFragment changeLanguageFragment = new LanguageFragment();
                currentFragment = getString(FRAGMENT_LANGUAGE_NAME);
                loadFragment(changeLanguageFragment, currentFragment);
                break;

            case R.id.btn_change_email:
                EmailFragment changeEmailFragment = new EmailFragment();
                currentFragment = getString(FRAGMENT_EMAIL_NAME);
                loadFragment(changeEmailFragment, currentFragment);
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

    public void onClickCancelButton(View view) {
        if (currentFragment.equals(getString(FRAGMENT_PASSWORD_NAME)) || currentFragment.equals(getString(FRAGMENT_LANGUAGE_NAME)) || currentFragment.equals(getString(FRAGMENT_EMAIL_NAME))) {
            currentFragment = getString(FRAGMENT_SETTINGS_NAME);
            undoFragment();
        }
    }

    public void onClickTickButton(View view) {
        if (currentFragment.equals(getString(FRAGMENT_PASSWORD_NAME))) {
            progressDialog = new ProgressDialog(this);
            editTextCurrentPassword = findViewById(R.id.et_enter_current_password);
            editTextNewPassword = findViewById(R.id.et_enter_new_password);
            editTextRepeatNewPassword = findViewById(R.id.et_repeat_new_password);
            if (updatePasswordOfUser()) {
                currentFragment = getString(FRAGMENT_SETTINGS_NAME);
                undoFragment();
            }
        } else if (currentFragment.equals(getString(FRAGMENT_EMAIL_NAME))) {
            progressDialog = new ProgressDialog(this);
            editTextCurrentPassword = findViewById(R.id.et_current_password);
            editTextNewEmail = findViewById(R.id.et_enter_new_email);
            if (updateEmailOfUser()) {
                currentFragment = getString(FRAGMENT_SETTINGS_NAME);
                undoFragment();
            }
        } else if (currentFragment.equals(getString(FRAGMENT_LANGUAGE_NAME))) {
            currentFragment = getString(FRAGMENT_SETTINGS_NAME);
            undoFragment();
        } else if (currentFragment.equals(getString(FRAGMENT_SETTINGS_NAME))) {
            editTextNewUsername = findViewById(R.id.et_new_nickname);
            String newUsername = editTextNewUsername.getText().toString().trim();
            Map<String,Object> updateData = new HashMap<>();
            updateData.put("userName",newUsername);
            new FirebaseLoader().updateByUUID(FirebaseCollections.Users, getCurrentUserId(),
                    updateData, new FirebaseListener() {
                        @Override
                        public void onSuccess() {
                            userName = newUsername;
                            editTextNewUsername.setText("");
                            Toast.makeText(MainActivity.this, R.string.username_updated, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(String error) {

                            Toast.makeText(MainActivity.this, R.string.error_username_updated, Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    public void setToolbar(String nameSelectedFragment) {
        TextView toolbarTitleView = findViewById(R.id.toolbar_title);
        ImageButton toolbarBackButton = findViewById(R.id.toolbar_back_button);
        ImageButton toolbarLogoutButton = findViewById(R.id.toolbar_logout_button);
        ImageButton toolbarCancelButton = findViewById(R.id.toolbar_cancel_button);
        ImageButton toolbarTickButton = findViewById(R.id.toolbar_tick_button);

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
                break;
            case "settings":
                toolbarTitleView.setText(TOOLBAR_SETTINGS_TEXT);
                toolbarTickButton.setVisibility(View.VISIBLE);
                break;
            case "personal_page":
                getUserName(getCurrentUserId(), listener);
                toolbarLogoutButton.setVisibility(View.VISIBLE);
                break;
            case "email":
                toolbarTitleView.setText(TOOLBAR_EMAIL_TEXT);
                toolbarCancelButton.setVisibility(View.VISIBLE);
                toolbarTickButton.setVisibility(View.VISIBLE);
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
            case "another_account":
                toolbarTitleView.setText(anotherAccountName);
                break;
        }
    }

    @SuppressLint("NonConstantResourceId")
    private final BottomNavigationView.OnNavigationItemSelectedListener navigationListener =
            item -> {
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
