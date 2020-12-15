package com.com.technoparkproject.repo;

import android.content.Context;
import android.util.ArrayMap;
import android.util.Log;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;

import com.com.technoparkproject.model_converters.FirebaseConverter;
import com.com.technoparkproject.model_converters.FromRoomConverter;
import com.com.technoparkproject.model_converters.ToRoomConverter;
import com.com.technoparkproject.models.Record;
import com.com.technoparkproject.models.Topic;
import com.com.technoparkproject.view_models.MainListOfRecordsViewModel;
import com.technopark.room.db.AppRoomDatabase;
import com.technopark.room.entities.TopicRecords;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import voice.it.firebaseloadermodule.FirebaseLoader;
import voice.it.firebaseloadermodule.cnst.FirebaseCollections;
import voice.it.firebaseloadermodule.listeners.FirebaseGetListListener;
import voice.it.firebaseloadermodule.listeners.FirebaseGetListener;
import voice.it.firebaseloadermodule.model.FirebaseModel;
import voice.it.firebaseloadermodule.model.FirebaseRecord;
import voice.it.firebaseloadermodule.model.FirebaseTopic;

public class AppRepoImpl {
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


    public static AppRepoImpl getAppRepo(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppRepoImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AppRepoImpl(context);
                }
            }
        }
        return INSTANCE;
    }
    
    // TCP/HTTP/DNS (depending on the port, 53=DNS, 80=HTTP, etc.)
    private boolean isOnline() {
        try {
            int timeoutMs = 1500;
            Socket sock = new Socket();
            SocketAddress sockaddr = new InetSocketAddress("8.8.8.8", 53);

            sock.connect(sockaddr, timeoutMs);
            sock.close();

            return true;
        } catch (IOException e) { return false; }

    }

    private void testOnline() {
        new FirebaseLoader().getFirst(FirebaseCollections.Topics, new FirebaseGetListener<FirebaseModel>() {
            @Override
            public void onFailure(String error) {
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

    public LiveData<ArrayMap<Topic,List<Record>>> queryAllTopicRecords(){
        testOnline();
        MediatorLiveData<ArrayMap<Topic,List<Record>>> topicRecords = new MediatorLiveData<>();
        topicRecords.addSource(isOnline, online -> {
            if (online){
                addSingleSource(topicRecords,queryOnlineAllTopicRecords());
            }
            else {
                Log.d("Room","RECTOPIC fetch");
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
                    Log.d("firebase topics", "empty response");
                    return;
                }
                Log.d("Firebase","topics fetch");
                List<Topic> topics = new FirebaseConverter().toTopicList(item);
                mDiskIO.execute(() -> mAppDb.appDao().insertTopics(ToRoomConverter.toTopicList(topics)));
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
                            if (item.size() == 0) {
                                Log.d("firebase records", "empty response");
                                return;
                            }
                            List<Record> records = new FirebaseConverter().toRecordList(item);
                            Log.d("Firebase","records fetch");
                            topicRecords.put(topic,records);
                            if (topicRecords.size() == topics.size()){
                                topicRecordsData.postValue(topicRecords);
                            }
                            updateRecordsCache(topicRecords.values());
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

    private LiveData<List<Topic>> queryCacheTopics(){
        return Transformations.map(mAppDb.appDao().getAllTopics(),
                FromRoomConverter::toTopicList);
    }

    public LiveData<List<Topic>> queryTopics() {
        testOnline();
        MediatorLiveData<List<Topic>> topicsData = new MediatorLiveData<>();
        topicsData.addSource(isOnline, online -> {
            if (online){
                queryOnlineTopics(topicsData);
            }
            else {
                LiveData<List<Topic>> cacheTopics = queryCacheTopics();
                topicsData.addSource(cacheTopics, topics -> {
                    Log.d("Room","topics fetch");
                    topicsData.setValue(topics);
                    topicsData.removeSource(cacheTopics);
                });
            }
            topicsData.removeSource(isOnline);
        });
        return topicsData;
    }

    private void queryOnlineTopics(MutableLiveData<List<Topic>> topicsData) {
        new FirebaseLoader().getAll(FirebaseCollections.Topics, new FirebaseGetListListener<FirebaseTopic>() {
            @Override
            public void onFailure(String error) {
            }

            @Override
            public void onGet(List<FirebaseTopic> item) {
                if (item.size() == 0) {
                    Log.d("firebase topics", "empty response");
                    return;
                }
                Log.d("Firebase","topics fetch");
                List<Topic> topics = new FirebaseConverter().toTopicList(item);
                topicsData.setValue(topics);

                mDiskIO.execute(() -> mAppDb.appDao().insertTopics(ToRoomConverter.toTopicList(topics)));
            }
        });
    }

    private LiveData<List<Record>> queryCacheRecords(final Topic topic){
        return Transformations.map(mAppDb.appDao().getRecordsByTopic(topic.uuid),
                FromRoomConverter::toRecordList);
    }


    public LiveData<List<Record>> queryRecords(final Topic topic) {
        testOnline();
        MediatorLiveData<List<Record>> recordsData = new MediatorLiveData<>();
        recordsData.addSource(isOnline, online -> {
            if (online){
                queryOnlineRecords(recordsData,topic);
            }
            else {
                LiveData<List<Record>> cacheTopics = queryCacheRecords(topic);
                recordsData.addSource(cacheTopics, records -> {
                    Log.d("Room","records fetch");
                    recordsData.setValue(records);
                    recordsData.removeSource(cacheTopics);
                });
            }
            recordsData.removeSource(isOnline);
        });
        return recordsData;
    }

    private void queryOnlineRecords(MutableLiveData<List<Record>> recordsList, Topic topic) {
        new FirebaseLoader().getAll(FirebaseCollections.Topics, topic.uuid,
                new FirebaseGetListListener<FirebaseRecord>() {
                    @Override
                    public void onFailure(String error) {
                    }

                    @Override
                    public void onGet(List<FirebaseRecord> item) {
                        if (item.size() == 0) {
                            Log.d("firebase records", "empty response");
                            return;
                        }
                        List<Record> records = new FirebaseConverter().toRecordList(item);
                        Log.d("Firebase","records fetch");
                        recordsList.setValue(records);
                        mDiskIO.execute(() -> mAppDb.appDao().insertRecords(ToRoomConverter.toRecordList(records)));
                    }
                });
    }

}
