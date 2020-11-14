package com.com.technoparkproject.view.adapters.main_list_records;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.com.technoparkproject.R;
import com.com.technoparkproject.interfaces.MainListRecordsInterface;
import com.com.technoparkproject.models.Record;
import com.com.technoparkproject.repositories.TestRecordsRepository;

class ItemListRecordsViewHolder extends RecyclerView.ViewHolder {
    private TextView textViewTitle;
    private TextView textViewDesc;
    private TextView textViewRecordTime;
    private ImageView recordImage;
    private ImageButton bMoreInfo;

    ItemListRecordsViewHolder(@NonNull View itemView) {
        super(itemView);

        this.textViewTitle = itemView.findViewById(R.id.mlr_text_view_title);
        this.textViewDesc = itemView.findViewById(R.id.mlr_text_view_desc);
        this.textViewRecordTime = itemView.findViewById(R.id.mlr_text_view_record_time);
        this.recordImage = itemView.findViewById(R.id.mlr_record_logo);
        this.bMoreInfo = itemView.findViewById(R.id.button_more);
    }

    void bindViewHolder(final MainListRecordsInterface listener, final Record record) {
        Context context = itemView.getContext();

        textViewTitle.setText(record.name);
        textViewDesc.setText(record.dateOfCreation);
        textViewRecordTime.setText(record.length);

        Drawable image = TestRecordsRepository.getRecordImageByUserUUID(record.userUUID, context);
        recordImage.setImageDrawable(image);

        bMoreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.showRecordMoreFun(record);
            }
        });
    }
}
