package com.com.technoparkproject.view.fragments;

import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.com.technoparkproject.R;
import com.com.technoparkproject.models.RecordUtils;
import com.com.technoparkproject.view_models.PlayerViewModel;

public class MinimizedPlayerFragment extends Fragment {
    private PlayerViewModel viewModel;
    ImageButton nextButton;
    ImageButton pauseButton;
    ImageButton playButton;
    ImageButton prevButton;
    TextView titleAudio;
    TextView authorAudio;
    SeekBar mediaPositionSeekBar;
    TextView duration;
    TextView currentTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(getActivity()).get(PlayerViewModel.class);
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
        mediaPositionSeekBar = view.findViewById(R.id.seekBar);
        currentTime = view.findViewById(R.id.current_time_tv);
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

        viewModel.currentPosition.observe(getViewLifecycleOwner(), new Observer<Long>() {
            @Override
            public void onChanged(Long aLong) {
                mediaPositionSeekBar.setProgress(aLong.intValue());
            }
        });
        mediaPositionSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                currentTime.setText(RecordUtils.durationFormatted(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                viewModel.currentPosition.removeObservers(getViewLifecycleOwner());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                viewModel.seekBarSeekTo(seekBar.getProgress());
                viewModel.currentPosition.observe(getViewLifecycleOwner(), new Observer<Long>() {
                    @Override
                    public void onChanged(Long aLong) {
                        seekBar.setProgress(aLong.intValue());
                    }
                });
            }
        });
        viewModel.currentMetadata.observe(getViewLifecycleOwner(), new Observer<MediaMetadataCompat>() {
            @Override
            public void onChanged(MediaMetadataCompat mediaMetadataCompat) {
                if (viewModel.currentMetadata.getValue() != null && viewModel.currentState.getValue() != null) {
                    updateUI(mediaMetadataCompat, viewModel.currentState.getValue());
                }
            }
        });
        viewModel.currentState.observe(getViewLifecycleOwner(), new Observer<PlaybackStateCompat>() {
            @Override
            public void onChanged(PlaybackStateCompat playbackStateCompat) {
                if (viewModel.currentMetadata.getValue() != null && playbackStateCompat != null) {
                    updateUI(viewModel.currentMetadata.getValue(), playbackStateCompat);
                }
            }
        });
        updateUI(viewModel.currentMetadata.getValue(), viewModel.currentState.getValue());
        return view;
    }

    private void updateUI(MediaMetadataCompat metadata, PlaybackStateCompat playbackStateCompat) {
        if (metadata != null && playbackStateCompat != null) {
            if (playbackStateCompat.getState() == PlaybackStateCompat.STATE_PLAYING) {
                playButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
            } else {
                pauseButton.setVisibility(View.GONE);
                playButton.setVisibility(View.VISIBLE);
            }
            titleAudio.setText(metadata.getDescription().getTitle());
            duration.setText(RecordUtils.durationFormatted(metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)));
            mediaPositionSeekBar.setMax((int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
        }
    }

}