package voice.it.firebaseloadermodule.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class IntentManager {
    private final Context context;

    public IntentManager(Context context) {
        this.context = context;
    }

    public Intent getActionIntent(String action) {
        Intent intent = new Intent(context, ServiceLoadFileState.class);
        intent.setAction(action);
        return intent;
    }


    public PendingIntent getPendingIntent(Intent intent) {
        return PendingIntent.getService(context,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT);
    }

    public void sendIntent(Intent intent) {
        if (isPreAndroidO())
            context.startService(intent);
        else
            context.startForegroundService(intent);
    }

    private Boolean isPreAndroidO() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O;
    }

    ;
}
