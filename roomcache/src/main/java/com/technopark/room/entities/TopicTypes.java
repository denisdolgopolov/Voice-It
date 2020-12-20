package com.technopark.room.entities;

import androidx.room.TypeConverter;

public enum TopicTypes {
    TOPIC_FRIEND(0), TOPIC_THEMATIC(1);
    private final int type;

    TopicTypes(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    @TypeConverter
    public static TopicTypes fromType(Integer type) {
        for (TopicTypes p : values()) {
            if (p.type==type) {
                return p;
            }
        }
        return null;
    }

    @TypeConverter
    public static Integer fromTopicType(TopicTypes topicType) {
        return topicType.type;
    }
}
