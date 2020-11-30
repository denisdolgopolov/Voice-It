package com.technopark.recorder.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.technopark.recorder.R;

public final class RecorderNotification {

    private RecorderNotification(){}

    private static final String CHANNEL_AUDIO_APP = "Voice-it channel";

    public static void createNotificationChannel(Context serviceCtx) {
        //notification channel is needed for API >= 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationManager notificationManager = serviceCtx.getSystemService(NotificationManager.class);
            if (notificationManager.getNotificationChannel(CHANNEL_AUDIO_APP)!=null)
                return;
            CharSequence name = serviceCtx.getString(R.string.channel_audio_app);
            String description = serviceCtx.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_AUDIO_APP, name, importance);
            channel.setDescription(description);
            channel.setSound(null,null);//disable sounds for this channel
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void updateNotification(String text, Context serviceCtx, int foregroundId){
        Notification recordNotification = buildForegroundNotification(text, serviceCtx);
        NotificationManager notificationManager =
                (NotificationManager)serviceCtx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(foregroundId,recordNotification);

    }

    public static Notification buildForegroundNotification(String text, Context serviceCtx) {
        NotificationCompat.Builder builder=
                new NotificationCompat.Builder(serviceCtx, CHANNEL_AUDIO_APP);

        //disable sound using builder if API < 26
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            builder.setSound(null);
        }

        builder.setOngoing(true)
                .setContentTitle(serviceCtx.getString(R.string.audio_record))
                .setContentText(text)
                .setSmallIcon(R.drawable.service_notification_icon)
                .setColor(serviceCtx.getResources().getColor(R.color.service_notification_color));

        PackageManager pm = serviceCtx.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(serviceCtx.getPackageName());
        if (intent == null)
            Log.e(RecorderNotification.class.getSimpleName(),"failed to create intent for main activity");
        intent.putExtra(RecordIntentConstants.NAME, RecordIntentConstants.VALUE);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(serviceCtx,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        return(builder.build());
    }
}
