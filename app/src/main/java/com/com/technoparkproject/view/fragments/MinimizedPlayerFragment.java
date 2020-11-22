package com.com.technoparkproject.view.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.Looper;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.com.technoparkproject.R;
import com.com.technoparkproject.models.Record;
import com.com.technoparkproject.view_models.MainListOfRecordsViewModel;
import com.com.technoparkproject.view_models.PlayerViewModel;
import com.com.technoparkproject.view_models.PlaylistViewModel;

public class MinimizedPlayerFragment extends Fragment {
    private PlayerViewModel viewModel;
    String TAG = "PlayerFragment";
    ImageButton nextButton;
    ImageButton pauseButton;
    ImageButton playButton;
    ImageButton prevButton;
    TextView titleAudio;
    TextView authorAudio;
    SeekBar seekBar;
    TextView duration;
    TextView currentTime;
    boolean updatePosition = true;
    Handler handler = new Handler(Looper.getMainLooper());
    private static long POSITION_UPDATE_INTERVAL_MILLIS = 1000L;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(
                this,
                new PlayerViewModel.Factory(getActivity())
        ).get(PlayerViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_minimized_player, container, false);
        pauseButton = view.findViewById(R.id.pause_button);
        nextButton = view.findViewById(R.id.next_button);
        prevButton = view.findViewById(R.id.prev_button);
        playButton = view.findViewById(R.id.play_button);
        titleAudio = view.findViewById(R.id.audio_title_tv);
        authorAudio = view.findViewById(R.id.audio_author_tv);
        seekBar = view.findViewById(R.id.seekBar);
        currentTime = view.findViewById(R.id.current_time_tv);
        // TODO СДЕЛАТЬ НОРМАЛЬНО ЧЕРЕЗ ЛАЙВДАТУ
        duration = view.findViewById(R.id.duration_tv);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.playButtonClicked();
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.pauseButtonClicked();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.nextButtonClicked();
            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.prevButtonClicked();
            }
        });
        viewModel.currentMetadata.observe(getViewLifecycleOwner(), new Observer<MediaMetadataCompat>() {
            @Override
            public void onChanged(MediaMetadataCompat mediaMetadataCompat) {
                if (viewModel.currentMetadata.getValue() != null && viewModel.currentState.getValue() != null) {
                    if (viewModel.currentState.getValue().getState() == PlaybackStateCompat.STATE_PLAYING) {
                        updatePosition = true;
                    } else {
                        updatePosition = false;
                    }
                    updateUI(mediaMetadataCompat, viewModel.currentState.getValue());
                }
            }
        });
        viewModel.currentState.observe(getViewLifecycleOwner(), new Observer<PlaybackStateCompat>() {
            @Override
            public void onChanged(PlaybackStateCompat playbackStateCompat) {
                if (viewModel.currentMetadata.getValue() != null && playbackStateCompat != null) {
                    if (playbackStateCompat.getState() == PlaybackStateCompat.STATE_PLAYING) {
                        updatePosition = true;
                        playButton.setVisibility(View.GONE);
                        Log.d(TAG, "updateUI: playing");
                        pauseButton.setVisibility(View.VISIBLE);

                    } else {
                        updatePosition = false;
                        pauseButton.setVisibility(View.GONE);
                        playButton.setVisibility(View.VISIBLE);
                    }
                    updateUI(viewModel.currentMetadata.getValue(), playbackStateCompat);
                }
            }
        });
        return view;
    }

    private void updateUI(MediaMetadataCompat metadata, PlaybackStateCompat playbackStateCompat) {
        if (metadata != null && playbackStateCompat != null) {
            titleAudio.setText(metadata.getDescription().getTitle());
            authorAudio.setText(metadata.getDescription().getDescription());
            duration.setText(String.valueOf(metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)));
            seekBar.setMax((int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
        }
    }
}