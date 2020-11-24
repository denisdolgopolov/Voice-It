package voice.it.firebaseloadermodule.cnst;

import androidx.annotation.NonNull;

public enum FirebaseFileTypes {
    RECORDS, TOPIC_LOGO_IMAGES, USER_PROFILE_IMAGES;

    @NonNull
    @Override
    public java.lang.String toString() {
        return name().toLowerCase();
    }
}
