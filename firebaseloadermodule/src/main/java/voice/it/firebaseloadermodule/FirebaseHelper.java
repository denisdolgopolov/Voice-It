package voice.it.firebaseloadermodule;

import voice.it.firebaseloadermodule.cnst.FirebaseCollections;
import voice.it.firebaseloadermodule.model.FirebaseModel;
import voice.it.firebaseloadermodule.model.FirebaseRecord;
import voice.it.firebaseloadermodule.model.FirebaseTopic;
import voice.it.firebaseloadermodule.model.FirebaseUser;

public class FirebaseHelper {
    public static String getCollectionNameByModel(FirebaseModel item) {
        if (item instanceof FirebaseTopic) return FirebaseCollections.Topics.toString();
        if (item instanceof FirebaseRecord) return FirebaseCollections.Records.toString();
        if (item instanceof FirebaseUser) return FirebaseCollections.Users.toString();
        throw new IllegalArgumentException();
    }
}
