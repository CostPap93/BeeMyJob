package com.example.mastermind.testapp;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Created by mastermind on 18/4/2018.
 */

public class SettingActivity  extends AppCompatActivity {
    SharedPreferences settingsPreferences;

    CheckBox checkBox;

    Button btnSave, btnCancel;
    RadioButton radioButton, radioButton1, radioButton2;
    ArrayList<Boolean> checkIsChanged;
    ArrayList<JobOffer> offers;
    ArrayList<JobOffer> asyncOffers;
    ArrayList<OfferCategory> categories;
    ArrayList<OfferArea> areas;
    String message = "";
    ArrayList<OfferArea> offerAreas;
    ArrayList<OfferCategory> offerCategories;
    SimpleDateFormat format;
    PendingIntent pendingIntentA;

    RequestQueue queue;
    String areasIds, categoriesIds;
    private RecyclerView categoriesRecyclerView;
    private RecyclerView areasRecyclerView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setTitle("Datalabs");
        settingsPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        checkBox = findViewById(R.id.chbox_category);

        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        radioButton = findViewById(R.id.rb_day);
        radioButton1 = findViewById(R.id.rb_once);
        radioButton2 = findViewById(R.id.rb_twice);
        checkIsChanged = new ArrayList<>();
        asyncOffers = new ArrayList<>();
        offers = new ArrayList<>();
        categories = new ArrayList<>();
        areas = new ArrayList<>();
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        areasIds = "";
        categoriesIds = "";

        for (int i = 0; i < settingsPreferences.getInt("numberOfCheckedCategories", 0); i++) {
            if (categoriesIds.equals("")) {
                categoriesIds += settingsPreferences.getInt("checkedCategoryId " + i, 0);
            } else {
                categoriesIds += "," + settingsPreferences.getInt("checkedCategoryId " + i, 0);
            }
        }

        for (int i = 0; i < settingsPreferences.getInt("numberOfCheckedAreas", 0); i++) {
            if (areasIds.equals("")) {
                areasIds += settingsPreferences.getInt("checkedAreaId " + i, 0);
            } else {
                areasIds += "," + settingsPreferences.getInt("checkedAreaId " + i, 0);
            }
        }


        if (queue == null) {
            queue = Volley.newRequestQueue(this);
        }


        System.out.println(settingsPreferences.getInt("numberOfCategories", 0));
        settingsPreferences.edit().putBoolean("checkIsChanged", false).apply();

        if (settingsPreferences.getInt("numberOfCategories", 0) != 0) {
            for (int i = 0; i < settingsPreferences.getInt("numberOfCategories", 0); i++) {
                OfferCategory category = new OfferCategory();
                category.setCatid(settingsPreferences.getInt("offerCategoryId " + i, 0));
                category.setTitle(settingsPreferences.getString("offerCategoryTitle " + i, ""));
                categories.add(category);
                System.out.println(categories.get(i).getTitle() + "checkBoxAdapter");
            }

            categoriesRecyclerView = findViewById(R.id.lv_categories);

            //RecyclerView layout manager
            LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(this);
            categoriesRecyclerView.setLayoutManager(recyclerLayoutManager);

            //RecyclerView item decorator
            DividerItemDecoration dividerItemDecoration =
                    new DividerItemDecoration(categoriesRecyclerView.getContext(),
                            recyclerLayoutManager.getOrientation());
            categoriesRecyclerView.addItemDecoration(dividerItemDecoration);

            //RecyclerView adapater
            CheckRecycleAdapter recyclerViewAdapter = new
                    CheckRecycleAdapter(categories,this);
            categoriesRecyclerView.setAdapter(recyclerViewAdapter);
        }



        if (settingsPreferences.getInt("numberOfAreas", 0) != 0) {
            for (int i = 0; i < settingsPreferences.getInt("numberOfAreas", 0); i++) {
                OfferArea area = new OfferArea();
                area.setAreaid(settingsPreferences.getInt("offerAreaId " + i, 0));
                area.setTitle(settingsPreferences.getString("offerAreaTitle " + i, ""));
                areas.add(area);
                System.out.println(areas.get(i).getTitle() + "checkBoxAdapter");
            }
            areasRecyclerView =findViewById(R.id.lv_areas);

            //RecyclerView layout manager
            LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(this);
            areasRecyclerView.setLayoutManager(recyclerLayoutManager);

            //RecyclerView item decorator
            DividerItemDecoration dividerItemDecoration =
                    new DividerItemDecoration(areasRecyclerView.getContext(),
                            recyclerLayoutManager.getOrientation());
            areasRecyclerView.addItemDecoration(dividerItemDecoration);

            //RecyclerView adapater
            CheckAreaRecycleAdapter recyclerViewAdapter = new
                    CheckAreaRecycleAdapter(areas,this);
            areasRecyclerView.setAdapter(recyclerViewAdapter);

        }

