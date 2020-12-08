package com.com.technoparkproject.interfaces;

import com.com.technoparkproject.models.Record;

public interface MainListRecordsInterface {
    void showAllRecords(String topicUUID);
    void showRecordMoreFun(Record record);
    void itemClicked(Record record);
}
