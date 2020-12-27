package com.com.technoparkproject.models;

import java.util.List;
import java.util.Objects;

public final class Topic {
    public final String uuid;
    public final String name;
    public final String logoImageUUID;
    public final List<String> records;
    public final TopicTypes type;

    public Topic(String uuid, String name, String logoImageUUID,
                 List<String> records, TopicTypes type) {
        this.uuid = uuid;
        this.name = name;
        this.logoImageUUID = logoImageUUID;
        this.records = records;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Topic topic = (Topic) o;
        return uuid.equals(topic.uuid) &&
                name.equals(topic.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name);
    }
}
