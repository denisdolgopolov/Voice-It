package com.technopark.room.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        foreignKeys = @ForeignKey(
                entity = Topic.class,
                parentColumns = "uuid",
                childColumns = "topicUUID",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("topicUUID"))
public final class Record {
    @PrimaryKey
    @NonNull
    private final String uuid;
    private final String name;
    private final String topicUUID;
    private final String dateOfCreation;
    private final String userUUID;
    private final String duration;

    public Record(@NonNull String uuid, String name,
                  @NonNull String topicUUID,
                  String dateOfCreation,
                  @NonNull String userUUID,
                  String duration) {
        this.uuid = uuid;
        this.name = name;
        this.topicUUID = topicUUID;
        this.dateOfCreation = dateOfCreation;
        this.userUUID = userUUID;
        this.duration = duration;
    }

    @NonNull
    public String getUuid() {
        return uuid;
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

    public String getDuration() {
        return duration;
    }
}
