package com.sailflorve.imageviewplus.sample;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn1).setOnClickListener(v ->
                startActivity(new Intent(this, ImageActivity.class)));

        findViewById(R.id.btn2).setOnClickListener(v ->
                startActivity(new Intent(this, ImageViewPagerActivity.class)));
    }
}
