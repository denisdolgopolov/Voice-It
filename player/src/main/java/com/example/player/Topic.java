package com.example.player;

import java.util.List;

public final class Topic {
    public final String uuid;
    public final String name;
    public final String logoImageUUID;
    public final List<String> records;
    public final String type;

    public Topic(String uuid, String name, String logoImageUUID,
                 List<String> records, String type) {
        this.uuid = uuid;
        this.name = name;
        this.logoImageUUID = logoImageUUID;
        this.records = records;
        this.type = type;
    }
}
