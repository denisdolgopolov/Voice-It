package com.technopark.recorder.views;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.technopark.recorder.utils.InjectorUtils;

public abstract class RecorderFragment extends Fragment {
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1000;


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkRecordPermissions() {
        if (requireActivity().checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            setPermissionRequestLayout();
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSIONS_REQUEST_RECORD_AUDIO);
        }
    }

    public abstract void setPermissionRequestLayout();

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //configure recorder manually, because it's not possible to fully init it
                //before permissions are granted by the user
                InjectorUtils.provideRecService(getContext()).configureRecording();
                setPermissionGrantedLayout();
            } else {
                setPermissionDeniedLayout();
            }
        }
    }

    protected abstract void setPermissionDeniedLayout();

    protected abstract void setPermissionGrantedLayout();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkRecordPermissions();
        }
    }
}

