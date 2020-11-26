package voice.it.firebaseloadermodule.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import voice.it.firebaseloadermodule.FirebaseFileLoader;
import voice.it.firebaseloadermodule.FirebaseLoader;
import voice.it.firebaseloadermodule.cnst.FileLoadState;
import voice.it.firebaseloadermodule.listeners.FirebaseListener;
import voice.it.firebaseloadermodule.model.FirebaseModel;

public class ServiceLoadFileState extends Service {
    private NotificationBuilder builder;

    @Override
    public void onCreate() {
        super.onCreate();

        builder = new NotificationBuilder(this);
        builder.showNotification(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getAction() == null) return START_REDELIVER_INTENT;

        switch(intent.getAction()) {
            case ServiceAction.PROGRESS:
                int progress = intent.getIntExtra(FileLoadState.PROGRESS, 0);
                builder.setProgress(progress, this);
                break;
            case ServiceAction.FAILED:
                builder.setProgress(0, this);
                break;
            case ServiceAction.SUCCESS:
                builder.setProgress(100, this);

                FirebaseModel item = (FirebaseModel)
                        intent.getSerializableExtra(FileLoadState.COMPLETED);
                new FirebaseLoader().add(item, new FirebaseListener() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(String error) {

                    }
                });
                break;
            case ServiceAction.STOP:
                new FirebaseFileLoader(this).stopUpload();
                break;
        }

        return START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
