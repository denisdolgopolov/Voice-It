package com.com.technoparkproject.interfaces;

import androidx.lifecycle.LiveData;

import com.com.technoparkproject.models.Record;

public interface MainListRecordsInterface {
    void showAllRecords(String topicUUID);
    void showRecordMoreFun(Record record);
    void itemClicked(Record record);
    LiveData<String> getNowPlayingRecordUUID();
}
