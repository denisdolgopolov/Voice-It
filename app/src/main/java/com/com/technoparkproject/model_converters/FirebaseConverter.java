package com.com.technoparkproject.model_converters;

import com.com.technoparkproject.models.Record;
import com.com.technoparkproject.models.Topic;
import com.com.technoparkproject.models.TopicTypes;

import java.util.ArrayList;
import java.util.List;

import voice.it.firebaseloadermodule.model.FirebaseRecord;
import voice.it.firebaseloadermodule.model.FirebaseTopic;

public class FirebaseConverter {
    public Topic toModel(FirebaseTopic firebaseTopic) {
        TopicTypes type;
        if(firebaseTopic.getType().equals(TopicTypes.TOPIC_FRIEND.toString())) {
            type = TopicTypes.TOPIC_FRIEND;
        } else {
            type = TopicTypes.TOPIC_THEMATIC;
        }
        return new Topic(firebaseTopic.getUuid(),
                firebaseTopic.getName(),
                firebaseTopic.getLogoImageUUID(),
                firebaseTopic.getRecords(),
                type);
    }

    public Record toModel(FirebaseRecord firebaseRecord) {
        return new Record(firebaseRecord.getUuid(),
                firebaseRecord.getName(),
                firebaseRecord.getTopicUUID(),
                firebaseRecord.getDateOfCreation(),
                firebaseRecord.getUserUUID(),
                firebaseRecord.getDuration().toString());
    }

    public List<Topic> toTopicList(List<FirebaseTopic> list) {
        List<Topic> result = new ArrayList<>();
        for (FirebaseTopic topic: list) {
            result.add(toModel(topic));
        }
        return result;
    }

    public List<Record> toRecordList(List<FirebaseRecord> list) {
        List<Record> result = new ArrayList<>();
        for (FirebaseRecord record: list) {
            result.add(toModel(record));
        }
        return result;
    }
}
