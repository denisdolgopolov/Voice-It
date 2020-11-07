package com.com.technoparkproject.view.adapters.main_list_records;

import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.com.technoparkproject.R;
import com.com.technoparkproject.interfaces.MainListRecordsInterface;

class ItemShowAllRecordsInTopic extends RecyclerView.ViewHolder {
    private ImageButton bShowAllRecords;

    ItemShowAllRecordsInTopic(@NonNull View itemView) {
        super(itemView);
        this.bShowAllRecords = itemView.findViewById(R.id.mlr_b_show_all_records_in_topic);
    }

    void bindViewHolder(final MainListRecordsInterface listener, final String topicUUID) {
        bShowAllRecords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.showAllRecords(topicUUID);
            }
        });
    }
}