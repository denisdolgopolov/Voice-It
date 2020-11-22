package com.com.technoparkproject.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.com.technoparkproject.R;
import com.com.technoparkproject.models.Record;
import com.com.technoparkproject.view_models.MainListOfRecordsViewModel;
import com.com.technoparkproject.view_models.PlaylistViewModel;

import java.util.ArrayList;
import java.util.List;

public class PlaylistFragment extends Fragment {
    PlaylistAdapter playlistAdapter = new PlaylistAdapter();
    PlaylistViewModel viewModel;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        viewModel = new ViewModelProvider(
                this,
                new PlaylistViewModel.Factory(getActivity())
        ).get(PlaylistViewModel.class);
        RecyclerView PlaylistRecyclerView = view.findViewById(R.id.playlist_recycler_view);
        PlaylistRecyclerView.setAdapter(playlistAdapter);
        playlistAdapter.setRecordList(viewModel.recordList);

        return view;
    }

    public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

        List<Record> recordList = new ArrayList<>();

        @NonNull
        @Override
        public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_of_list_records, parent, false);
            return new PlaylistViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
            Record record = recordList.get(position);
            holder.textViewTitle.setText(record.name);
            holder.textViewRecordTime.setText(record.duration);
            holder.textViewDesc.setText(record.userUUID);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                     viewModel.itemClicked(position);
                     getFragmentManager().beginTransaction().add(R.id.minimized_player_fragment_container, new MinimizedPlayerFragment()).commit();

                }
            });
            holder.recordImage.setImageResource(R.drawable.mlr_test_record_image);
        }

        @Override
        public int getItemCount() {
            return recordList.size();
        }

        public void setRecordList(List<Record> recordArrayList) {
            this.recordList = recordArrayList;
            notifyDataSetChanged();
        }

        public class PlaylistViewHolder extends RecyclerView.ViewHolder {
            public TextView textViewTitle;
            public TextView textViewDesc;
            public TextView textViewRecordTime;
            public ImageView recordImage;

            PlaylistViewHolder(@NonNull View itemView) {
                super(itemView);
                this.textViewTitle = itemView.findViewById(R.id.mlr_text_view_title);
                this.textViewDesc = itemView.findViewById(R.id.mlr_text_view_desc);
                this.textViewRecordTime = itemView.findViewById(R.id.mlr_text_view_record_time);
                this.recordImage = itemView.findViewById(R.id.mlr_record_logo);
            }
        }
    }
}
