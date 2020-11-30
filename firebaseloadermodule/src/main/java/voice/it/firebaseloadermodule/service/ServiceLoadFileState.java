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
    private FirebaseModel firebaseModel;

    @Override
    public void onCreate() {
        super.onCreate();

        builder = new NotificationBuilder(this);
        builder.showNotification(this);
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if(intent.getAction() == null) return START_REDELIVER_INTENT;

        switch(intent.getAction()) {
            case ServiceAction.GET_ITEM:
                firebaseModel = (FirebaseModel)
                        intent.getSerializableExtra(FileLoadState.COMPLETED);

                assert firebaseModel != null;

                builder.setTitle(firebaseModel.getName());
                builder.setButton("stop");
                break;
            case ServiceAction.PROGRESS:
                int progress = intent.getIntExtra(FileLoadState.PROGRESS, 0);
                builder.setProgress(progress);
                break;
            case ServiceAction.FAILED:
                builder.setProgress(0);
                builder.setTitle("load failed");
                builder.setButton("ok");
                break;
            case ServiceAction.SUCCESS:
                builder.setProgress(100);
                builder.setTitle("load success");
                builder.setButton("ok");

                assert firebaseModel != null;
                new FirebaseLoader().add(firebaseModel, new FirebaseListener() {
                    @Override
                    public void onSuccess() {
                        Intent newIntent = new Intent("update_list_records");
                        sendBroadcast(newIntent);
                    }

                    @Override
                    public void onFailure(String error) {

                    }
                });
                break;
            case ServiceAction.STOP:
                assert firebaseModel != null;
                new FirebaseFileLoader(this).stopUpload(firebaseModel);
                stopSelf();
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
