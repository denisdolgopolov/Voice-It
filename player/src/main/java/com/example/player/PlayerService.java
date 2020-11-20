package com.example.player;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.session.MediaButtonReceiver;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.android.exoplayer2.ExoPlayerLibraryInfo.TAG;

final public class PlayerService extends Service {
    private final int NOTIFICATION_ID = 404;
    private final String NOTIFICATION_DEFAULT_CHANNEL_ID = "default_channel";
    private final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder().setActions(
            PlaybackStateCompat.ACTION_PLAY
                    | PlaybackStateCompat.ACTION_STOP
                    | PlaybackStateCompat.ACTION_PAUSE
                    | PlaybackStateCompat.ACTION_PLAY_PAUSE
                    | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
    );

    private MediaSessionCompat mediaSession;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private boolean audioFocusRequested = false;
    private SimpleExoPlayer exoPlayer;

    public void setPlaylistAndCurrentItemNumber(List<MediaMetadataCompat> playlist, int number) {
        this.playlist = playlist;
        this.maxIndex = playlist.size() - 1;
        this.currentItemIndex = number;
    }

    public List<MediaMetadataCompat> playlist = new ArrayList<>();
    public int currentItemIndex = 0;
    public int maxIndex;

    public MediaMetadataCompat getNext() {
        if (currentItemIndex == maxIndex)
            currentItemIndex = 0;
        else
            currentItemIndex++;
        return getCurrent();
    }

    public MediaMetadataCompat getPrevious() {
        if (currentItemIndex == 0)
            currentItemIndex = maxIndex;
        else
            currentItemIndex--;
        return getCurrent();
    }

