package com.com.technoparkproject.repositories;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.com.technoparkproject.R;
import com.com.technoparkproject.models.Record;
import com.com.technoparkproject.models.Topic;
import com.com.technoparkproject.models.TopicTypes;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TestRecordsRepository {

    private static List<Record> getListRecord() {
        return Arrays.asList(
                new Record("1", "testName1", "asdsad", "29.30.3030", "sadsada", "3:33"),
                new Record("2", "testName2", "asdsad", "29.30.3030", "sadsada", "20:20"),
                new Record("3", "testName3", "asdsad", "29.30.3030", "sadsada", "1:20")
        );
    }

    static public Record getRecordByUUID(String UUID) {
        return getListRecord().get(Integer.parseInt(UUID) - 1);
    }

    static public Drawable getRecordImageByUserUUID(String UUID, Context context) {
        return ContextCompat.getDrawable(context, R.drawable.test_record_image);
    }

    static public List<Topic> getListTopics() {
        return Arrays.asList(
                new Topic(UUID.randomUUID().toString(), "Friends", UUID.randomUUID().toString(),
                        Arrays.asList("1", "2", "3"), TopicTypes.TOPIC_FRIEND),
                new Topic(UUID.randomUUID().toString(), "Dogs", UUID.randomUUID().toString(),
                        Arrays.asList("1", "2", "3"), TopicTypes.TOPIC_THEMATIC),
                new Topic(UUID.randomUUID().toString(), "Дача", UUID.randomUUID().toString(),
                        Arrays.asList("1"), TopicTypes.TOPIC_THEMATIC),
                new Topic(UUID.randomUUID().toString(), "Дом", UUID.randomUUID().toString(),
                        Arrays.asList("1", "2"), TopicTypes.TOPIC_THEMATIC),
                new Topic(UUID.randomUUID().toString(), "Кухня", UUID.randomUUID().toString(),
                        Arrays.asList("1", "2", "3"), TopicTypes.TOPIC_THEMATIC)
        );
    }
}
