package voice.it.firebaseloadermodule.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.UUID;

import voice.it.firebaseloadermodule.R;

public class NotificationBuilder {
    private final Context context;

    private static final int  NOTIFICATION_ID = 639;

    private NotificationCompat.Builder builder;

    public NotificationBuilder(Context context) {
        this.context = context;
    }

    public void showNotification(Service service) {
        IntentManager intentManager = new IntentManager(context);
        Intent intentStop = intentManager.getActionIntent(ServiceAction.STOP);
        PendingIntent pendingStop = intentManager.getPendingIntent(intentStop);

        NotificationCompat.Action actionStop =
                new NotificationCompat.Action.Builder(null, "stop", pendingStop)
                .build();

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            String channelId = UUID.randomUUID().toString();
            createChannel(service, channelId);
            builder = new NotificationCompat.Builder(context, channelId);
        } else {
            builder = new NotificationCompat.Builder(context);
        }

        Notification notification = builder
                .setSmallIcon(R.drawable.ic_download)
                .setContentTitle("Загрузка файла")
                .addAction(actionStop)
                .build();
        service.startForeground(NOTIFICATION_ID, notification);
    }

    public void setProgress(int progress, Service service) {
        builder.setProgress(100, progress, false);
        NotificationManager notificationManager = (NotificationManager)
                service.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.notify(NOTIFICATION_ID, builder.build());

    }

    @TargetApi(26)
    private void createChannel(Service service, String channelId) {
        NotificationManager notificationManager = (NotificationManager)
                service.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelName = "voice_it";
        NotificationChannel notificationChannel = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_DEFAULT);
        assert notificationManager != null;
        notificationManager.createNotificationChannel(notificationChannel);
    }
}
