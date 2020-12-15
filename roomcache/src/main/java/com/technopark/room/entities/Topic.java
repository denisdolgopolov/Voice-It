package com.technopark.room.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public final class Topic {
    @PrimaryKey
    @NonNull
    private final String uuid;
    private final String name;
    private final String logoImageUUID;
    private final TopicTypes type;

    public Topic(@NonNull String uuid, String name,
                 @NonNull String logoImageUUID, TopicTypes type) {
        this.uuid = uuid;
        this.name = name;
        this.logoImageUUID = logoImageUUID;
        this.type = type;
    }

    @NonNull
    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getLogoImageUUID() {
        return logoImageUUID;
    }

    public TopicTypes getType() {
        return type;
    }
}
