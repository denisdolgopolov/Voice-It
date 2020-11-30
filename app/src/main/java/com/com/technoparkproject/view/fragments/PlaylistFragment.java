package com.com.technoparkproject.view.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.com.technoparkproject.R;
import com.com.technoparkproject.models.Record;
import com.com.technoparkproject.view.activities.MainActivity;
import com.com.technoparkproject.view.adapters.main_list_records.ItemListRecordsViewHolder;
import com.com.technoparkproject.view_models.PlaylistViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.List;

public class PlaylistFragment extends Fragment {
    PlaylistAdapter playlistAdapter = new PlaylistAdapter();
    PlaylistViewModel playlistViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setToolbar(getString(R.string.fragment_playlist_name));
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        playlistViewModel = new ViewModelProvider(getActivity()).get(PlaylistViewModel.class);

        RecyclerView PlaylistRecyclerView = view.findViewById(R.id.playlist_recycler_view);
        PlaylistRecyclerView.setAdapter(playlistAdapter);

        getChildFragmentManager().beginTransaction().replace(R.id.minimized_player, new MinimizedPlayerFragment()).commit();

        View playerView = view.findViewById(R.id.minimized_player);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(playerView);
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        playlistViewModel.currentPlaylist.observe(getViewLifecycleOwner(), new Observer<List<Record>>() {
            @Override
            public void onChanged(List<Record> records) {
                playlistAdapter.setRecordList(records);
            }
        });

        playlistViewModel.currentState.observe(getViewLifecycleOwner(), new Observer<PlaybackStateCompat>() {
            @Override
            public void onChanged(PlaybackStateCompat playbackStateCompat) {
                if (playbackStateCompat != null) {
                    if (playbackStateCompat.getState() != PlaybackStateCompat.STATE_STOPPED) {
                        if (behavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            PlaylistRecyclerView.setPaddingRelative(PlaylistRecyclerView.getPaddingStart(), PlaylistRecyclerView.getPaddingTop(), PlaylistRecyclerView.getPaddingEnd(), playerView.getHeight() + 10);
                            behavior.setHideable(false);
                        }
                    } else {
                        if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                            behavior.setHideable(true);
                            behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                            PlaylistRecyclerView.setPaddingRelative(PlaylistRecyclerView.getPaddingStart(), PlaylistRecyclerView.getPaddingTop(), PlaylistRecyclerView.getPaddingEnd(), 10);
                        }
                    }
                }

            }
        });
        return view;
    }

    public class PlaylistAdapter extends RecyclerView.Adapter<ItemListRecordsViewHolder> {

        List<Record> recordList = new ArrayList<>();

        @NonNull
        @Override
        public ItemListRecordsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_of_list_records, parent, false);
            return new ItemListRecordsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemListRecordsViewHolder holder, int position) {
            Record record = recordList.get(position);
            holder.bindViewHolder(null, record);

            playlistViewModel.currentMetadata.observe(getViewLifecycleOwner(), new Observer<MediaMetadataCompat>() {
                @Override
                public void onChanged(MediaMetadataCompat mediaMetadataCompat) {
                    if (playlistViewModel.currentMetadata.getValue() != null) {
                        if (record.uuid.equals(playlistViewModel.currentMetadata.getValue().getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID))) {
                            holder.itemView.setBackgroundColor(Color.parseColor(getString(R.string.selected_record_color)));
                        } else {
                            holder.itemView.setBackgroundColor(Color.WHITE);
                        }
                    }
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playlistViewModel.itemClicked(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return recordList.size();
        }

        public void setRecordList(List<Record> recordArrayList) {
            this.recordList = recordArrayList;
            notifyDataSetChanged();
        }
    }

}
