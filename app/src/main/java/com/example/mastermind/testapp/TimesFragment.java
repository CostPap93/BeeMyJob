package com.example.mastermind.testapp;

import android.app.PendingIntent;
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

import com.android.volley.RequestQueue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Kostas on 3/6/2018.
 */

public class TimesFragment extends Fragment {

    SharedPreferences settingsPreferences;

    RadioButton radioButton, radioButton1, radioButton2;

    SimpleDateFormat format;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_times, container, false);

        settingsPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());
        radioButton = rootView.findViewById(R.id.rb_day);
        radioButton1 = rootView.findViewById(R.id.rb_once);
        radioButton2 = rootView.findViewById(R.id.rb_twice);

        if (settingsPreferences.getLong("interval", 0) == 86400000) {
            radioButton.setChecked(true);
        } else if (settingsPreferences.getLong("interval", 0) == 302400000) {
            radioButton1.setChecked(true);
        } else {
            radioButton2.setChecked(true);
        }

        return rootView;
    }
}