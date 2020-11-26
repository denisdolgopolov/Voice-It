package com.com.technoparkproject.repository;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;


//todo: move this method to a class that works with local repo
public final class RecordRepo{

    private static final String DEFAULT_RECORD_NAME = "Моя запись";

    //generates temporary file for recording
    public static File createTempFile(String suffix,Context context) {
        try {
            File tempFileDir = context.getCacheDir();
            return File.createTempFile(DEFAULT_RECORD_NAME, suffix, tempFileDir);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(RecordRepo.class.getSimpleName(),
                    "can't create file temp for recording",e);
            return null;
        }
    }

    public static void deleteTempFile(File recFile){
        boolean isFileDel =  recFile.delete();
        if (!isFileDel)
            Log.e(RecordRepo.class.getSimpleName(),
                    "can't delete file: "+recFile.toString());
    }
}
