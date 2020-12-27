package com.com.technoparkproject.model_converters;

import com.technopark.recorder.repository.RecordTopic;

import voice.it.firebaseloadermodule.model.FirebaseRecord;

public class RecordConverter {
    public static FirebaseRecord toFirebaseModel(RecordTopic recordTopic,
                                                 String UUID,
                                                 String topicUUID, String userUUID) {
        return new FirebaseRecord(
                UUID,
                recordTopic.getName(),
                topicUUID,
                "some date",
                userUUID,
                recordTopic.getDuration());
    }
}
