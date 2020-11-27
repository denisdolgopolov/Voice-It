package com.com.technoparkproject.view.fragments;

import android.os.Bundle;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.com.technoparkproject.R;
import com.com.technoparkproject.view_models.PlayerViewModel;
import com.com.technoparkproject.view_models.PlaylistViewModel;
import com.example.repo.Record;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.List;

public class PlaylistFragment extends Fragment {
    public static final String MINIMIZED_PLAYER_FRAGMENT_TAG = "MINIMIZED_PLAYER_FRAGMENT_TAG";
    boolean addedMinimizedPlayerFragment = false;
    PlaylistAdapter playlistAdapter = new PlaylistAdapter();
    PlaylistViewModel playlistViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        playlistViewModel = new ViewModelProvider(getActivity()).get(PlaylistViewModel.class);
        RecyclerView PlaylistRecyclerView = view.findViewById(R.id.playlist_recycler_view);
        PlaylistRecyclerView.setAdapter(playlistAdapter);
        getChildFragmentManager().beginTransaction().replace(R.id.minimized_player, new MinimizedPlayerFragment()).commit();
        View playerView = view.findViewById(R.id.minimized_player);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(playerView);
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        playlistViewModel.currentPlaylist.observe(getViewLifecycleOwner(), new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {
                playlistAdapter.setRecordList(playlistViewModel.getListOfRecordsFromUUIDs(strings));
            }
        });

        playlistViewModel.currentState.observe(getViewLifecycleOwner(), new Observer<PlaybackStateCompat>() {
            @Override
            public void onChanged(PlaybackStateCompat playbackStateCompat) {
                if (playbackStateCompat != null) {
                    if (playbackStateCompat.getState() != PlaybackStateCompat.STATE_STOPPED) {
                        if (!addedMinimizedPlayerFragment) {
                            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            PlaylistRecyclerView.setPaddingRelative(PlaylistRecyclerView.getPaddingStart(), PlaylistRecyclerView.getPaddingTop(), PlaylistRecyclerView.getPaddingEnd(), playerView.getHeight() + 10);
                            addedMinimizedPlayerFragment = true;
                        }
                    } else {
                        if (addedMinimizedPlayerFragment) {
                            behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                            PlaylistRecyclerView.setPaddingRelative(PlaylistRecyclerView.getPaddingStart(), PlaylistRecyclerView.getPaddingTop(), PlaylistRecyclerView.getPaddingEnd(), 0);
                            addedMinimizedPlayerFragment = false;
                        }
                    }
                }
            }
        });
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
            holder.textViewRecordTime.setText(durationFormat(Long.parseLong(record.duration)));
            holder.textViewDesc.setText(record.userUUID);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Надо ли выносить адаптер в отдельный файл, а вью модель убирать за интерфейс?
                    playlistViewModel.itemClicked(position);
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
    // TODO вынести это в какой-нибудь утильный класс
    public static String durationFormat(long duration) {
        String durationString;
        int seconds = (int) duration / 1000;
        int minutes = (int) seconds / 60;
        seconds = seconds - minutes * 60;
        if (seconds >= 10) {
            durationString = String.valueOf(minutes) + ":" + String.valueOf(seconds);
        } else {
            durationString = String.valueOf(minutes) + ":0" + String.valueOf(seconds);
        }
        return durationString;
    }
}
