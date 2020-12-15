package com.com.technoparkproject.model_converters;

import android.util.ArrayMap;

import com.com.technoparkproject.models.Record;
import com.com.technoparkproject.models.Topic;
import com.com.technoparkproject.models.TopicTypes;

import java.util.ArrayList;
import java.util.List;

public final class FromRoomConverter {
    private FromRoomConverter(){}
    public static Topic toModel(com.technopark.room.entities.Topic roomTopic) {
        return new Topic(roomTopic.getUuid(),
                roomTopic.getName(),
                roomTopic.getLogoImageUUID(),
                new ArrayList<>(),
                TopicTypes.valueOf(roomTopic.getType().name()));
    }

    public static Record toModel(com.technopark.room.entities.Record roomRecord) {
        return new Record(roomRecord.getUuid(),
                roomRecord.getName(),
                roomRecord.getTopicUUID(),
                roomRecord.getDateOfCreation(),
                roomRecord.getUserUUID(),
                roomRecord.getDuration());
    }

    public static List<Topic> toTopicList(List<com.technopark.room.entities.Topic> roomTopics) {
        List<Topic> result = new ArrayList<>();
        for (com.technopark.room.entities.Topic topic: roomTopics) {
            result.add(toModel(topic));
        }
        return result;
    }

    public static List<Record> toRecordList(List<com.technopark.room.entities.Record> roomRecord) {
        List<Record> result = new ArrayList<>();
        for (com.technopark.room.entities.Record record: roomRecord) {
            result.add(toModel(record));
        }
        return result;
    }

    public static ArrayMap<Topic,List<Record>> toTopicRecords(List<com.technopark.room.entities.TopicRecords> roomTopicRecords) {
        ArrayMap<Topic,List<Record>> topicRecords = new ArrayMap<>();
        for (com.technopark.room.entities.TopicRecords topicRecs: roomTopicRecords) {
            topicRecords.put(toModel(topicRecs.topic),toRecordList(topicRecs.records));
        }
        return topicRecords;
    }
}
