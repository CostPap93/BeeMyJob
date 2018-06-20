package com.example.mastermind.testapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Toast;

import com.android.volley.RequestQueue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Kostas on 3/6/2018.
 */

public class TimesFragment extends Fragment {

    SharedPreferences settingsPreferences;

    RadioButton radioButton, radioButton1, radioButton2;

    Button btn_save_times;

    PendingIntent pendingIntentA;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_times, container, false);

        settingsPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());
        radioButton = rootView.findViewById(R.id.rb_day);
        radioButton1 = rootView.findViewById(R.id.rb_once);
        radioButton2 = rootView.findViewById(R.id.rb_twice);
        btn_save_times = rootView.findViewById(R.id.btn_save_times);

        if (settingsPreferences.getLong("interval", 0) == 86400000) {
            radioButton.setChecked(true);
        } else if (settingsPreferences.getLong("interval", 0) == 302400000) {
            radioButton1.setChecked(true);
        } else {
            radioButton2.setChecked(true);
        }

        btn_save_times.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(radioButton.isChecked()){
                    settingsPreferences.edit().putLong("interval",86400000).apply();
                }else if(radioButton1.isChecked()){
                    settingsPreferences.edit().putLong("interval",302400000).apply();
                }else{
                    settingsPreferences.edit().putLong("interval",604800000).apply();
                }
                cancel();
                start();
            }
        });

        return rootView;
    }

    public void start() {

        AlarmManager manager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

        Intent alarmIntent = new Intent(getContext(), AlarmReceiver.class);
        pendingIntentA = PendingIntent.getBroadcast(getContext(), 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), settingsPreferences.getLong("interval", 0), pendingIntentA);


    }

    public void cancel() {
        Intent alarmIntent = new Intent(getContext(), AlarmReceiver.class);
        AlarmManager manager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        manager.cancel(PendingIntent.getBroadcast(getContext(), 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT));

    }

}