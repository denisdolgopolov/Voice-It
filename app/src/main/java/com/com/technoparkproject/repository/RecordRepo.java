package com.com.technoparkproject.repository;

import android.content.Context;

import java.io.File;
import java.util.Collection;
import java.util.List;

public interface RecordRepo {
    List<Record> getRecords();
    void addRecord(Record record);
    File createTempFile(String suffix, Context context);
    void deleteTempFile(File recFile);
}
