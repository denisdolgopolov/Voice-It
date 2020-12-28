package voice.it.firebaseloadermodule.model;

import java.util.List;

public class FirebaseUser extends FirebaseModel {
    private List<String> records;
    private String profileImageUUID;
    private String userName;
    private String userAuthUUID;


    public FirebaseUser(String userName, List<String> records, String profileImageUUID, String userAuthUUID) {
        super(userAuthUUID, userName);
        this.records = records;
        this.profileImageUUID = profileImageUUID;
        this.userName = userName;
        this.userAuthUUID = userAuthUUID;
    }

    public FirebaseUser() { super(); }

    public List<String> getRecords() { return records; }
    public String getProfileImageUUID() {
        return profileImageUUID;
    }
    public String getUserName() { return userName; }
    public String getUserAuthUUID() { return userAuthUUID; }
}
