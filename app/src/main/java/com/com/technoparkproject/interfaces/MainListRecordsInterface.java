package com.com.technoparkproject.interfaces;

import com.com.technoparkproject.view_models.MainListOfRecordsViewModel;
import com.example.repo.Record;

public interface MainListRecordsInterface {
    void showAllRecords(String topicUUID);
    void showRecordMoreFun(Record record);
    /*MainListOfRecordsViewModel getViewModel();*/
}