    public MediaMetadataCompat getCurrent() {
        return playlist.get(currentItemIndex);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant")
            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTIFICATION_DEFAULT_CHANNEL_ID,
                    "Player controls",
                    NotificationManagerCompat.IMPORTANCE_NONE
            );
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .setAcceptsDelayedFocusGain(false)
                    .setWillPauseWhenDucked(true)
                    .setAudioAttributes(audioAttributes)
                    .build();
        }
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaSession = new MediaSessionCompat(this, "PlayerService");
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        );
        mediaSession.setCallback(mediaSessionCallback);
        Context appContext = getApplicationContext();
        PendingIntent activityIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                getPackageManager().getLaunchIntentForPackage(getPackageName()),
                0
        );
        mediaSession.setSessionActivity(activityIntent);
        Intent mediaButtonIntent = new Intent(
                Intent.ACTION_MEDIA_BUTTON,
                null,
                getApplicationContext(),
                MediaButtonReceiver.class
        );
        mediaSession.setMediaButtonReceiver(
                PendingIntent.getBroadcast(getApplicationContext(), 0, mediaButtonIntent, 0)
        );
        exoPlayer = new SimpleExoPlayer.Builder(this).build();
        exoPlayer.addListener(exoPlayerListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaSession.release();
        exoPlayer.release();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new PlayerServiceBinder();
    }

    public class PlayerServiceBinder extends Binder {
        public MediaSessionCompat.Token getMediaSessionToken() {
            return mediaSession.getSessionToken();
        }

        public PlayerService getServiceInstance() {
            return PlayerService.this;
        }
    }

    private MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {

        private Uri currentUri;
        int currentState = PlaybackStateCompat.STATE_STOPPED;

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            super.onPlayFromUri(uri, extras);
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);
        }

        @Override
        public void onPlay() {
            if (!playlist.isEmpty()) {
                if (!exoPlayer.getPlayWhenReady()) {
                    startService(new Intent(getApplicationContext(), PlayerService.class));
                }
                MediaMetadataCompat mediaMetadataCompat = getCurrent();
                MediaSessionCompat.QueueItem item = new MediaSessionCompat.QueueItem(mediaMetadataCompat.getDescription(), 1);
                mediaSession.setMetadata(mediaMetadataCompat);
                prepareToPlay(mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI));
                if (!audioFocusRequested) {
                    audioFocusRequested = true;
                    int audioFocusResult;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        audioFocusResult = audioManager.requestAudioFocus(audioFocusRequest);
                    } else {
                        audioFocusResult = audioManager.requestAudioFocus(
                                audioFocusChangeListener,
                                AudioManager.STREAM_MUSIC,
                                AudioManager.AUDIOFOCUS_GAIN);
                    }
                    if (audioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                        return;
                }

                mediaSession.setActive(true);
                registerReceiver(becomingNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
                exoPlayer.setPlayWhenReady(true);

                if (currentState == PlaybackStateCompat.STATE_PAUSED) {
                    long position = exoPlayer.getCurrentPosition();
                    mediaSession.setPlaybackState(
                            stateBuilder.setState(
                                    PlaybackStateCompat.STATE_PLAYING,
                                    position, 1).build()
                    );
                } else {
                    mediaSession.setPlaybackState(
                            stateBuilder.setState(
                                    PlaybackStateCompat.STATE_PLAYING,
                                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                                    1).build()
                    );
                }
                currentState = PlaybackStateCompat.STATE_PLAYING;
                refreshNotificationAndForegroundStatus(currentState);
            }
        }

        @Override
        public void onPause() {
            long position;
            if (exoPlayer.getPlayWhenReady()) {
                position = exoPlayer.getCurrentPosition();
                exoPlayer.setPlayWhenReady(false);
                unregisterReceiver(becomingNoisyReceiver);
            } else {
                position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
            }
            mediaSession.setPlaybackState(
                    stateBuilder.setState(
                            PlaybackStateCompat.STATE_PAUSED,
                            position,
                            1).build()
            );
            currentState = PlaybackStateCompat.STATE_PAUSED;
            refreshNotificationAndForegroundStatus(currentState);
        }

        @Override
        public void onStop() {
            if (exoPlayer.getPlayWhenReady()) {
                exoPlayer.setPlayWhenReady(false);
                unregisterReceiver(becomingNoisyReceiver);
            }
            if (audioFocusRequested) {
                audioFocusRequested = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    audioManager.abandonAudioFocusRequest(audioFocusRequest);
                } else {
                    audioManager.abandonAudioFocus(audioFocusChangeListener);
                }
            }

            mediaSession.setActive(false);
            mediaSession.setPlaybackState(
                    stateBuilder.setState(
                            PlaybackStateCompat.STATE_STOPPED,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                            1).build()
            );
            currentState = PlaybackStateCompat.STATE_STOPPED;
            refreshNotificationAndForegroundStatus(currentState);
            stopSelf();
        }

        @Override
        public void onSkipToNext() {
            MediaMetadataCompat mediaMetadataCompat = getNext();
            mediaSession.setMetadata(mediaMetadataCompat);
            mediaSession.setPlaybackState(
                    stateBuilder.setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            0,
                            1).build()
            );
            currentState = PlaybackStateCompat.STATE_PLAYING;
            refreshNotificationAndForegroundStatus(currentState);
            prepareToPlay(mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI));
        }

        @Override
        public void onSkipToPrevious() {
            MediaMetadataCompat mediaMetadataCompat = getPrevious();
            mediaSession.setMetadata(mediaMetadataCompat);
            mediaSession.setPlaybackState(
                    stateBuilder.setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            0,
                            1).build()
            );
            currentState = PlaybackStateCompat.STATE_PLAYING;
            refreshNotificationAndForegroundStatus(currentState);
            prepareToPlay(mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI));
        }

        private void prepareToPlay(String uri) {
            if (!Uri.parse(uri).equals(currentUri)) {
                currentUri = Uri.parse(uri);
                exoPlayer.setMediaItem(MediaItem.fromUri(currentUri));
                exoPlayer.prepare();
            }
        }
    };

    @Override
    public void onTaskRemoved(Intent rootIntent) {

        super.onTaskRemoved(rootIntent);
    }

    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            Log.d(TAG, "onAudioFocusChange");
            if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                mediaSessionCallback.onPlay();
                // TODO WTF DON'T WORKING
                Log.d(TAG, "onAudioFocusChange: GAIN");
            }
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                Log.d(TAG, "onAudioFocusChange: LOSS");
                mediaSessionCallback.onPause();
            }
        }
    };

    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Стопаем звук при вытаскивании наушников
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                mediaSessionCallback.onPause();
            }
        }
    };

    private Player.EventListener exoPlayerListener = new Player.EventListener() {

        @Override
        public void onPlaybackStateChanged(int state) {
            if (state == ExoPlayer.STATE_ENDED) {
                mediaSessionCallback.onSkipToNext();
            }
        }
    };

    private void refreshNotificationAndForegroundStatus(int playbackState) {
        NotificationHelper notificationHelper = new NotificationHelper();
        switch (playbackState) {
            case PlaybackStateCompat.STATE_PLAYING:
            case PlaybackStateCompat.STATE_PAUSED: {
                startForeground(NOTIFICATION_ID, notificationHelper.getNotification(playbackState, mediaSession, this));
                break;
            }
            default: {
                stopForeground(true);
                break;
            }
        }
    }
}



