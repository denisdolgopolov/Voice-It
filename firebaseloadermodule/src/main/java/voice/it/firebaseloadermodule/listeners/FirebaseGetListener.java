package voice.it.firebaseloadermodule.listeners;

import voice.it.firebaseloadermodule.model.FirebaseModel;

public interface FirebaseGetListener<T> {
    void onFailure(String error);
    void onGet(T item);
}
