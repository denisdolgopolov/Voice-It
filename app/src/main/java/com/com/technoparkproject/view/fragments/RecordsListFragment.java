package com.com.technoparkproject.view.fragments;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.com.technoparkproject.R;
import com.com.technoparkproject.repository.Record;
import com.com.technoparkproject.repository.RecordRepoImpl;

import java.util.Collection;
import java.util.List;

public class RecordsListFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_records,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recycler = view.findViewById(R.id.records);
        final RecordsListFragment.RecordsAdapter adapter = new RecordsListFragment.RecordsAdapter();
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private class RecordsAdapter extends RecyclerView.Adapter<RecordsListFragment.RecordViewHolder> {

        private final List<Record> mRecords = RecordRepoImpl.getInstance(getContext()).getRecords();

        @NonNull
        @Override
        public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RecordsListFragment.RecordViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.record_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecordsListFragment.RecordViewHolder holder, int position) {
            final Record record = mRecords.get(position);
            holder.mRecName.setText(record.getName());
            holder.mRecTopic.setText(record.getTopic());
            holder.mRecTime.setText(DateUtils.formatElapsedTime(record.getDuration()/1000));
            holder.mRecFile.setText(record.getRecordFile().toString());
        }

        @Override
        public int getItemCount() {
            return mRecords.size();
        }
    }

    static class RecordViewHolder extends RecyclerView.ViewHolder {

        protected TextView mRecName;
        protected TextView mRecTopic;
        protected TextView mRecTime;
        protected TextView mRecFile;

        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            mRecName = itemView.findViewById(R.id.record_name);
            mRecTopic = itemView.findViewById(R.id.record_topic);
            mRecTime = itemView.findViewById(R.id.record_time);
            mRecFile = itemView.findViewById(R.id.record_file);
        }
    }
}