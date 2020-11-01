package com.com.technoparkproject.models;

public final class Record {
    public final String UUID;
    public final String name;
    public final String topicUUID;
    public final String dateOfCreation;
    public final String userUUID;
    public final String length;

    public Record(String UUID, String name,
                  String topicUUID,
                  String dateOfCreation,
                  String userUUID,
                  String length) {
        this.UUID = UUID;
        this.name = name;
        this.topicUUID = topicUUID;
        this.dateOfCreation = dateOfCreation;
        this.userUUID = userUUID;
        this.length = length;
    }
}
