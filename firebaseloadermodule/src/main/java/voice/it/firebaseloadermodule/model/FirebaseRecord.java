package voice.it.firebaseloadermodule.model;

public class FirebaseRecord extends FirebaseModel {
    private String name;
    private String topicUUID;
    private String dateOfCreation;
    private String userUUID;
    private Long duration;


    public FirebaseRecord(String uuid, String name, String topicUUID, String dateOfCreation, String userUUID, Long duration) {
        super(uuid);
        this.name = name;
        this.topicUUID = topicUUID;
        this.dateOfCreation = dateOfCreation;
        this.userUUID = userUUID;
        this.duration = duration;
    }

    public FirebaseRecord() {
        super();
    }

    public String getName() {
        return name;
    }

    public String getTopicUUID() {
        return topicUUID;
    }

    public String getDateOfCreation() {
        return dateOfCreation;
    }

    public String getUserUUID() {
        return userUUID;
    }

    public Long getDuration() {
        return duration;
    }
}
