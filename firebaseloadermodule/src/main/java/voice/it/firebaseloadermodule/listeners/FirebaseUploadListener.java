package voice.it.firebaseloadermodule.listeners;

public interface FirebaseUploadListener {
    void onFailure(String error);
    void onSuccess();
    void onProgress(int progress);
}
