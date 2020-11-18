package com.com.technoparkproject.repository;

import android.content.Context;

import com.com.technoparkproject.R;
import com.com.technoparkproject.VoiceItApplication;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordRepo {

    private static final String DEFAULT_RECORD_NAME = "Моя запись";

    public static RecordRepo getInstance(Context context) {
        return VoiceItApplication.from(context).getRecordRepo();
    }

    public void addRecord(Record record){
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
}
