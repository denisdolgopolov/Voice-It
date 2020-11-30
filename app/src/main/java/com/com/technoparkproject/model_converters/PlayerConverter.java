package com.com.technoparkproject.model_converters;

import com.example.player.Record;

public class PlayerConverter {
    public static Record toPlayerRecord(com.com.technoparkproject.models.Record record) {
        return new Record(record.uuid, record.name, record.topicUUID,
                record.dateOfCreation, record.userUUID, record.duration);
    }

    public static com.com.technoparkproject.models.Record toMainRecord(Record record) {
        return new com.com.technoparkproject.models.Record(record.uuid, record.name, record.topicUUID,
                record.dateOfCreation, record.userUUID, record.duration);
    }
}
