package com.com.technoparkproject.view.adapters.main_list_records;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.com.technoparkproject.R;
import com.com.technoparkproject.interfaces.MainListRecordsInterface;
import com.com.technoparkproject.models.Record;
import com.com.technoparkproject.models.RecordUtils;

import de.hdodenhof.circleimageview.CircleImageView;
import voice.it.firebaseloadermodule.FirebaseFileLoader;
import voice.it.firebaseloadermodule.cnst.FirebaseFileTypes;
import voice.it.firebaseloadermodule.listeners.FirebaseGetUriListener;

public class ItemListRecordsViewHolder extends RecyclerView.ViewHolder implements FirebaseGetUriListener {
    private final TextView textViewTitle;
    private final TextView textViewDesc;
    private final TextView textViewRecordTime;
    private final CircleImageView recordImage;
    private final ImageButton bMoreInfo;

    public ItemListRecordsViewHolder(@NonNull View itemView) {
        super(itemView);

        this.textViewTitle = itemView.findViewById(R.id.mlr_text_view_title);
        this.textViewDesc = itemView.findViewById(R.id.mlr_text_view_desc);
        this.textViewRecordTime = itemView.findViewById(R.id.mlr_text_view_record_time);
        this.recordImage = itemView.findViewById(R.id.mlr_record_logo);
        this.bMoreInfo = itemView.findViewById(R.id.button_more);
    }

    public void bindViewHolder(final MainListRecordsInterface listener, final Record record) {

        textViewTitle.setText(record.name);
        textViewDesc.setText(record.dateOfCreation);
        textViewRecordTime.setText(RecordUtils.durationFormatted(record.duration));
        new FirebaseFileLoader(recordImage.getContext())
                .getDownloadUri(FirebaseFileTypes.USER_PROFILE_IMAGES, record.userUUID, this);


        if (listener != null) {
            bMoreInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.showRecordMoreFun(record);
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.itemClicked(record);
                }
            });

        } else
            bMoreInfo.setVisibility(View.GONE);
    }

    @Override
    public void onGet(Uri uri) {
        Glide
                .with(recordImage)
                .load(uri.toString())
                .into(recordImage);
    }

    @Override
    public void onFailure(String error) {

    }
}
