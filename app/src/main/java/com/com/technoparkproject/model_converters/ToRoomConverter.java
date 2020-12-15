package com.com.technoparkproject.model_converters;

import com.technopark.room.entities.Record;
import com.technopark.room.entities.Topic;
import com.technopark.room.entities.TopicTypes;

import java.util.ArrayList;
import java.util.List;

public class ToRoomConverter {
    public static Topic toModel(com.com.technoparkproject.models.Topic topic) {
        return new Topic(topic.uuid,
                topic.name,
                topic.logoImageUUID,
                TopicTypes.valueOf(topic.type.name()));
    }

    public static Record toModel(com.com.technoparkproject.models.Record record) {
        return new Record(record.uuid,
                record.name,
                record.topicUUID,
                record.dateOfCreation,
                record.userUUID,
                record.duration);
    }

    public static List<Topic> toTopicList(List<com.com.technoparkproject.models.Topic> roomTopics) {
        List<Topic> result = new ArrayList<>();
        for (com.com.technoparkproject.models.Topic topic: roomTopics) {
            result.add(toModel(topic));
        }
        return result;
    }

    public static List<Record> toRecordList(List<com.com.technoparkproject.models.Record> roomRecord) {
        List<Record> result = new ArrayList<>();
        for (com.com.technoparkproject.models.Record record: roomRecord) {
            result.add(toModel(record));
        }
        return result;
    }
}
