package com.com.technoparkproject.view.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.com.technoparkproject.R;
import com.com.technoparkproject.TestErrorShower;
import com.com.technoparkproject.broadcasts.BroadcastUpdateListRecords;
import com.com.technoparkproject.interfaces.MainListRecordsInterface;
import com.com.technoparkproject.models.Record;
import com.com.technoparkproject.view.activities.MainActivity;
import com.com.technoparkproject.view.adapters.main_list_records.RecyclerTopicsWithRecordsAdapter;
import com.com.technoparkproject.view_models.AnotherAccountListOfRecordsViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;

import de.hdodenhof.circleimageview.CircleImageView;
import voice.it.firebaseloadermodule.FirebaseFileLoader;
import voice.it.firebaseloadermodule.cnst.FirebaseFileTypes;
import voice.it.firebaseloadermodule.listeners.FirebaseGetUriListener;

public class AnotherAccountFragment extends Fragment implements MainListRecordsInterface, FirebaseGetUriListener {
    private RecyclerTopicsWithRecordsAdapter adapter;
    AnotherAccountListOfRecordsViewModel viewModel;
    private static final int FRAGMENT_ANOTHER_ACCOUNT_NAME = R.string.fragment_another_account_name;
    String userUUID = null;
    CircleImageView profileImageView;

    private final BroadcastUpdateListRecords receiverUpdateList = new BroadcastUpdateListRecords();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setToolbar(getString(FRAGMENT_ANOTHER_ACCOUNT_NAME));
        ViewGroup view = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.fragment_another_account,
                container, false);

        view.findViewById(R.id.b_subscribe).setOnClickListener(button -> {
            TestErrorShower.showErrorDevelopment(getContext());
        });
        viewModel = new ViewModelProvider(getActivity()).get(AnotherAccountListOfRecordsViewModel.class);
        viewModel.queryRecordTopics(userUUID);
        RecyclerView rvAnotherAccountList = view.findViewById(R.id.aclr_list);
        rvAnotherAccountList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecyclerTopicsWithRecordsAdapter(this);
        rvAnotherAccountList.setAdapter(adapter);
        profileImageView = view.findViewById(R.id.profile_image_view);
        if (viewModel.getProfileImage() == null) {
            new FirebaseFileLoader(getActivity().getApplicationContext())
                    .getDownloadUri(FirebaseFileTypes.USER_PROFILE_IMAGES, viewModel.userUUID, this);
        } else {
            profileImageView.setImageBitmap(viewModel.getProfileImage());
        }
        return view;
    }

    public void setUserUUID(String id) {
        this.userUUID = id;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        observeToData(viewModel);
    }

    private void observeToData(final AnotherAccountListOfRecordsViewModel viewModel) {
        viewModel.getTopicRecords().observe(getViewLifecycleOwner(), topicRecs -> adapter.setItems(topicRecs));
    }

    @Override
    public void showAllRecords(String topicUUID) {
        TestErrorShower.showErrorDevelopment(getContext());
    }

    @Override
    public void showRecordMoreFun(final Record record) {
        if (getContext() == null) return;

        final Dialog dialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialog);

        @SuppressLint("InflateParams")
        View bottomSheetView = getLayoutInflater().inflate(R.layout.mlr_bottom_sheet_record_functions, null);
        dialog.setContentView(bottomSheetView);
        bottomSheetView.findViewById(R.id.mlr_add_to_queue).setOnClickListener(v -> {
            viewModel.addToPlaylistClicked(record);
            dialog.dismiss();
        });
        bottomSheetView.findViewById(R.id.mlr_go_to_account).setOnClickListener(v -> {
            ((MainActivity) getActivity()).onClickGoToAccount(record.userUUID);
            dialog.dismiss();
        });
        dialog.show();
    }

    @Override
    public void itemClicked(Record record) {
        viewModel.itemClicked(record);
    }

    @Override
    public LiveData<String> getNowPlayingRecordUUID() {
        return viewModel.nowPlayingRecordUUID;
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().registerReceiver(receiverUpdateList, receiverUpdateList.getIntentFilter());
        receiverUpdateList.setListener(() -> viewModel.queryRecordTopics(userUUID));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(receiverUpdateList);
    }

    @Override
    public void onGet(Uri uri) {
        Glide
                .with(profileImageView)
                .load(uri.toString())
                .into(profileImageView);
    }

    @Override
    public void onFailure(String error) {

    }
}
