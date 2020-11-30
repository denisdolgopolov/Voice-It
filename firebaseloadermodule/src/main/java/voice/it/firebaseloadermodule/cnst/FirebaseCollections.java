package voice.it.firebaseloadermodule.cnst;

import androidx.annotation.NonNull;

public enum FirebaseCollections {
    Records(), Topics(), Users();

    @NonNull
    @Override
    public java.lang.String toString() {
        return name().toLowerCase();
    }
}
