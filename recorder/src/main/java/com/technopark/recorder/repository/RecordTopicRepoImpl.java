package com.technopark.recorder.repository;

import android.content.Context;
import android.util.Log;

import com.technopark.recorder.VoiceItApplication;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//todo: make this as a local storage??
public final class RecordTopicRepoImpl implements RecordTopicRepo{

    private final Context mContext;

    public RecordTopicRepoImpl(Context context){
        mContext = context;
    }

    public static RecordTopicRepo getInstance(Context context) {
        return VoiceItApplication.from(context).getRecordTopicRepo();
    }

    final Map<UUID,RecordTopic> mRecTopics = new HashMap<>();

    private static final String DEFAULT_RECORD_NAME = "Моя запись";
    private static final String DEFAULT_REC_NAME = "Новая запись";
    private static final String DEFAULT_REC_TOPIC = "Топик записи";

    public Collection<RecordTopic> getRecTopics(){
        return mRecTopics.values();
    }


    private UUID mLastRecTopicUUID;

    public UUID createRecord(String suffix){
        File recFile = createTempFile(suffix);
        RecordTopic recordTopic = new RecordTopic(recFile,DEFAULT_REC_NAME,DEFAULT_REC_TOPIC,0);
        UUID id = UUID.randomUUID();
        mRecTopics.put(id,recordTopic);
        mLastRecTopicUUID = id;
        return id;
    }

    public RecordTopic getRecord(UUID uuid){
        return new RecordTopic(mRecTopics.get(uuid));
    }

    public RecordTopic getLastRecord(){
        return getRecord(mLastRecTopicUUID);
    }

    public void updateLastTopic(String topic){
        mRecTopics.get(mLastRecTopicUUID).setTopic(topic);
    }

    public void updateLastName(String name){
        mRecTopics.get(mLastRecTopicUUID).setName(name);
    }

    public void updateLastDuration(int duration){
        mRecTopics.get(mLastRecTopicUUID).setDuration(duration);
    }

    public void deleteLastRecord(){
        RecordTopic recordTopic = mRecTopics.remove(mLastRecTopicUUID);
        deleteTempFile(recordTopic.getRecordFile());
    }

    //generates temporary file for recording
    private File createTempFile(String suffix) {
        try {
            File tempFileDir = mContext.getCacheDir();
            return File.createTempFile(DEFAULT_RECORD_NAME, suffix, tempFileDir);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(RecordTopicRepoImpl.class.getSimpleName(),
                    "can't create file temp for recording",e);
            return null;
        }
    }

    private void deleteTempFile(File recFile){
        boolean isFileDel =  recFile.delete();
        if (!isFileDel)
            Log.e(RecordTopicRepoImpl.class.getSimpleName(),
                    "can't delete file: "+recFile.toString());
    }
}
