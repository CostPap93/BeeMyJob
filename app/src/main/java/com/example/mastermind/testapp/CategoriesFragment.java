package com.example.mastermind.testapp;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kostas on 3/6/2018.
 */

public class CategoriesFragment extends Fragment {

    SharedPreferences settingsPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_categories, container, false);

        settingsPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());
        checkBox = rootView.findViewById(R.id.chbox_category);

        btnSave = rootView.findViewById(R.id.btn_save_categories);
        btnCancel = rootView.findViewById(R.id.btn_cancel);
        radioButton = rootView.findViewById(R.id.rb_day);
        radioButton1 = rootView.findViewById(R.id.rb_once);
        radioButton2 = rootView.findViewById(R.id.rb_twice);
        checkIsChanged = new ArrayList<>();
        asyncOffers = new ArrayList<>();
        offers = new ArrayList<>();
        categories = new ArrayList<>();
        areas = new ArrayList<>();
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


        if (queue == null) {
            queue = Volley.newRequestQueue(MyApplication.getAppContext());
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

            categoriesRecyclerView = rootView.findViewById(R.id.lv_categories);

            //RecyclerView layout manager
            LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(MyApplication.getAppContext());
            categoriesRecyclerView.setLayoutManager(recyclerLayoutManager);

            //RecyclerView item decorator
            DividerItemDecoration dividerItemDecoration =
                    new DividerItemDecoration(categoriesRecyclerView.getContext(),
                            recyclerLayoutManager.getOrientation());
            categoriesRecyclerView.addItemDecoration(dividerItemDecoration);

            //RecyclerView adapater
            CheckRecycleAdapter recyclerViewAdapter = new
                    CheckRecycleAdapter(categories,MyApplication.getAppContext());
            categoriesRecyclerView.setAdapter(recyclerViewAdapter);
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> categoriesNames = new ArrayList<>();
                OfferCategory offerCategory;
                offerCategories = new ArrayList<>();
                categoriesIds = "";
                areasIds ="";
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

                    for (int j = 0; j < settingsPreferences.getInt("numberOfCategories", 0); j++) {
                        settingsPreferences.edit().remove("checkedCategoryId "+ j).apply();
                        settingsPreferences.edit().remove("checkedCategoryTitle "+ j).apply();
                    }

                    for (int j = 0; j < settingsPreferences.getInt("numberOfCategories", 0); j++) {
                        for(OfferCategory oc : offerCategories) {
                            if(oc.getCatid() == settingsPreferences.getInt("offerCategoryId "+j,0)) {

                                settingsPreferences.edit().putInt("checkedCategoryId " + j, oc.getCatid()).apply();
                                settingsPreferences.edit().putString("checkedCategoryTitle " + j, oc.getTitle()).apply();
                            }
                        }
                    }

                    for (OfferCategory oc : offerCategories) {
                        if (categoriesIds.equals("")) {
                            categoriesIds += oc.getCatid();
                        } else {
                            categoriesIds += "," + oc.getCatid();
                        }

                    }
                    for(int i =0;i<settingsPreferences.getInt("numberOfCheckedAreas",0);i++){
                        if (areasIds.equals("")){
                            areasIds += settingsPreferences.getString("checkedAreaTitle "+i,"");
                        } else {
                            areasIds += "," + settingsPreferences.getString("checkedAreaTitle "+i,"");

                        }
                    }

                    volleySaveOffers(categoriesIds, areasIds);
                }
            }
        });


        return rootView;


    }

    public void RefreshOperation() {

        if (queue == null) {
            queue = Volley.newRequestQueue(MyApplication.getAppContext());
        }
        if(isConn()) {
            queue.add(volleyUpdateDefault());
        }else {
            Toast.makeText(MyApplication.getAppContext(),"Πρέπει να είστε συνδεδεμένος στο ίντερνετ για να κάνετε ανανέωση!",Toast.LENGTH_LONG).show();
        }
    }

    public boolean isConn() {
        ConnectivityManager connectivityManager = (ConnectivityManager) MyApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isWifiConn = networkInfo.isConnected();
        networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isMobileConn = networkInfo.isConnected();
        Log.d("connection", "Wifi connected: " + isWifiConn);
        Log.d("connection", "Mobile connected: " + isMobileConn);
        return isWifiConn || isMobileConn;
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

//                            if (settingsPreferences.getInt("numberOfCategories", 0) != 0) {
//                                for (int i = 0; i < settingsPreferences.getInt("numberOfCategories", 0); i++) {
//                                    OfferCategory category = new OfferCategory();
//                                    category.setCatid(settingsPreferences.getInt("offerCategoryId " + i, 0));
//                                    category.setTitle(settingsPreferences.getString("offerCategoryTitle " + i, ""));
//                                    categoriesRefresh.add(category);
//                                    System.out.println(categoriesRefresh.get(i).getTitle() + "checkBoxAdapter");
//                                }
//
//                                //RecyclerView layout manager
//                                LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(MyApplication.getAppContext());
//                                categoriesRecyclerView.setLayoutManager(recyclerLayoutManager);
//
//                                //RecyclerView item decorator
//                                DividerItemDecoration dividerItemDecoration =
//                                        new DividerItemDecoration(categoriesRecyclerView.getContext(),
//                                                recyclerLayoutManager.getOrientation());
//                                categoriesRecyclerView.addItemDecoration(dividerItemDecoration);
//
//                                //RecyclerView adapater
//                                CheckRecycleAdapter recyclerViewAdapter = new
//                                        CheckRecycleAdapter(categoriesRefresh,MyApplication.getAppContext());
//                                categoriesRecyclerView.setAdapter(recyclerViewAdapter);

//                            }
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
                    Toast.makeText(MyApplication.getAppContext(), Utils.getServerError(), Toast.LENGTH_LONG).show();
                    Intent intentError = new Intent(MyApplication.getAppContext(), SettingActivity.class);
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
                    Toast.makeText(MyApplication.getAppContext(), Utils.getServerError(), Toast.LENGTH_LONG).show();
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


}