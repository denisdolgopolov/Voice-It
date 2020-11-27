package voice.it.firebaseloadermodule.model;

import java.util.List;

public class FirebaseTopic extends FirebaseModel {
    private String logoImageUUID;
    private List<String> records;
    private String type;

    public FirebaseTopic(String name, String logoImageUUID, List<String> records, String type, String uuid) {
        super(uuid, name);
        this.logoImageUUID = logoImageUUID;
        this.records = records;
        this.type = type;
    }

    public FirebaseTopic() {
        super();
    }

    public String getLogoImageUUID() {
        return logoImageUUID;
    }

    public List<String> getRecords() {
        return records;
    }

    public String getType() {
        return type;
    }


}
