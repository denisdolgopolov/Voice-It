package voice.it.firebaseloadermodule.model;

import java.util.List;

public class FirebaseUser extends FirebaseModel {
    private String name;
    private List<String> records;
    private String profileImageUUID;


    public FirebaseUser(String uuid, String name, List<String> records, String profileImageUUID) {
        super(uuid);
        this.name = name;
        this.records = records;
        this.profileImageUUID = profileImageUUID;
    }

    public FirebaseUser() {
        super();
    }


    public String getName() {
        return name;
    }

    public List<String> getRecords() {
        return records;
    }

    public String getProfileImageUUID() {
        return profileImageUUID;
    }
}
