package com.com.technoparkproject.repository;

import android.content.Context;
import android.util.Log;

import com.com.technoparkproject.VoiceItApplication;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecordRepoImpl implements RecordRepo{

    final List<Record> mRecords = new ArrayList<>();

    private static final String DEFAULT_RECORD_NAME = "Моя запись";

    public static RecordRepo getInstance(Context context) {
        return VoiceItApplication.from(context).getRecordRepo();
    }

    public List<Record> getRecords(){
        return mRecords;
    }

    public void addRecord(Record record){
        Log.d("RECORD OBJ","name "+record.getName()+
                ",topic "+record.getTopic()+
                ",file "+record.getRecordFile()+
                ",duration "+record.getDuration());
        mRecords.add(record);
    }



    //generates temporary file for recording
    public File createTempFile(String suffix,Context context) {
        try {
            File tempFileDir = context.getCacheDir();
            return File.createTempFile(DEFAULT_RECORD_NAME, suffix, tempFileDir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteTempFile(File recFile){
        boolean isFileDel =  recFile.delete();
        if (!isFileDel)
            Log.e(this.getClass().getSimpleName(),
                    "can't delete file: "+recFile.toString());
    }
}
