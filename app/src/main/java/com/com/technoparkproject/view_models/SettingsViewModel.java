package com.com.technoparkproject.view_models;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.google.firebase.auth.FirebaseAuth;

import voice.it.firebaseloadermodule.FirebaseFileLoader;
import voice.it.firebaseloadermodule.cnst.FirebaseFileTypes;
import voice.it.firebaseloadermodule.listeners.FirebaseGetUriListener;

public class SettingsViewModel extends AndroidViewModel {
    private Bitmap profileImage = null;
    public SettingsViewModel(@NonNull Application application) {
        super(application);
    }

    public void setProfileImage(Bitmap profileImage){
        this.profileImage = profileImage;
    }

    public Bitmap getProfileImage() {
        return profileImage;
    }
}
