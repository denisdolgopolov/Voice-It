package voice.it.firebaseloadermodule.listeners;

import java.util.List;

import voice.it.firebaseloadermodule.model.FirebaseModel;

public interface FirebaseGetListListener<T extends FirebaseModel> {
    void onFailure(String error);
    void onGet(List<T> item);
}
