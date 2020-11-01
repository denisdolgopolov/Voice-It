package com.com.technoparkproject.view.adapters.main_list_records;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.com.technoparkproject.R;

class ItemTopicNameViewHolder extends RecyclerView.ViewHolder {
    TextView textViewTitle;

    ItemTopicNameViewHolder(@NonNull View itemView) {
        super(itemView);
        this.textViewTitle = itemView.findViewById(R.id.text_view_title);
    }
}
