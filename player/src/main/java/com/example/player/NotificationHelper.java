package com.example.player;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;


public class NotificationHelper {
    public static final String CHANNEL_ID = "default_channel";
    private static final String CURRENT_FRAGMENT = "CURRENT FRAGMENT";

    static NotificationCompat.Builder from(Context context, MediaSessionCompat mediaSession) {
        MediaControllerCompat controller = mediaSession.getController();
        MediaMetadataCompat mediaMetadata = controller.getMetadata();
        MediaDescriptionCompat description = mediaMetadata.getDescription();
        String subtitle = "";
        if (mediaSession.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_BUFFERING) subtitle = "Loading...";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder
                .setContentTitle(description.getTitle())
                .setContentText(subtitle)
                .setSubText(description.getDescription())
                .setLargeIcon(description.getIconBitmap())
                .setContentIntent(controller.getSessionActivity())
                .setDeleteIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP)
                )
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        return builder;
    }

    public Notification getNotification(int playbackState, MediaSessionCompat mediaSession, PlayerService service) {
        final String FRAGMENT_PLAYLIST_NAME = service.getResources().getString(R.string.fragment_playlist_name);
        NotificationCompat.Builder builder = NotificationHelper.from(service, mediaSession);
        builder.addAction(new NotificationCompat.Action(R.drawable.ic_stop_and_close_player,
                        service.getString(R.string.stop_and_close_player),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_STOP)
                )
        );
        builder.addAction(new NotificationCompat.Action(R.drawable.ic_previous,
                        service.getString(R.string.previous),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                service,
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                )
        );
        if (playbackState == PlaybackStateCompat.STATE_PLAYING)
            builder.addAction(new NotificationCompat.Action(R.drawable.ic_pause,
                    service.getString(R.string.pause),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_PLAY_PAUSE))
            );
        else {
            builder.addAction(new NotificationCompat.Action(R.drawable.ic_play,
                    service.getString(R.string.play),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_PLAY_PAUSE))
            );
        }
        builder.addAction(new NotificationCompat.Action(R.drawable.ic_next,
                service.getString(R.string.next),
                MediaButtonReceiver.buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_SKIP_TO_NEXT))
        );


        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 2, 3, 4)
                .setShowCancelButton(true)
                .setMediaSession(mediaSession.getSessionToken())
                .setCancelButtonIntent(MediaButtonReceiver
                        .buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_STOP))
                .setMediaSession(mediaSession.getSessionToken())
        );
        builder.setSmallIcon(R.drawable.ic_notificon);
        builder.setColor(ContextCompat.getColor(service, R.color.colorRed));
        builder.setShowWhen(false);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setOnlyAlertOnce(true);
        builder.setChannelId(CHANNEL_ID);
        builder.setContentIntent(service.activityIntent);
        return builder.build();
    }
}