package com.com.technoparkproject.repo;

import android.content.Context;
import android.util.ArrayMap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.com.technoparkproject.model_converters.FirebaseConverter;
import com.com.technoparkproject.model_converters.FromRoomConverter;
import com.com.technoparkproject.model_converters.ToRoomConverter;
import com.com.technoparkproject.models.Record;
import com.com.technoparkproject.models.Topic;
import com.technopark.room.db.AppRoomDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import voice.it.firebaseloadermodule.FirebaseLoader;
import voice.it.firebaseloadermodule.cnst.FirebaseCollections;
import voice.it.firebaseloadermodule.listeners.FirebaseGetListListener;
import voice.it.firebaseloadermodule.listeners.FirebaseGetListener;
import voice.it.firebaseloadermodule.model.FirebaseModel;
import voice.it.firebaseloadermodule.model.FirebaseRecord;
import voice.it.firebaseloadermodule.model.FirebaseTopic;

public class AppRepoImpl implements AppRepo{
    private final Executor mDiskIO;

    private final Executor mNetworkIO;

    private volatile static AppRepoImpl INSTANCE;
    private final AppRoomDatabase mAppDb;
    private final MutableLiveData<Boolean> isOnline;

    public AppRepoImpl(Context context) {
        mAppDb = AppRoomDatabase.getDatabase(context);
        isOnline = new MutableLiveData<>();
        mDiskIO = Executors.newSingleThreadExecutor();
        mNetworkIO = Executors.newFixedThreadPool(3);
    }


