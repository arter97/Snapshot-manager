package com.arter97.snapshotmanager;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set color accordingly to the protected status
        getWindow().getDecorView().setBackgroundColor(getColor(R.color.colorGreen));
        getWindow().setStatusBarColor(getColor(R.color.colorGreen));
    }
}
