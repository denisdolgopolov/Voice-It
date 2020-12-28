package com.com.technoparkproject.view.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.com.technoparkproject.R;
import com.com.technoparkproject.utils.FileUtil;
import com.com.technoparkproject.view.activities.MainActivity;
import com.com.technoparkproject.view_models.SettingsViewModel;
import com.example.player.PlayerService;
import com.google.firebase.auth.FirebaseAuth;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import voice.it.firebaseloadermodule.FirebaseFileLoader;
import voice.it.firebaseloadermodule.cnst.FirebaseFileTypes;
import voice.it.firebaseloadermodule.listeners.FirebaseGetUriListener;
import voice.it.firebaseloadermodule.listeners.FirebaseListener;
import voice.it.firebaseloadermodule.model.FirebaseModel;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends Fragment implements FirebaseGetUriListener, ImageLoadingListener {

    public static final String TAG = "SettingsFragmentTag";
    private static final int FRAGMENT_SETTINGS_NAME = R.string.fragment_settings_name;
    public static final String PROFILE_IMAGE = "Profile image";
    public static final int READ_STORAGE_PERMISSION = 1001;
    SettingsViewModel viewModel;
    ImageView profileImageView;
    private ImageLoader imageLoader = ImageLoader.getInstance();


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Intent.CONTENTS_FILE_DESCRIPTOR) {
                if (data != null) {
                    Uri imageUri = data.getData();
                    try {
                        File file = FileUtil.from(getActivity(), imageUri);
                        FileInputStream fileInputStream = new FileInputStream(file);
                        InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
                        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        new FirebaseFileLoader(getActivity()).uploadFile(
                                fileInputStream,
                                FirebaseFileTypes.USER_PROFILE_IMAGES,
                                (long) selectedImage.getAllocationByteCount(),
                                new FirebaseModel(FirebaseAuth.getInstance().getUid(), PROFILE_IMAGE),
                                new FirebaseListener() {
                                    @Override
                                    public void onSuccess() {
                                        Toast toast = Toast.makeText(getActivity(), "Изображение загружено", Toast.LENGTH_SHORT);
                                        toast.show();
                                        try {
                                            imageStream.close();
                                            fileInputStream.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onFailure(String error) {

                                    }
                                }
                        );

                        profileImageView.setImageBitmap(selectedImage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setToolbar(getString(FRAGMENT_SETTINGS_NAME));
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        viewModel = new ViewModelProvider(getActivity()).get(SettingsViewModel.class);
        imageLoader.init(new ImageLoaderConfiguration.Builder(getActivity()).build());
        view.findViewById(R.id.new_profile_image_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), Intent.CONTENTS_FILE_DESCRIPTOR);
            }
        });
        profileImageView = view.findViewById(R.id.settings_profile_image_view);

        if (viewModel.getProfileImage() == null) {
            new FirebaseFileLoader(getActivity().getApplicationContext())
                    .getDownloadUri(FirebaseFileTypes.USER_PROFILE_IMAGES, FirebaseAuth.getInstance().getUid(), this);
        } else {
            profileImageView.setImageBitmap(viewModel.getProfileImage());
        }

        return view;
    }

    @Override
    public void onGet(Uri uri) {
        imageLoader.loadImage(String.valueOf(uri), this);
    }

    @Override
    public void onFailure(String error) {

    }

    @Override
    public void onLoadingStarted(String imageUri, View view) {

    }

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

    }

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        profileImageView.setImageBitmap(loadedImage);
        viewModel.setProfileImage(loadedImage);
    }

    @Override
    public void onLoadingCancelled(String imageUri, View view) {

    }
}
