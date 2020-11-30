package voice.it.firebaseloadermodule.listeners;

import android.net.Uri;

public interface FirebaseGetUriListener {
    void onGet(Uri uri);
    void onFailure(String error);
}
