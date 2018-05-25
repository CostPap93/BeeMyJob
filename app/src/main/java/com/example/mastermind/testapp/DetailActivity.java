package com.example.mastermind.testapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.text.SimpleDateFormat;

/**
 * Created by Kostas on 7/5/2018.
 */

public class DetailActivity extends AppCompatActivity {

    SimpleDateFormat format;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getSupportActionBar().setTitle("Datalabs");


        TextView txt_title = findViewById(R.id.txt_title);
        TextView txt_date = findViewById(R.id.txt_date);
        TextView txt_description = findViewById(R.id.txt_description);
        TextView txt_link = findViewById(R.id.txt_link);
        format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        JobOffer jobOffer = (JobOffer) getIntent().getSerializableExtra("jobOffer");


        txt_title.setText(jobOffer.getTitle());
        txt_date.setText("Δημοσιεύτηκε: " +format.format(jobOffer.getDate()));
        txt_description.setText(String.valueOf(jobOffer.getDesc()));
        txt_link.setText(String.valueOf(jobOffer.getLink()));



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
