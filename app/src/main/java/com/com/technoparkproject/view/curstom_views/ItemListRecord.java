package com.com.technoparkproject.view.curstom_views;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.com.technoparkproject.models.Record;
import com.com.technoparkproject.repositories.TestRecordsRepository;
import com.com.technoparkproject.view.adapters.main_list_records.ItemListRecordsViewHolder;

public class ItemListRecord {
    public static void inflateView(Record record, ItemListRecordsViewHolder holder) {
        Context context = holder.itemView.getContext();

        holder.textViewTitle.setText(record.name);
        holder.textViewDesc.setText(record.dateOfCreation);
        holder.textViewRecordTime.setText(record.length);

        Drawable image = TestRecordsRepository.getRecordImageByUserUUID(record.userUUID, context);
        holder.recordImage.setImageDrawable(image);
    }
}
