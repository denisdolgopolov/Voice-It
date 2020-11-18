package com.com.technoparkproject.repository;

import android.content.Context;

import java.io.File;
import java.util.Collection;

public interface RecordRepo {
    Collection<Record> getRecords();
    void addRecord(Record record);
    File createTempFile(String suffix, Context context);
}
