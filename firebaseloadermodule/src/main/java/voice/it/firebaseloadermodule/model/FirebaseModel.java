package voice.it.firebaseloadermodule.model;

import java.io.Serializable;

public class FirebaseModel implements Serializable {
    private String uuid;
    private String name;

    public FirebaseModel(String uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public FirebaseModel() {

    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }
}
