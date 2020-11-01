package com.com.technoparkproject.view.adapters.main_list_records;

import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.com.technoparkproject.R;

class ItemShowAllRecordsInTopic extends RecyclerView.ViewHolder {
    ImageButton bShowAllRecords;

    ItemShowAllRecordsInTopic(@NonNull View itemView) {
        super(itemView);
        this.bShowAllRecords = itemView.findViewById(R.id.b_show_all_records_in_topic);
    }
}