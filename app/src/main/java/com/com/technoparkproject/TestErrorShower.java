package com.com.technoparkproject;

import android.app.AlertDialog;
import android.content.Context;

public class TestErrorShower {
    public static void showErrorDevelopment(Context context) {
        if(context == null) return;
        new AlertDialog.Builder(context)
                .setTitle(R.string.in_development)
                .setPositiveButton(R.string.ok_wait, null)
                .create()
                .show();
    }
}