    public static AppRepo getAppRepo(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppRepoImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AppRepoImpl(context);
                }
            }
        }
        return INSTANCE;
    }

    private void testOnline() {
        new FirebaseLoader().getFirst(FirebaseCollections.Topics, new FirebaseGetListener<FirebaseModel>() {
            @Override
            public void onFailure(String error) {
                //
            }
            @Override
            public void onGet(FirebaseModel item) {
                isOnline.setValue(item != null);
            }
        });
    }


    private static <V> void addSingleSource(MediatorLiveData<V> mediator,LiveData<V> source){
        mediator.addSource(source, v -> {
            mediator.setValue(v);
            mediator.removeSource(source);
        });
    }

    public LiveData<List<Topic>> queryAllTopics() {
        testOnline();
        MediatorLiveData<List<Topic>> topicsData = new MediatorLiveData<>();
        topicsData.addSource(isOnline, online -> {
            if (online){
                addSingleSource(topicsData,queryOnlineAllTopics());
            }
            else {
                addSingleSource(topicsData,queryCacheAllTopics());
            }
            topicsData.removeSource(isOnline);
        });
        return topicsData;
    }

    private LiveData<List<Topic>> queryCacheAllTopics(){
        return Transformations.map(mAppDb.appDao().getAllTopics(),
                FromRoomConverter::toTopicList);
    }

    private LiveData<List<Topic>> queryOnlineAllTopics() {
        MutableLiveData<List<Topic>> topicsData = new MutableLiveData<>();
        mNetworkIO.execute(() -> new FirebaseLoader().getAll(FirebaseCollections.Topics, new FirebaseGetListListener<FirebaseTopic>() {
            @Override
            public void onFailure(String error) {
            }

            @Override
            public void onGet(List<FirebaseTopic> item) {
                if (item.size() == 0) {
                    return;
                }
                List<Topic> topics = new FirebaseConverter().toTopicList(item);
                topicsData.postValue(topics);
                mDiskIO.execute(() -> mAppDb.appDao().insertTopics(ToRoomConverter.toTopicList(topics)));
            }
        }));
        return topicsData;
    }


    public LiveData<ArrayMap<Topic,List<Record>>> queryAllTopicRecords(){
        testOnline();
        MediatorLiveData<ArrayMap<Topic,List<Record>>> topicRecords = new MediatorLiveData<>();
        topicRecords.addSource(isOnline, online -> {
            if (online){
                addSingleSource(topicRecords,queryOnlineAllTopicRecords());
            }
            else {
                addSingleSource(topicRecords,queryCacheAllTopicRecords());
            }
            topicRecords.removeSource(isOnline);
        });
        return topicRecords;
    }

    private LiveData<ArrayMap<Topic,List<Record>>> queryCacheAllTopicRecords(){
        return Transformations.map(mAppDb.appDao().getAllTopicRecords(),
                FromRoomConverter::toTopicRecords);
    }

    private LiveData<ArrayMap<Topic,List<Record>>> queryOnlineAllTopicRecords(){
        MutableLiveData<ArrayMap<Topic,List<Record>>> topicRecordsData = new MutableLiveData<>();
        mNetworkIO.execute(() -> new FirebaseLoader().getAll(FirebaseCollections.Topics, new FirebaseGetListListener<FirebaseTopic>() {
            @Override
            public void onFailure(String error) {
            }

            @Override
            public void onGet(List<FirebaseTopic> item) {
                if (item.size() == 0) {
                    return;
                }
                List<Topic> topics = new FirebaseConverter().toTopicList(item);
                mDiskIO.execute(() ->{
                    mAppDb.clearAllTables();
                    mAppDb.appDao().insertTopics(ToRoomConverter.toTopicList(topics));
                });
                queryOnlineRecords(topicRecordsData,topics);
            }
        })
        );
        return topicRecordsData;
    }

    private void queryOnlineRecords(MutableLiveData<ArrayMap<Topic, List<Record>>> topicRecordsData, List<Topic> topics) {
        ArrayMap<Topic,List<Record>> topicRecords = new ArrayMap<>();
        for (Topic topic : topics){
            new FirebaseLoader().getAll(FirebaseCollections.Topics, topic.uuid,
                    new FirebaseGetListListener<FirebaseRecord>() {
                        @Override
                        public void onFailure(String error) {
                        }
                        @Override
                        public void onGet(List<FirebaseRecord> item) {
                            List<Record> records = new FirebaseConverter().toRecordList(item);
                            topicRecords.put(topic,records);
                            topicRecords.size();
                            if (topicRecords.size() == topics.size()) {
                                topicRecordsData.postValue(topicRecords);
                                updateRecordsCache(topicRecords.values());
                            }
                        }
                    });
        }
    }

    private void updateRecordsCache(Collection<List<Record>> recordsList){
        List<Record> allRecords = new ArrayList<>();
        for (List<Record> records : recordsList){
            allRecords.addAll(records);
        }
        mDiskIO.execute(() -> mAppDb.appDao().insertRecords(ToRoomConverter.toRecordList(allRecords)));

    }

    public LiveData<ArrayMap<Topic, List<Record>>> queryAllTopicRecordsByUser(String userUUID) {
        testOnline();
        MediatorLiveData<ArrayMap<Topic,List<Record>>> topicRecords = new MediatorLiveData<>();
        topicRecords.addSource(isOnline, online -> {
            if (online){
                addSingleSource(topicRecords,queryOnlineAllTopicRecordsByUser(userUUID));
            }
            else {
                addSingleSource(topicRecords,queryCacheAllTopicRecordsByUser(userUUID));
            }
            topicRecords.removeSource(isOnline);
        });
        return topicRecords;
    }

    private LiveData<ArrayMap<Topic,List<Record>>> queryOnlineAllTopicRecordsByUser(String userUUID){
        MutableLiveData<ArrayMap<Topic,List<Record>>> topicRecordsData = new MutableLiveData<>();
        mNetworkIO.execute(() -> new FirebaseLoader().getAll(FirebaseCollections.Topics, new FirebaseGetListListener<FirebaseTopic>() {
                    @Override
                    public void onFailure(String error) {
                    }

                    @Override
                    public void onGet(List<FirebaseTopic> item) {
                        if (item.size() == 0) {
                            return;
                        }
                        List<Topic> topics = new FirebaseConverter().toTopicList(item);
                        mDiskIO.execute(() -> mAppDb.appDao().insertTopics(ToRoomConverter.toTopicList(topics)));
                        queryOnlineRecordsByUser(topicRecordsData,topics, userUUID);
                    }
                })
        );
        return topicRecordsData;
    }

    private void queryOnlineRecordsByUser(MutableLiveData<ArrayMap<Topic, List<Record>>> topicRecordsData,
                                          List<Topic> topics,
                                          String userUUID) {
        ArrayMap<Topic,List<Record>> topicRecords = new ArrayMap<>();
        FirebaseLoader firebaseLoader = new FirebaseLoader();
        ListIterator<Topic> topicsIter = topics.listIterator();
        while (topicsIter.hasNext()){
            int index = topicsIter.nextIndex();
            Topic topic = topicsIter.next();
            FirebaseGetListListener<FirebaseRecord> recordsListener = new FirebaseGetListListener<FirebaseRecord>() {
                @Override
                public void onFailure(String error) {
                }

                @Override
                public void onGet(List<FirebaseRecord> item) {
                    if (item.isEmpty())
                        return;
                    List<Record> records = new FirebaseConverter().toRecordList(item);
                    topicRecords.put(topic, records);
                    topicRecords.size();
                    if (index == topics.size()-1) {
                        topicRecordsData.postValue(topicRecords);
                        updateRecordsCache(topicRecords.values());
                    }
                }
            };
            firebaseLoader.getAllByUser(FirebaseCollections.Topics, topic.uuid,
                    userUUID,recordsListener);
        }
    }

    private LiveData<ArrayMap<Topic,List<Record>>> queryCacheAllTopicRecordsByUser(String userUUID){
        return Transformations.map(mAppDb.appDao().getAllTopicRecordsByUser(userUUID),
                FromRoomConverter::toTopicRecords);
    }

}
