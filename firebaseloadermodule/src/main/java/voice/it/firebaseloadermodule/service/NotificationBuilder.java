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
    private final Service context;

    private static final int  NOTIFICATION_ID = 639;

    private NotificationCompat.Builder builder;

    public NotificationBuilder(Service context) {
        this.context = context;
    }

    public void showNotification(Service service) {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            String channelId = UUID.randomUUID().toString();
            createChannel(service, channelId);
            builder = new NotificationCompat.Builder(context, channelId);
        } else {
            builder = new NotificationCompat.Builder(context);
        }

        Notification notification = builder
                .setSmallIcon(R.drawable.ic_download)
                .setContentTitle("Загрузка файла: ")
                .build();
        service.startForeground(NOTIFICATION_ID, notification);
    }

    public void setProgress(int progress) {
        builder.setProgress(100, progress, false);
        builder.setColor(context.getResources().getColor(R.color.service_notification_color));
        notifyNotification();
    }

    public void setTitle(String title) {
        builder.setContentTitle(title);
        notifyNotification();
    }

    public void setButton(String text) {
        IntentManager intentManager = new IntentManager(context);
        Intent intentStop = intentManager.getActionIntent(ServiceAction.STOP);
        PendingIntent pendingStop = intentManager.getPendingIntent(intentStop);

        NotificationCompat.Action actionStop =
                new NotificationCompat.Action.Builder(null, text, pendingStop)
                        .build();
        builder.addAction(actionStop);
        notifyNotification();
    }

    private void notifyNotification() {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    @TargetApi(26)
    private void createChannel(Service service, String channelId) {
        String channelName = "voice_it";
        NotificationChannel notificationChannel = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_DEFAULT);

        NotificationManager notificationManager = (NotificationManager)
                service.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.createNotificationChannel(notificationChannel);
    }
}
