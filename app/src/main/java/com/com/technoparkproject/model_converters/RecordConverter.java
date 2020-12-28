package com.com.technoparkproject.model_converters;

import com.com.technoparkproject.models.TopicTypes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.technopark.recorder.repository.RecordTopic;

import java.util.Collections;

import voice.it.firebaseloadermodule.model.FirebaseRecord;
import voice.it.firebaseloadermodule.model.FirebaseTopic;

public class RecordConverter {
    public static FirebaseRecord toFirebaseRecord(RecordTopic recordTopic,
                                                 String recUUID,
                                                 String topicUUID,
                                                  String userUUID) {
        return new FirebaseRecord(
                recUUID,
                recordTopic.getName(),
                topicUUID,
                "some date",
                userUUID,
                recordTopic.getDuration());
    }

    public static FirebaseTopic toFirebaseTopic(RecordTopic recordTopic,
                                                String recordUUID,
                                                String topicUUID) {
        return new FirebaseTopic(recordTopic.getTopic(),
                "randomUUID",
                Collections.singletonList(recordUUID),
                TopicTypes.TOPIC_THEMATIC.toString(),
                topicUUID);
    }
}
