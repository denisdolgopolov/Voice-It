package com.technopark.room.entities;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class TopicRecords {
    @Embedded
    public Topic topic;
    @Relation(
            parentColumn = "uuid",
            entityColumn = "topicUUID"
    )
    public List<Record> records;
}