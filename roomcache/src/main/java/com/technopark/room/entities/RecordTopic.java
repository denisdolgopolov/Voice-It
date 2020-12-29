package com.technopark.room.entities;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class RecordTopic {
    @Embedded
    public Record record;
    @Relation(
            parentColumn = "topicUUID",
            entityColumn = "uuid"
    )
    public List<Topic> topics;
}
