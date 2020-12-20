package com.com.technoparkproject.repo;

import android.util.ArrayMap;

import androidx.lifecycle.LiveData;

import com.com.technoparkproject.models.Record;
import com.com.technoparkproject.models.Topic;

import java.util.List;

public interface AppRepo {
    LiveData<ArrayMap<Topic, List<Record>>> queryAllTopicRecords();
}
