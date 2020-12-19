package com.com.technoparkproject.view.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
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
    public static final String TAG = "PLAYLISTFRAGMENTTAG";
    PlaylistAdapter playlistAdapter = new PlaylistAdapter();
    PlaylistViewModel playlistViewModel;
    RecyclerView playlistRecyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setToolbar(getString(R.string.fragment_playlist_name));
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        playlistViewModel = new ViewModelProvider(getActivity()).get(PlaylistViewModel.class);

        playlistRecyclerView = view.findViewById(R.id.playlist_recycler_view);
        playlistRecyclerView.setAdapter(playlistAdapter);

        getChildFragmentManager().beginTransaction().replace(R.id.minimized_player, new MinimizedPlayerFragment()).commit();

        View playerView = view.findViewById(R.id.minimized_player);
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(playerView);
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
                            behavior.setHideable(false);
                            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            playerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                            playlistRecyclerView.setPadding(playlistRecyclerView.getPaddingStart(), playlistRecyclerView.getPaddingTop(), playlistRecyclerView.getPaddingEnd(), playerView.getMeasuredHeight() + 8);
                        }
                    } else {
                        if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                            behavior.setHideable(true);
                            behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                            playlistRecyclerView.setPaddingRelative(playlistRecyclerView.getPaddingStart(), playlistRecyclerView.getPaddingTop(), playlistRecyclerView.getPaddingEnd(), 8);
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
                            holder.itemView.setBackgroundColor(getResources().getColor(R.color.selected_record_color));
                        } else {
                            holder.itemView.setBackgroundColor(getResources().getColor(R.color.mainBackgroundColor));
                        }
                    } else {
                        holder.itemView.setBackgroundColor(getResources().getColor(R.color.mainBackgroundColor));
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
