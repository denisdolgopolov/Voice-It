package voice.it.firebaseloadermodule.listeners;

import voice.it.firebaseloadermodule.model.FirebaseModel;

public interface FirebaseGetListener<T extends FirebaseModel> {
    void onFailure(String error);
    void onGet(T item);
}