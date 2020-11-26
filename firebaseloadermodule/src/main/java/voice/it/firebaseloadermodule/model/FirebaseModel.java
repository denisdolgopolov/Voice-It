package voice.it.firebaseloadermodule.model;

import java.io.Serializable;

public class FirebaseModel implements Serializable {
    private String uuid;

    public FirebaseModel(String uuid) {
        this.uuid = uuid;
    }

    public FirebaseModel() {

    }


    public String getUuid() {
        return uuid;
    }

}
