package com.com.technoparkproject.view.adapters.main_list_records;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.com.technoparkproject.R;

public class ItemListRecordsViewHolder extends RecyclerView.ViewHolder {
    public TextView textViewTitle;
    public TextView textViewDesc;
    public TextView textViewRecordTime;
    public ImageView recordImage;

    ItemListRecordsViewHolder(@NonNull View itemView) {
        super(itemView);

        this.textViewTitle = itemView.findViewById(R.id.text_view_title);
        this.textViewDesc = itemView.findViewById(R.id.text_view_desc);
        this.textViewRecordTime = itemView.findViewById(R.id.text_view_record_time);
        this.recordImage = itemView.findViewById(R.id.record_logo);
    }
}
