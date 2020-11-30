package com.com.technoparkproject.view.adapters.main_list_records;

import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.com.technoparkproject.R;
import com.com.technoparkproject.interfaces.MainListRecordsInterface;
import com.com.technoparkproject.models.Record;
import com.com.technoparkproject.models.Topic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RecyclerTopicsWithRecordsAdapter extends RecyclerView.Adapter {
    private final ArrayList<Object> items = new ArrayList<>();
    private final ArrayMap<Topic, List<Record>> allTopics = new ArrayMap<>();
    private static final int COUNT_RECORDS_SHOW = 3;
    private final MainListRecordsInterface listener;

    public RecyclerTopicsWithRecordsAdapter(MainListRecordsInterface listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType) {
            case ViewTypes.TYPE_TOPIC_NAME:
                View viewTopicName = inflater.inflate(R.layout.mlr_item_topic_name,
                        parent, false);
                return new ItemTopicNameViewHolder(viewTopicName);
            case ViewTypes.TYPE_RECORD:
                View viewRecord = inflater.inflate(R.layout.item_of_list_records,
                        parent, false);
                return new ItemListRecordsViewHolder(viewRecord);
            default:
                View viewButton = inflater.inflate(R.layout.mlr_item_show_all_records_in_topic,
                        parent, false);
                return new ItemShowAllRecordsInTopic(viewButton);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        Object item = items.get(position);
        switch(type) {
            case ViewTypes.TYPE_TOPIC_NAME:
                Topic topic = (Topic) item;
                ItemTopicNameViewHolder topicViewHolder = (ItemTopicNameViewHolder) holder;
                topicViewHolder.bindViewHolder(topic);
                break;
            case ViewTypes.TYPE_RECORD:
                Record record = (Record) item;
                ItemListRecordsViewHolder recordViewHolder = (ItemListRecordsViewHolder) holder;
                recordViewHolder.bindViewHolder(listener, record);
                break;
            case ViewTypes.TYPE_BUTTON_SHOW_ALL_RECORDS:
                final String topicUUID = (String) item;
                ItemShowAllRecordsInTopic buttonViewHolder = (ItemShowAllRecordsInTopic) holder;
                buttonViewHolder.bindViewHolder(listener, topicUUID);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void showItems(ArrayMap<Topic, List<Record>> list) {
        int startIndex = items.size();

        for (int i = 0; i < list.size(); i++) {
            Topic topic = list.keyAt(i);
            items.add(topic);
            int countRecordShow = Math.min(list.valueAt(i).size(), COUNT_RECORDS_SHOW);
            for (int g = 0; g < countRecordShow; g++) {
                items.add(list.valueAt(i).get(g));
            }
            if (countRecordShow == COUNT_RECORDS_SHOW)
                items.add(topic.uuid);
        }
        notifyDataSetChanged();
    }

    public void setItems(ArrayMap<Topic, List<Record>> map) {
        allTopics.clear();
        items.clear();
        allTopics.putAll(map);
        showItems(allTopics);
    }

    public void filterItemsByTopicName(String str) {
        items.clear();
        ArrayMap<Topic, List<Record>> filtered = new ArrayMap<>();
        for (Topic topic : (Set<Topic>) allTopics.keySet())
            if (topic.name.contains(str)) filtered.put(topic, allTopics.get(topic));
        showItems(filtered);
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof Topic) return ViewTypes.TYPE_TOPIC_NAME;
        if (item instanceof Record) return ViewTypes.TYPE_RECORD;
        return ViewTypes.TYPE_BUTTON_SHOW_ALL_RECORDS;
    }

    static class ViewTypes {
        static final int TYPE_TOPIC_NAME = 1;
        static final int TYPE_RECORD = 2;
        static final int TYPE_BUTTON_SHOW_ALL_RECORDS = 3;
    }

}
