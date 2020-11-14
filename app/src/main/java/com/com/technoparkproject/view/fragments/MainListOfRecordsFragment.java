package com.com.technoparkproject.view.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.com.technoparkproject.R;
import com.com.technoparkproject.TestErrorShower;
import com.com.technoparkproject.interfaces.MainListRecordsInterface;
import com.com.technoparkproject.models.Record;
import com.com.technoparkproject.models.Topic;
import com.com.technoparkproject.view.adapters.main_list_records.RecyclerTopicsWithRecordsAdapter;
import com.com.technoparkproject.view_models.MainListOfRecordsViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class MainListOfRecordsFragment extends Fragment implements MainListRecordsInterface {
    private RecyclerTopicsWithRecordsAdapter adapter;
    private AutoCompleteTextView searchingField;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) LayoutInflater.from(getContext())
                .inflate(R.layout.fragment_main_list_records, container,
                        false);

        RecyclerView rvMainList = view.findViewById(R.id.mlr_rv_main_list);
        rvMainList.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new RecyclerTopicsWithRecordsAdapter(this);
        rvMainList.setAdapter(adapter);

        searchingField = view.findViewById(R.id.mlr_et_searching);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainListOfRecordsViewModel viewModel = new ViewModelProvider(this,
                new ViewModelProvider.NewInstanceFactory())
                .get(MainListOfRecordsViewModel.class);
        observeToData(viewModel);
    }

    private void observeToData(MainListOfRecordsViewModel viewModel) {
        viewModel.getTopics().observe(this, new Observer<List<Topic>>() {
            @Override
            public void onChanged(List<Topic> topics) {
                adapter.setItems(topics);
                setAutoCompleteValues(topics);
            }
        });

        viewModel.setSearchingInput(searchingField);
        viewModel.getSearchingValue().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                adapter.filterItemsByTopicName(s);
            }
        });
    }

    private void setAutoCompleteValues(List<Topic> topics) {
        ArrayList<String> names = new ArrayList<>();
        for (Topic topic: topics)
            names.add(topic.name);

        if(getContext() == null) return;
        ArrayAdapter adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, names);
        searchingField.setAdapter(adapter);
    }

    @Override
    public void showAllRecords(String topicUUID) {
        TestErrorShower.showErrorDevelopment(getContext());
    }

    @Override
    public void showRecordMoreFun(Record record) {
        if(getContext() == null) return;

        Dialog dialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialog);
        @SuppressLint("InflateParams")
        View bottomSheetView = getLayoutInflater()
                .inflate(R.layout.mlr_bottom_sheet_record_functions, null);
        dialog.setContentView(bottomSheetView);
        bottomSheetView.findViewById(R.id.mlr_add_to_queue)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TestErrorShower.showErrorDevelopment(getContext());
                    }
                });
        bottomSheetView.findViewById(R.id.mlr_download)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TestErrorShower.showErrorDevelopment(getContext());
                    }
                });
        dialog.show();
    }


}