        if (settingsPreferences.getLong("interval", 0) == 86400000) {
            radioButton.setChecked(true);
        } else if (settingsPreferences.getLong("interval", 0) == 302400000) {
            radioButton1.setChecked(true);
        } else {
            radioButton2.setChecked(true);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_refresh) {

            RefreshOperation();

        }

        return super.onOptionsItemSelected(item);
    }

    public void RefreshOperation() {

        if(isConn()) {
            queue.add(volleyUpdateDefault());
        }else {
            Toast.makeText(SettingActivity.this,"Πρέπει να είστε συνδεδεμένος στο ίντερνετ για να κάνετε ανανέωση!",Toast.LENGTH_LONG).show();
        }
    }


    public void btnSaveClicked(View view) throws ExecutionException, InterruptedException {


        CheckBox checkBox;
        ArrayList<String> categoriesNames = new ArrayList<>();
        offerCategories = new ArrayList<>();
        ArrayList<String> areasNames = new ArrayList<>();
        offerAreas = new ArrayList<>();

        OfferCategory offerCategory;
        OfferArea offerArea;
        areasIds = "";
        categoriesIds = "";
        if (isConn()) {

            for (int i = 0; i < categoriesRecyclerView.getChildCount(); i++) {
                checkBox = categoriesRecyclerView.getChildAt(i).findViewById(R.id.chbox_category);
                if (checkBox.isChecked()) {
                    categoriesNames.add(checkBox.getText().toString());
                }
            }

            for (int j = 0; j < settingsPreferences.getInt("numberOfCategories", 0); j++) {
                for (String name : categoriesNames) {
                    if (name.equals(settingsPreferences.getString("offerCategoryTitle " + j, ""))) {
                        offerCategory = new OfferCategory();
                        offerCategory.setCatid(settingsPreferences.getInt("offerCategoryId " + j, 0));
                        offerCategory.setTitle(settingsPreferences.getString("offerCategoryTitle " + j, ""));
                        offerCategories.add(offerCategory);
                    }
                }
            }


            for (int i = 0; i < areasRecyclerView.getChildCount(); i++) {
                checkBox = areasRecyclerView.getChildAt(i).findViewById(R.id.chbox_category);
                if (checkBox.isChecked()) {
                    areasNames.add(checkBox.getText().toString());
                }
            }

            for (int j = 0; j < settingsPreferences.getInt("numberOfAreas", 0); j++) {
                for (String name : areasNames) {
                    if (name.equals(settingsPreferences.getString("offerAreaTitle " + j, ""))) {
                        offerArea = new OfferArea();
                        offerArea.setAreaid(settingsPreferences.getInt("offerAreaId " + j, 0));
                        offerArea.setTitle(settingsPreferences.getString("offerAreaTitle " + j, ""));
                        offerAreas.add(offerArea);
                    }
                }
            }

            System.out.println(offerCategories.size());
            if (!offerCategories.isEmpty() && !offerAreas.isEmpty()) {
                cancel();

                if (radioButton.isChecked()) {
                    if (!(settingsPreferences.getLong("interval", 0) == 86400000)) {
                        settingsPreferences.edit().putLong("interval", 86400000).apply();

                    }
                } else if (radioButton1.isChecked()) {
                    if (!(settingsPreferences.getLong("interval", 0) == 302400000)) {
                        settingsPreferences.edit().putLong("interval", 302400000).apply();

                    }
                } else {
                    if (!(settingsPreferences.getLong("interval", 0) == 604800000)) {
                        settingsPreferences.edit().putLong("interval", 604800000).apply();

                    }
                }

                System.out.println(settingsPreferences.getLong("interval", 0));

                start();

                for (OfferCategory oc : offerCategories) {
                    if (categoriesIds.equals("")) {
                        categoriesIds += oc.getCatid();
                    } else {
                        categoriesIds += "," + oc.getCatid();
                    }

                }
                System.out.println(categoriesIds);

                for (OfferArea oa : offerAreas) {
                    if (areasIds.equals("")) {
                        areasIds += oa.getAreaid();
                    } else {
                        areasIds += "," + oa.getAreaid();
                    }

                }

                System.out.println(categoriesIds);
                System.out.println(areasIds);
                volleySaveOffers(categoriesIds, areasIds);


            } else if (offerCategories.isEmpty()) {
                Toast.makeText(MyApplication.getAppContext(), "Πρέπει να επιλέξετε τουλάχιστον μία κατηγορία!", Toast.LENGTH_LONG).show();
            } else if (offerAreas.isEmpty()) {
                Toast.makeText(MyApplication.getAppContext(), "Πρέπει να επιλέξετε τουλάχιστον μία περιοχή!", Toast.LENGTH_LONG).show();
            }


        } else {
            Toast.makeText(SettingActivity.this, "Πρέπει να είστε συνδεδεμένος για να γίνει αποθήκευση των επιλογών σας!", Toast.LENGTH_LONG).show();
        }

    }

    public void btnCancelClicked(View view) {
        Intent intent = new Intent(SettingActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void btnResetClicked(View view) throws ExecutionException, InterruptedException {

        if (isConn()) {
            cancel();

            volleySetDefault();
            settingsPreferences.edit().putLong("interval", 86400000).apply();
            start();
        } else {
            Toast.makeText(SettingActivity.this, "Πρέπει να είστε συνδεδεμένος για να γίνει επαναφορά των επιλογών σας!", Toast.LENGTH_LONG).show();
        }
    }

    public boolean isConn() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isWifiConn = networkInfo.isConnected();
        networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isMobileConn = networkInfo.isConnected();
        Log.d("connection", "Wifi connected: " + isWifiConn);
        Log.d("connection", "Mobile connected: " + isMobileConn);
        return isWifiConn || isMobileConn;
    }

    public void start() {

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent alarmIntent = new Intent(SettingActivity.this, AlarmReceiver.class);
        pendingIntentA = PendingIntent.getBroadcast(SettingActivity.this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), settingsPreferences.getLong("interval", 0), pendingIntentA);


    }

    public void cancel() {
        Intent alarmIntent = new Intent(SettingActivity.this, AlarmReceiver.class);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.cancel(PendingIntent.getBroadcast(SettingActivity.this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT));

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(SettingActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    public StringRequest volleyUpdateDefault() {
        String url = Utils.getUrl()+"jobOfferCategories.php?";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println(response);
                        ArrayList<OfferCategory> categoriesRefresh = new ArrayList<>();


                        // Display the first 500 characters of the response string.
                        try {
                            JSONObject jsonObjectAll = new JSONObject(response);

                            JSONArray jsonArray = jsonObjectAll.getJSONArray("joboffercategories");
                            System.out.println(jsonArray.length());
                            settingsPreferences.edit().putInt("numberOfCategories", jsonArray.length()).apply();
                            System.out.println(settingsPreferences.getInt("numberOfCategories", 0));
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObjectCategory = jsonArray.getJSONObject(i);
                                settingsPreferences.edit().putInt("offerCategoryId " + i, Integer.valueOf(jsonObjectCategory.getString("jacat_id"))).apply();
                                settingsPreferences.edit().putString("offerCategoryTitle " + i, jsonObjectCategory.getString("jacat_title")).apply();
                                System.out.println(jsonObjectCategory.toString());

                            }

                            if (settingsPreferences.getInt("numberOfCategories", 0) != 0) {
                                for (int i = 0; i < settingsPreferences.getInt("numberOfCategories", 0); i++) {
                                    OfferCategory category = new OfferCategory();
                                    category.setCatid(settingsPreferences.getInt("offerCategoryId " + i, 0));
                                    category.setTitle(settingsPreferences.getString("offerCategoryTitle " + i, ""));
                                    categoriesRefresh.add(category);
                                    System.out.println(categoriesRefresh.get(i).getTitle() + "checkBoxAdapter");
                                }

                                //RecyclerView layout manager
                                LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(SettingActivity.this);
                                categoriesRecyclerView.setLayoutManager(recyclerLayoutManager);

                                //RecyclerView item decorator
                                DividerItemDecoration dividerItemDecoration =
                                        new DividerItemDecoration(categoriesRecyclerView.getContext(),
                                                recyclerLayoutManager.getOrientation());
                                categoriesRecyclerView.addItemDecoration(dividerItemDecoration);

                                //RecyclerView adapater
                                CheckRecycleAdapter recyclerViewAdapter = new
                                        CheckRecycleAdapter(categoriesRefresh,SettingActivity.this);
                                categoriesRecyclerView.setAdapter(recyclerViewAdapter);

                                queue.add(volleyUpdateDefaultAreas());

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    message = "TimeOutError";
                    //This indicates that the reuest has either time out or there is no connection

                } else if (error instanceof AuthFailureError) {
                    message = "AuthFailureError";
                    // Error indicating that there was an Authentication Failure while performing the request

                } else if (error instanceof ServerError) {
                    message = "ServerError";
                    //Indicates that the server responded with a error response

                } else if (error instanceof NetworkError) {
                    message = "NetworkError";
                    //Indicates that there was network error while performing the request

                } else if (error instanceof ParseError) {
                    message = "ParseError";
                    // Indicates that the server response could not be parsed

                }
                System.out.println("Volley: " + message);
                if (!message.equals("")) {
                    Toast.makeText(SettingActivity.this, Utils.getServerError(), Toast.LENGTH_LONG).show();
                    Intent intentError = new Intent(SettingActivity.this, SettingActivity.class);
                    startActivity(intentError);
                }
            }
        }
        );
        return stringRequest;
    }

    public StringRequest volleyUpdateDefaultAreas() {
        String url = Utils.getUrl()+"jobOfferAreas.php?";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println(response);
                        ArrayList<OfferArea> areasRefresh = new ArrayList<>();


                        // Display the first 500 characters of the response string.
                        try {
                            JSONObject jsonObjectAll = new JSONObject(response);

                            JSONArray jsonArray = jsonObjectAll.getJSONArray("jobofferareas");
                            System.out.println(jsonArray.length());
                            settingsPreferences.edit().putInt("numberOfAreas", jsonArray.length()).apply();
                            System.out.println(settingsPreferences.getInt("numberOfAreas", 0));
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObjectCategory = jsonArray.getJSONObject(i);
                                settingsPreferences.edit().putInt("offerAreaId " + i, Integer.valueOf(jsonObjectCategory.getString("jloc_id"))).apply();
                                settingsPreferences.edit().putString("offerAreaTitle " + i, jsonObjectCategory.getString("jloc_title")).apply();
                                System.out.println(jsonObjectCategory.toString());

                            }

                            if (settingsPreferences.getInt("numberOfAreas", 0) != 0) {
                                for (int i = 0; i < settingsPreferences.getInt("numberOfAreas", 0); i++) {
                                    OfferArea category = new OfferArea();
                                    category.setAreaid(settingsPreferences.getInt("offerAreaId " + i, 0));
                                    category.setTitle(settingsPreferences.getString("offerAreaTitle " + i, ""));
                                    areasRefresh.add(category);
                                    System.out.println(areasRefresh.get(i).getTitle() + "checkBoxAdapter");
                                }

                                //RecyclerView layout manager
                                LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(SettingActivity.this);
                                areasRecyclerView.setLayoutManager(recyclerLayoutManager);

                                //RecyclerView item decorator
                                DividerItemDecoration dividerItemDecoration =
                                        new DividerItemDecoration(areasRecyclerView.getContext(),
                                                recyclerLayoutManager.getOrientation());
                                areasRecyclerView.addItemDecoration(dividerItemDecoration);

                                //RecyclerView adapater
                                CheckAreaRecycleAdapter recyclerViewAdapter = new
                                        CheckAreaRecycleAdapter(areasRefresh,SettingActivity.this);
                                areasRecyclerView.setAdapter(recyclerViewAdapter);


                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    message = "TimeOutError";
                    //This indicates that the reuest has either time out or there is no connection

                } else if (error instanceof AuthFailureError) {
                    message = "AuthFailureError";
                    // Error indicating that there was an Authentication Failure while performing the request

                } else if (error instanceof ServerError) {
                    message = "ServerError";
                    //Indicates that the server responded with a error response

                } else if (error instanceof NetworkError) {
                    message = "NetworkError";
                    //Indicates that there was network error while performing the request

                } else if (error instanceof ParseError) {
                    message = "ParseError";
                    // Indicates that the server response could not be parsed

                }
                System.out.println("Volley: " + message);
                if (!message.equals("")) {
                    Toast.makeText(SettingActivity.this, Utils.getServerError(), Toast.LENGTH_LONG).show();
                    Intent intentError = new Intent(SettingActivity.this, SettingActivity.class);
                    startActivity(intentError);
                }
            }
        }
        );
        return stringRequest;
    }


    public void volleySaveOffers(final String param, final String param2) {

        String url = Utils.getUrl()+"jobAdsArray.php?";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // Display the first 500 characters of the response string.
                        System.out.println("Volley: " + message);
                        System.out.println(response);

                        try {
                            JSONObject jsonObjectAll = new JSONObject(response);
                            JSONArray jsonArray = jsonObjectAll.getJSONArray("offers");
                            int i = 0;

                            while (i < jsonArray.length() && i < 5) {


                                JSONObject jsonObjectCategory = jsonArray.getJSONObject(i);


                                JobOffer offer = new JobOffer();
                                offer.setId(Integer.valueOf(jsonObjectCategory.getString("jad_id")));
                                offer.setCatid(Integer.valueOf(jsonObjectCategory.getString("jad_catid")));
                                offer.setAreaid(Integer.valueOf(jsonObjectCategory.getString("jloc_id")));
                                offer.setTitle(jsonObjectCategory.getString("jad_title"));
                                offer.setCattitle(jsonObjectCategory.getString("jacat_title"));
                                offer.setAreatitle(jsonObjectCategory.getString("jloc_title"));
                                offer.setLink(jsonObjectCategory.getString("jad_link"));
                                offer.setDesc(jsonObjectCategory.getString("jad_desc"));
                                offer.setDate(format.parse(jsonObjectCategory.getString("jad_date")));
                                offer.setDownloaded(jsonObjectCategory.getString("jad_downloaded"));
                                System.out.println(offer.getTitle() + " first time");

                                asyncOffers.add(offer);

                                Collections.sort(asyncOffers, new Comparator<JobOffer>() {
                                    @Override
                                    public int compare(JobOffer jobOffer, JobOffer t1) {
                                        if (jobOffer.getDate().getTime() - t1.getDate().getTime() < 0)
                                            return 1;
                                        else if (jobOffer.getDate().getTime() - t1.getDate().getTime() == 0)
                                            return 0;
                                        else
                                            return -1;
                                    }
                                });
                                for (int x = 0; x < asyncOffers.size(); x++) {
                                    System.out.println(asyncOffers.get(x).getTitle());
                                }


                                i++;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }


                        for (int j = 0; j < settingsPreferences.getInt("numberOfCategories", 0); j++) {
                            System.out.println(settingsPreferences.getString("checkedCategoryTitle " + j, "") + "Removed from checked categories");
                            settingsPreferences.edit().remove("checkedCatergoryId " + j).apply();
                            settingsPreferences.edit().remove("checkedCatergoryTitle " + j).apply();
                        }

                        for (OfferCategory oc : offerCategories) {

                            System.out.println(settingsPreferences.getString("checkedCategoryTitle " + offerCategories.indexOf(oc), "") + "Previously in checked categories");
                            settingsPreferences.edit().putInt("checkedCategoryId " + offerCategories.indexOf(oc), oc.getCatid()).apply();
                            settingsPreferences.edit().putString("checkedCategoryTitle " + offerCategories.indexOf(oc), oc.getTitle()).apply();
                            System.out.println(settingsPreferences.getString("checkedCategoryTitle " + offerCategories.indexOf(oc), "") + "Added to checked categories");

                        }

                        for (int j = 0; j < settingsPreferences.getInt("numberOfAreas", 0); j++) {
                            System.out.println(settingsPreferences.getString("checkedAreaTitle " + j, "") + "Removed from checked categories");
                            settingsPreferences.edit().remove("checkedAreaId " + j).apply();
                            settingsPreferences.edit().remove("checkedAreaTitle " + j).apply();
                        }

                        for (OfferArea oa : offerAreas) {

                            System.out.println(settingsPreferences.getString("checkedAreaTitle " + offerAreas.indexOf(oa), "") + "Previously in checked categories");
                            settingsPreferences.edit().putInt("checkedAreaId " + offerAreas.indexOf(oa), oa.getAreaid()).apply();
                            settingsPreferences.edit().putString("checkedAreaTitle " + offerAreas.indexOf(oa), oa.getTitle()).apply();
                            System.out.println(settingsPreferences.getString("checkedAreaTitle " + offerAreas.indexOf(oa), "") + "Added to checked categories");

                        }
                        settingsPreferences.edit().putInt("numberOfCheckedCategories", offerCategories.size()).apply();
                        settingsPreferences.edit().putInt("numberOfCheckedAreas", offerAreas.size()).apply();


                        for (int j = 0; j < 5; j++) {
                            settingsPreferences.edit().remove("offerId " + j).apply();
                            settingsPreferences.edit().remove("offerCatid " + j).apply();
                            settingsPreferences.edit().remove("offerAreaid " + j).apply();
                            settingsPreferences.edit().remove("offerTitle " + j).apply();
                            settingsPreferences.edit().remove("offerCattitle " + j).apply();
                            settingsPreferences.edit().remove("offerAreatitle " + j).apply();
                            settingsPreferences.edit().remove("offerLink " + j).apply();
                            settingsPreferences.edit().remove("offerDesc " + j).apply();
                            settingsPreferences.edit().remove("offerDate " + j).apply();
                            settingsPreferences.edit().remove("offerDownloaded " + j).apply();
                        }

                        for (int i = 0; i < asyncOffers.size(); i++) {
                            System.out.println(asyncOffers.get(i).getTitle() + " in the Array that fills settings ");
                        }
                        if (asyncOffers.size() > 0) {
                            for (int i = 0; i < asyncOffers.size(); i++) {
                                if (i < 5) {

                                    settingsPreferences.edit().putInt("offerId " + i, asyncOffers.get(i).getId()).apply();
                                    settingsPreferences.edit().putInt("offerCatid " + i, asyncOffers.get(i).getCatid()).apply();
                                    settingsPreferences.edit().putInt("offerAreaid " + i, asyncOffers.get(i).getAreaid()).apply();
                                    settingsPreferences.edit().putString("offerTitle " + i, asyncOffers.get(i).getTitle()).apply();
                                    settingsPreferences.edit().putString("offerCattitle " + i, asyncOffers.get(i).getCattitle()).apply();
                                    settingsPreferences.edit().putString("offerAreatitle " + i, asyncOffers.get(i).getAreatitle()).apply();
                                    settingsPreferences.edit().putString("offerLink " + i, asyncOffers.get(i).getLink()).apply();
                                    settingsPreferences.edit().putString("offerDesc " + i, asyncOffers.get(i).getDesc()).apply();
                                    settingsPreferences.edit().putLong("offerDate " + i, asyncOffers.get(i).getDate().getTime()).apply();
                                    settingsPreferences.edit().putString("offerDownloaded " + i, asyncOffers.get(i).getDownloaded()).apply();
                                    System.out.println(settingsPreferences.getLong("offerDate " + i, 0));
                                    System.out.println(settingsPreferences.getString("offerTitle " + i, ""));
                                    settingsPreferences.edit().putInt("numberOfOffers", asyncOffers.size()).apply();
                                } else
                                    settingsPreferences.edit().putInt("numberOfOffers", 5).apply();
                            }

                            settingsPreferences.edit().putLong("lastSeenDate", asyncOffers.get(0).getDate().getTime()).apply();
                            settingsPreferences.edit().putLong("lastNotDate", asyncOffers.get(0).getDate().getTime()).apply();

                            System.out.println(settingsPreferences.getLong("lastSeenDate", 0));
                        } else {
                            settingsPreferences.edit().putInt("numberOfOffers", 0).apply();
                        }

                        Intent intent = new Intent(SettingActivity.this,MainActivity.class);
                        startActivity(intent);




                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    message = "TimeOutError";
                    //This indicates that the reuest has either time out or there is no connection

                } else if (error instanceof AuthFailureError) {
                    message = "AuthFailureError";
                    // Error indicating that there was an Authentication Failure while performing the request

                } else if (error instanceof ServerError) {
                    message = "ServerError";
                    //Indicates that the server responded with a error response

                } else if (error instanceof NetworkError) {
                    message = "NetworkError";
                    //Indicates that there was network error while performing the request

                } else if (error instanceof ParseError) {
                    message = "ParseError";
                    // Indicates that the server response could not be parsed

                }
                System.out.println("Volley: " + message);
                if (!message.equals("")) {
                    Toast.makeText(SettingActivity.this, Utils.getServerError(), Toast.LENGTH_LONG).show();
                    Intent intentError = new Intent(SettingActivity.this, SettingActivity.class);
                    startActivity(intentError);
                }
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("jacat_id", param);
                params.put("jloc_id", param2);

                return params;
            }
        };
        Volley.newRequestQueue(MyApplication.getAppContext()).add(stringRequest);

    }


    public void volleySetDefault() {
        String url = Utils.getUrl()+"jobOfferCategories.php";
        categoriesIds = "";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        // Display the first 500 characters of the response string.
                        try {
                            JSONObject jsonObjectAll = new JSONObject(response);

                            JSONArray jsonArray = jsonObjectAll.getJSONArray("joboffercategories");
                            System.out.println(jsonArray.length());
                            settingsPreferences.edit().putInt("numberOfCategories", jsonArray.length()).apply();
                            settingsPreferences.edit().putInt("numberOfCheckedCategories", jsonArray.length()).apply();
                            System.out.println(settingsPreferences.getInt("numberOfCategories", 0));
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObjectCategory = jsonArray.getJSONObject(i);
                                settingsPreferences.edit().putInt("offerCategoryId " + i, Integer.valueOf(jsonObjectCategory.getString("jacat_id"))).apply();
                                settingsPreferences.edit().putInt("checkedCategoryId " + i, Integer.valueOf(jsonObjectCategory.getString("jacat_id"))).apply();
                                settingsPreferences.edit().putString("offerCategoryTitle " + i, jsonObjectCategory.getString("jacat_title")).apply();
                                settingsPreferences.edit().putString("checkedCategoryTitle " + i, jsonObjectCategory.getString("jacat_title")).apply();
                                if (categoriesIds.equals("")) {
                                    categoriesIds += jsonObjectCategory.getString("jacat_id") ;
                                } else
                                    categoriesIds += "," +jsonObjectCategory.getString("jacat_id");
                                System.out.println(categoriesIds.toString());


                                System.out.println(jsonObjectCategory.toString());
                                System.out.println(settingsPreferences.getInt("checkedCategoryId " + i, 0) + "In The Task set Default");
                                System.out.println(settingsPreferences.getString("checkedCategoryTitle " + i, ""));
                            }
                            System.out.println(settingsPreferences.getInt("numberOfCheckedCategories", 0));
                            volleySetDefaultAreas();


                        } catch (JSONException e) {

                            e.printStackTrace();
                            Intent intentError = new Intent(SettingActivity.this, MainActivity.class);
                            startActivity(intentError);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    message = "TimeOutError";
                    //This indicates that the reuest has either time out or there is no connection

                } else if (error instanceof AuthFailureError) {
                    message = "AuthFailureError";
                    // Error indicating that there was an Authentication Failure while performing the request

                } else if (error instanceof ServerError) {
                    message = "ServerError";
                    //Indicates that the server responded with a error response

                } else if (error instanceof NetworkError) {
                    message = "NetworkError";
                    //Indicates that there was network error while performing the request

                } else if (error instanceof ParseError) {
                    message = "ParseError";
                    // Indicates that the server response could not be parsed

                }
                System.out.println("Volley: " + message);
                if (!message.equals("")) {
                    Toast.makeText(SettingActivity.this, Utils.getServerError(), Toast.LENGTH_LONG).show();
                    Intent intentError = new Intent(SettingActivity.this, MainActivity.class);
                    startActivity(intentError);
                }
            }
        }
        );
        Volley.newRequestQueue(MyApplication.getAppContext()).add(stringRequest);
    }

    public void volleySetCheckedCategories(final String param, final String param2) {
        String url = Utils.getUrl()+"jobAdsArray.php?";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // Display the first 500 characters of the response string.
                        try {
                            JSONObject jsonObjectAll = new JSONObject(response);
                            JSONArray jsonArray = jsonObjectAll.getJSONArray("offers");
                            int i = 0;

                            while (i < jsonArray.length() && i < 5) {


                                JSONObject jsonObjectCategory = jsonArray.getJSONObject(i);

                                JobOffer offer = new JobOffer();
                                offer.setId(Integer.valueOf(jsonObjectCategory.getString("jad_id")));
                                offer.setCatid(Integer.valueOf(jsonObjectCategory.getString("jad_catid")));
                                offer.setAreaid(Integer.valueOf(jsonObjectCategory.getString("jloc_id")));
                                offer.setTitle(jsonObjectCategory.getString("jad_title"));
                                offer.setCattitle(jsonObjectCategory.getString("jacat_title"));
                                offer.setAreatitle(jsonObjectCategory.getString("jloc_title"));
                                offer.setLink(jsonObjectCategory.getString("jad_link"));
                                offer.setDesc(jsonObjectCategory.getString("jad_desc"));
                                offer.setDate(format.parse(jsonObjectCategory.getString("jad_date")));
                                offer.setDownloaded(jsonObjectCategory.getString("jad_downloaded"));
                                System.out.println(offer.getTitle() + " first time");

                                asyncOffers.add(offer);

                                Collections.sort(asyncOffers, new Comparator<JobOffer>() {
                                    @Override
                                    public int compare(JobOffer jobOffer, JobOffer t1) {
                                        if (jobOffer.getDate().getTime() - t1.getDate().getTime() < 0)
                                            return 1;
                                        else if (jobOffer.getDate().getTime() - t1.getDate().getTime() == 0)
                                            return 0;
                                        else
                                            return -1;
                                    }
                                });
                                for (int x = 0; x < asyncOffers.size(); x++) {
                                    System.out.println(asyncOffers.get(x).getTitle());
                                }


                                i++;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        for (int j = 0; j < 5; j++) {
                            settingsPreferences.edit().remove("offerId " + j).apply();
                            settingsPreferences.edit().remove("offerAreaid " + j).apply();
                            settingsPreferences.edit().remove("offerCatid " + j).apply();
                            settingsPreferences.edit().remove("offerTitle " + j).apply();
                            settingsPreferences.edit().remove("offerCattitle " + j).apply();
                            settingsPreferences.edit().remove("offerAreatitle " + j).apply();
                            settingsPreferences.edit().remove("offerLink " + j).apply();
                            settingsPreferences.edit().remove("offerDesc " + j).apply();
                            settingsPreferences.edit().remove("offerDate " + j).apply();
                            settingsPreferences.edit().remove("offerDownloaded " + j).apply();
                        }

                        for (int i = 0; i < asyncOffers.size(); i++) {
                            System.out.println(asyncOffers.get(i).getTitle() + " in the Array that fills settings ");
                        }

                        if (asyncOffers.size() > 0) {
                            for (int i = 0; i < asyncOffers.size(); i++) {
                                if (i < 5) {

                                    settingsPreferences.edit().putInt("offerId " + i, asyncOffers.get(i).getId()).apply();
                                    settingsPreferences.edit().putInt("offerCatid " + i, asyncOffers.get(i).getCatid()).apply();
                                    settingsPreferences.edit().putInt("offerAreaid " + i, asyncOffers.get(i).getAreaid()).apply();
                                    settingsPreferences.edit().putString("offerTitle " + i, asyncOffers.get(i).getTitle()).apply();
                                    settingsPreferences.edit().putString("offerCattitle " + i, asyncOffers.get(i).getCattitle()).apply();
                                    settingsPreferences.edit().putString("offerAreatitle " + i, asyncOffers.get(i).getAreatitle()).apply();
                                    settingsPreferences.edit().putString("offerLink " + i, asyncOffers.get(i).getLink()).apply();
                                    settingsPreferences.edit().putString("offerDesc " + i, asyncOffers.get(i).getDesc()).apply();
                                    settingsPreferences.edit().putLong("offerDate " + i, asyncOffers.get(i).getDate().getTime()).apply();
                                    settingsPreferences.edit().putString("offerDownloaded " + i, asyncOffers.get(i).getDownloaded()).apply();
                                    System.out.println(settingsPreferences.getLong("offerDate " + i, 0));
                                    System.out.println(settingsPreferences.getString("offerTitle " + i, ""));
                                    settingsPreferences.edit().putInt("numberOfOffers", asyncOffers.size()).apply();
                                } else
                                    settingsPreferences.edit().putInt("numberOfOffers", 5).apply();
                            }

                            settingsPreferences.edit().putLong("lastSeenDate", asyncOffers.get(0).getDate().getTime()).apply();
                            settingsPreferences.edit().putLong("lastNotDate", asyncOffers.get(0).getDate().getTime()).apply();

                            System.out.println(settingsPreferences.getLong("lastSeenDate", 0));

                        }
                        System.out.println(settingsPreferences.getInt("numberOfCheckedCategories", 0));

                        Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                        startActivity(intent);


                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    message = "TimeOutError";
                    //This indicates that the reuest has either time out or there is no connection

                } else if (error instanceof AuthFailureError) {
                    message = "AuthFailureError";
                    // Error indicating that there was an Authentication Failure while performing the request

                } else if (error instanceof ServerError) {
                    message = "ServerError";
                    //Indicates that the server responded with a error response

                } else if (error instanceof NetworkError) {
                    message = "NetworkError";
                    //Indicates that there was network error while performing the request

                } else if (error instanceof ParseError) {
                    message = "ParseError";
                    // Indicates that the server response could not be parsed

                }
                System.out.println("Volley: " + message);
                if (!message.equals("")) {
                    Toast.makeText(SettingActivity.this, Utils.getServerError(), Toast.LENGTH_LONG).show();
                    Intent intentError = new Intent(SettingActivity.this, MainActivity.class);
                    startActivity(intentError);
                }
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("jacat_id", param);
                params.put("jloc_id", param2);

                return params;
            }
        };
        Volley.newRequestQueue(SettingActivity.this).add(stringRequest);
    }


    public void volleySetDefaultAreas() {
        String url = Utils.getUrl()+"jobOfferAreas.php";
        areasIds = "";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        // Display the first 500 characters of the response string.
                        try {
                            JSONObject jsonObjectAll = new JSONObject(response);

                            JSONArray jsonArray = jsonObjectAll.getJSONArray("jobofferareas");
                            System.out.println(jsonArray.length());
                            settingsPreferences.edit().putInt("numberOfAreas", jsonArray.length()).apply();
                            settingsPreferences.edit().putInt("numberOfCheckedAreas", jsonArray.length()).apply();
                            System.out.println(settingsPreferences.getInt("numberOfAreas", 0));
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObjectCategory = jsonArray.getJSONObject(i);
                                settingsPreferences.edit().putInt("offerAreaId " + i, Integer.valueOf(jsonObjectCategory.getString("jloc_id"))).apply();
                                settingsPreferences.edit().putInt("checkedAreaId " + i, Integer.valueOf(jsonObjectCategory.getString("jloc_id"))).apply();
                                settingsPreferences.edit().putString("offerAreaTitle " + i, jsonObjectCategory.getString("jloc_title")).apply();
                                settingsPreferences.edit().putString("checkedAreaTitle " + i, jsonObjectCategory.getString("jloc_title")).apply();
                                if (areasIds.equals("")) {
                                    areasIds += jsonObjectCategory.getString("jloc_id");
                                } else
                                    areasIds +=  ","+jsonObjectCategory.getString("jloc_id");
                                System.out.println(areasIds.toString());

                                System.out.println(jsonObjectCategory.toString());
                                System.out.println(settingsPreferences.getInt("checkedAreaId " + i, 0) + "In The Task set Default");
                                System.out.println(settingsPreferences.getString("checkedAreaTitle " + i, ""));
                            }
                            System.out.println(settingsPreferences.getInt("numberOfCheckedAreas", 0));

                            volleySetCheckedCategories(categoriesIds, areasIds);


                        } catch (JSONException e) {

                            e.printStackTrace();
                            Intent intentError = new Intent(SettingActivity.this, MainActivity.class);
                            startActivity(intentError);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    message = "TimeOutError";
                    //This indicates that the reuest has either time out or there is no connection

                } else if (error instanceof AuthFailureError) {
                    message = "AuthFailureError";
                    // Error indicating that there was an Authentication Failure while performing the request

                } else if (error instanceof ServerError) {
                    message = "ServerError";
                    //Indicates that the server responded with a error response

                } else if (error instanceof NetworkError) {
                    message = "NetworkError";
                    //Indicates that there was network error while performing the request

                } else if (error instanceof ParseError) {
                    message = "ParseError";
                    // Indicates that the server response could not be parsed

                }
                System.out.println("Volley: " + message);
                if (!message.equals("")) {
                    Toast.makeText(SettingActivity.this, Utils.getServerError(), Toast.LENGTH_LONG).show();
                    Intent intentError = new Intent(SettingActivity.this, MainActivity.class);
                    startActivity(intentError);
                }
            }
        }
        );
        Volley.newRequestQueue(SettingActivity.this).add(stringRequest);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        queue.stop();
    }




}





