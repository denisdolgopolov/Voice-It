package com.com.technoparkproject;

import android.app.AlertDialog;
import android.content.Context;

public class TestErrorShower {
    public static void showErrorDevelopment(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Находится в разработке")
                .setPositiveButton("ок, жду", null)
                .create()
                .show();
    }
}
