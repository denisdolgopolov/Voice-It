package com.com.technoparkproject.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class BroadcastUpdateListRecords extends BroadcastReceiver {
    private BroadcastUpdateListRecordListener listener;
    private static final String BROADCAST_UPDATE_LIST_RECORDS = "update_list_records";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(listener != null)
            listener.onUpdate();
    }

    public void setListener(BroadcastUpdateListRecordListener listener) {
        this.listener = listener;
    }

    public IntentFilter getIntentFilter() {
        return new IntentFilter(BROADCAST_UPDATE_LIST_RECORDS);
    }
}
