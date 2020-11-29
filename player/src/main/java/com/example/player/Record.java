package com.example.player;

public final class Record {
    public final String uuid;
    public final String name;
    public final String topicUUID;
    public final String dateOfCreation;
    public final String userUUID;
    public final String duration;

    public Record(String uuid, String name,
                  String topicUUID,
                  String dateOfCreation,
                  String userUUID,
                  String duration) {
        this.uuid = uuid;
        this.name = name;
        this.topicUUID = topicUUID;
        this.dateOfCreation = dateOfCreation;
        this.userUUID = userUUID;
        this.duration = duration;
    }
}
