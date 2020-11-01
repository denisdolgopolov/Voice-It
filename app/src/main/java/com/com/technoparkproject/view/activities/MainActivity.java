package com.com.technoparkproject.view.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.com.technoparkproject.R;
import com.com.technoparkproject.view.fragments.MainListOfRecordsFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState == null)
            testLoadFragment();

    }

    private void testLoadFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_container, new MainListOfRecordsFragment())
                .commit();
    }

}
