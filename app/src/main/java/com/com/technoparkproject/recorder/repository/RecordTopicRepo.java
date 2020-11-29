package com.com.technoparkproject.recorder.repository;

import android.content.Context;

import java.util.Collection;
import java.util.UUID;

public interface RecordTopicRepo {
    Collection<RecordTopic> getRecTopics();
    UUID createRecord(String suffix);
    RecordTopic getRecord(UUID uuid);
    RecordTopic getLastRecord();
    void updateLastTopic(String topic);
    void updateLastName(String name);
    void updateLastDuration(int duration);
    void deleteLastRecord();
}
