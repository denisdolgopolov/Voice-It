package com.com.technoparkproject.models;

import java.util.List;

public final class Topic {
    public final String uuid;
    public final String name;
    public final String logoImageUUID;
    public final List<String> records;
    public final TopicTypes type;

    public Topic(String uuid, String name, String logoImageUUID,
                 List<String> records, TopicTypes type) {
        this.uuid = uuid;
        this.name = name;
        this.logoImageUUID = logoImageUUID;
        this.records = records;
        this.type = type;
    }
}
