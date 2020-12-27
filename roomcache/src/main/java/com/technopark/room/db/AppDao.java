package com.technopark.room.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.technopark.room.entities.Record;
import com.technopark.room.entities.RecordTopic;
import com.technopark.room.entities.Topic;
import com.technopark.room.entities.TopicRecords;

import java.util.List;

@Dao
public interface AppDao {

    @Query("SELECT * FROM Record WHERE topicUUID=:topicUUID")
    LiveData<List<Record>> getRecordsByTopic(String topicUUID);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRecords(List<Record> records);

    @Query("SELECT * FROM Topic")
    LiveData<List<Topic>> getAllTopics();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTopics(List<Topic> topics);

    @Transaction
    @Query("SELECT * FROM Topic")
    LiveData<List<TopicRecords>> getAllTopicRecords();

    @Transaction
    @Query("SELECT * FROM Record WHERE userUUID=:userUUID")
    LiveData<List<RecordTopic>> getAllTopicRecordsByUser(String userUUID);
}
