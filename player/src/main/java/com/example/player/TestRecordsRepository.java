package com.example.player;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TestRecordsRepository {

    public static final String SOURCE1_URL = "https://storage.googleapis.com/uamp/The_Kyoto_Connection_-_Wake_Up/03_-_Voyage_I_-_Waterfall.mp3";
    private static final String SOURCE2_URL = "https://storage.googleapis.com/uamp/The_Kyoto_Connection_-_Wake_Up/02_-_Geisha.mp3";
    private static final String SOURCE3_URL = "https://freemusicarchive.org/track/Peculate_-_There_Are_No_Angels_-_09_The_Immediate_Task_1053/download";
    private static final String SOURCE4_URL = "https://freemusicarchive.org/track/Peculate_-_There_Are_No_Angels_-_07_Vale_of_Tears_1866/download";

    public static List<Record> getListRecord() {
        return Arrays.asList(
                new Record(SOURCE1_URL,
                        "RECORD #1",
                        "TopicUUID",
                        "29.30.3030",
                        "asdsad",
                        "296000"),
                new Record(SOURCE2_URL,
                        "RECORD #2",
                        "TopicUUID",
                        "29.30.3030",
                        "asdsad",
                        "474000"),
                new Record(SOURCE3_URL,
                        "RECORD #3",
                        "TopicUUID",
                        "29.30.3030",
                        "asdsad",
                        "369000"),
                new Record(SOURCE4_URL,
                        "RECORD #4",
                        "TopicUUID",
                        "29.30.3030",
                        "asdsad",
                        "225000"));
    }

    public static String getUriFromRecordUUID(String UUID) {
        return UUID;
    }

    static public Record getRecordByUUID(String UUID) {
        for (Record record: getListRecord()){
            if (record.uuid == UUID) return record;
        }
        return null;
    }

    static public Drawable getRecordImageByUserUUID(String UUID, Context context) {
        return ContextCompat.getDrawable(context, R.drawable.mlr_test_record_image);
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
