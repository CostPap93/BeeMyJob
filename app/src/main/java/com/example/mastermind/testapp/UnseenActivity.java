package com.example.mastermind.testapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

/**
 * Created by mastermind on 9/5/2018.
 */

public class UnseenActivity  extends AppCompatActivity {
    SharedPreferences settingsPreferences;
    ArrayList<JobOffer> offers;
    ArrayList<JobOffer> asyncOffers;


    ListView lv;
    DateFormat format;
    String[] paths;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unseen);

        getSupportActionBar().setTitle("BeeMyJob");

        lv = findViewById(R.id.listView);
        asyncOffers = new ArrayList<>();
        offers = new ArrayList<>();
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


        settingsPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        System.out.println(settingsPreferences.getInt("numberOfCategories", 0) == 0);
        System.out.println(settingsPreferences.getInt("numberOfCheckedCategories", 0) == 0);


        System.out.println(settingsPreferences.getBoolean("checkIsChanged", false));

            for (int i = 0; i < settingsPreferences.getInt("numberOfUnseenOffers", 0); i++) {

                JobOffer jobOffer = new JobOffer();
                jobOffer.setId(settingsPreferences.getInt("offerId " + i, 0));
                jobOffer.setCatid(settingsPreferences.getInt("offerCatid " + i, 0));
                jobOffer.setAreaid(settingsPreferences.getInt("offerAreaid " + i, 0));
                jobOffer.setTitle(settingsPreferences.getString("offerTitle " + i, ""));
                jobOffer.setCattitle(settingsPreferences.getString("offerCattitle " + i, ""));
                jobOffer.setAreatitle(settingsPreferences.getString("offerAreatitle " + i, ""));
                jobOffer.setLink(settingsPreferences.getString("offerLink " + i, ""));
                jobOffer.setDesc(settingsPreferences.getString("offerDesc " + i, ""));
                jobOffer.setDate(new Date(settingsPreferences.getLong("offerDate " + i, 0)));
                jobOffer.setDownloaded(settingsPreferences.getString("offerDownloaded " + i, ""));

                if(jobOffer.getDate().getTime()>settingsPreferences.getLong("lastSeenDate",jobOffer.getDate().getTime()))
                    settingsPreferences.edit().putLong("lastSeenDate",jobOffer.getDate().getTime()).apply();

                offers.add(jobOffer);


            }


        System.out.println(offers.toString());
        JobOfferAdapter jobOfferAdapter = new JobOfferAdapter(getApplicationContext(), offers);
        lv.setAdapter(jobOfferAdapter);
        System.out.println(settingsPreferences.getLong("interval",0));


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intentToDetail = new Intent(UnseenActivity.this,DetailActivity.class);
                intentToDetail.putExtra("jobOffer", (Serializable) adapterView.getItemAtPosition(i));
                startActivity(intentToDetail);

            }
        });

        if (settingsPreferences.getInt("numberOfImages",0)>0) {
            paths = new String[settingsPreferences.getInt("numberOfImages", 0)];
            for (int i = 1; i <= paths.length; i++) {
                paths[i - 1] = settingsPreferences.getString("imageUri" + i, "");
            }
            loadImageFromStorage(paths);
        }


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(UnseenActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void loadImageFromStorage(String[] paths)
    {
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        for(String path : paths) {


            try {
                File d = new File(path);
                System.out.println("This is the path to upload: " + d.toString());
                bitmaps.add(BitmapFactory.decodeStream(new FileInputStream(d)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        Random r = new Random();

        int rnum =r.nextInt(paths.length);
        ImageButton img = findViewById(R.id.imgBtn_ad);
        img.setVisibility(View.VISIBLE);
        img.setImageBitmap(bitmaps.get(rnum));

    }
}
