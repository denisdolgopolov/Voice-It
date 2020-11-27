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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import com.example.repo.Record;
import com.example.repo.TestRecordsRepository;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final public class PlayerService extends MediaBrowserServiceCompat {
    private static MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
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

    public MediaSessionCompat mediaSession;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private boolean audioFocusRequested = false;
    public SimpleExoPlayer exoPlayer;
    private static final String TAG = "PLAYER_SERVICE";

    RecordLoader recordLoader = new RecordLoader() {

        //TODO
        @Override
        public Record getRecordByUUID(String UUID) {
            return TestRecordsRepository.getRecordByUUID(UUID);
        }

        @Override
        public String getAudioSourceURLByRecordUUID(String UUID) {
            return TestRecordsRepository.getUriFromRecordUUID(UUID);
        }

        @Override
        public String getImageSourceURLByUUID(String UUID) {
            return TestRecordsRepository.getImageSourceURLByUUID(UUID);
        }
    };

    public int currentItemIndex = 0;
    public MutableLiveData<List<String>> playlist = new MutableLiveData<>();

    public String getNextRecordUUID() {
        if (currentItemIndex == playlist.getValue().size() - 1)
            currentItemIndex = 0;
        else
            currentItemIndex++;
        return getCurrentRecordUUID();
    }

    public String getPreviousRecordUUID() {
        if (currentItemIndex == 0)
            currentItemIndex = playlist.getValue().size() - 1;
        else
            currentItemIndex--;
        return getCurrentRecordUUID();
    }

    public String getCurrentRecordUUID() {
        return Objects.requireNonNull(playlist.getValue()).get(currentItemIndex);
    }

    private AudioAttributes getAudioAttributes() {
        return new AudioAttributes
                .Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant")
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.createNotificationChannel(new NotificationChannel(
                    NOTIFICATION_DEFAULT_CHANNEL_ID,
                    "Player controls",
                    NotificationManagerCompat.IMPORTANCE_NONE
            ));
            audioFocusRequest = new AudioFocusRequest
                    .Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .setAcceptsDelayedFocusGain(false)
                    .setWillPauseWhenDucked(true)
                    .setAudioAttributes(getAudioAttributes())
                    .build();
        }
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        initMediaSession();
        initExoPLayer();
        registerReceiver(becomingNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(becomingNoisyReceiver);
        stopForeground(true);
        Log.d(TAG, "Clean: ");
        mediaSession.release();
        exoPlayer.release();
        mediaSession = null;
        exoPlayer = null;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new PlayerServiceBinder();
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return null;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

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

        private String currentRecordUUID = "";
        int currentState = PlaybackStateCompat.STATE_STOPPED;

        @Override
        public void onSeekTo(long pos) {
            exoPlayer.setPlayWhenReady(false);
            mediaSession.setPlaybackState(
                    stateBuilder.setState(
                            PlaybackStateCompat.STATE_BUFFERING,
                            pos,
                            1).build()
            );
            exoPlayer.seekTo(pos);
            exoPlayer.prepare();
            exoPlayer.setPlayWhenReady(true);
            mediaSession.setPlaybackState(
                    stateBuilder.setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            pos,
                            1).build()
            );

        }

        @Override
        public void onPlay() {
            if (!playlist.getValue().isEmpty()) {
                if (!exoPlayer.getPlayWhenReady()) {
                    startService(new Intent(getApplicationContext(), PlayerService.class));
                }

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

                prepareToPlay(getCurrentRecordUUID());

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

            } else {
                position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
            }
            mediaSession.setPlaybackState(
                    stateBuilder
                            .setState(
                                    PlaybackStateCompat.STATE_PAUSED,
                                    position,
                                    1
                            )
                            .build()
            );
            currentState = PlaybackStateCompat.STATE_PAUSED;
            refreshNotificationAndForegroundStatus(currentState);
        }

        @Override
        public void onStop() {
            if (exoPlayer.getPlayWhenReady()) {
                exoPlayer.setPlayWhenReady(false);

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
                    stateBuilder
                            .setState(
                                    PlaybackStateCompat.STATE_STOPPED,
                                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                                    1
                            ).build()
            );
            currentState = PlaybackStateCompat.STATE_STOPPED;
            refreshNotificationAndForegroundStatus(currentState);
            stopSelf();
        }

        @Override
        public void onSkipToNext() {
            skipTo(getNextRecordUUID());
        }

        @Override
        public void onSkipToPrevious() {
            skipTo(getPreviousRecordUUID());
        }

        private void prepareToPlay(String preparingRecordUUID) {
            if (!currentRecordUUID.equals(preparingRecordUUID)) {
                currentRecordUUID = preparingRecordUUID;
                mediaSession.setMetadata(getMediaMetadataFromUUID(currentRecordUUID));
                exoPlayer.setMediaItem(MediaItem.fromUri(recordLoader.getAudioSourceURLByRecordUUID(currentRecordUUID)));
                exoPlayer.prepare();
            } else {
                if (currentState != PlaybackStateCompat.STATE_PAUSED) {
                    exoPlayer.seekTo(0L);
                }
            }
            mediaSession.setActive(true);
            exoPlayer.setPlayWhenReady(true);
        }

        private void skipTo(String currentRecordUUID) {

            mediaSession.setPlaybackState(
                    stateBuilder.setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            0,
                            1
                    ).build()
            );
            currentState = PlaybackStateCompat.STATE_PLAYING;
            refreshNotificationAndForegroundStatus(currentState);
            prepareToPlay(currentRecordUUID);
        }
    };

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopForeground(true);
        mediaSession.release();
        exoPlayer.release();
        unregisterReceiver(becomingNoisyReceiver);
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }

    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = focusChange -> {
        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            mediaSessionCallback.onPlay();
        }
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            mediaSessionCallback.onPause();
        }
    };

    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Стопаем звук при вытаскивании наушников
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                if (mediaSessionCallback != null) {
                    mediaSessionCallback.onPause();
                }
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


    MediaMetadataCompat getMediaMetadataFromUUID(String UUID) {
        Record record = recordLoader.getRecordByUUID(UUID);
        return recordToMediaMetadataCompat(record);
    }


    public MediaMetadataCompat recordToMediaMetadataCompat(Record record) {
        // TODO убрать хардкод
        Bitmap art = BitmapFactory.decodeResource(getResources(), R.drawable.mlr_test_record_image);
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, art);
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, record.name);
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, record.topicUUID);
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, record.userUUID);
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, Long.parseLong(record.duration));
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, record.uuid);
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, recordLoader.getAudioSourceURLByRecordUUID(record.uuid));
        MediaMetadataCompat mediaMetadataCompat = metadataBuilder.build();

        return mediaMetadataCompat;

    }

    private void initExoPLayer() {
        exoPlayer = new SimpleExoPlayer.Builder(this).build();
        exoPlayer.addListener(exoPlayerListener);
    }

    private void initMediaSession() {
        mediaSession = new MediaSessionCompat(this, "PlayerService");
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        );

        mediaSession.setCallback(mediaSessionCallback);
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
    }

    interface RecordLoader {
        Record getRecordByUUID(String UUID);

        String getAudioSourceURLByRecordUUID(String UUID);

        String getImageSourceURLByUUID(String UUID);
    }
}



