package com.example.mastermind.testapp;

import android.app.AlertDialog;
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
import android.widget.ListView;
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

public class AreasFragment extends Fragment {

    SharedPreferences settingsPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());

    CheckBox checkBox;
    AlertDialog alertDialog;
    AlertDialog.Builder dialogBuilder;

    Button btnSave;
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
    MyListView lv_areas;

    RequestQueue queue;
    String areasIds, categoriesIds;
    private RecyclerView categoriesRecyclerView;
    private RecyclerView areasRecyclerView;

    AreasFragmentListener activityCommander;

    public interface AreasFragmentListener{
        public void changeOffers2();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_areas, container, false);

        settingsPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());
        checkBox = rootView.findViewById(R.id.chbox_category);

        btnSave =rootView.findViewById(R.id.btn_save_areas);
        checkIsChanged = new ArrayList<>();
        asyncOffers = new ArrayList<>();
        offers = new ArrayList<>();
        categories = new ArrayList<>();
        areas = new ArrayList<>();
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        areasIds = "";
        categoriesIds = "";

        if (queue == null) {
            queue = Volley.newRequestQueue(MyApplication.getAppContext());
        }


        for (int i = 0; i < settingsPreferences.getInt("numberOfCheckedAreas", 0); i++) {
            if (areasIds.equals("")) {
                areasIds += settingsPreferences.getInt("checkedAreaId " + i, 0);
            } else {
                areasIds += "," + settingsPreferences.getInt("checkedAreaId " + i, 0);
            }
        }

        if (settingsPreferences.getInt("numberOfAreas", 0) != 0) {
            for (int i = 0; i < settingsPreferences.getInt("numberOfAreas", 0); i++) {
                OfferArea area = new OfferArea();
                area.setAreaid(settingsPreferences.getInt("offerAreaId " + i, 0));
                area.setTitle(settingsPreferences.getString("offerAreaTitle " + i, ""));
                areas.add(area);
            }

            lv_areas = rootView.findViewById(R.id.lv_areas);
            CheckBoxAreaAdapter checkBoxAreaAdapter = new CheckBoxAreaAdapter(getContext(),areas);
            lv_areas.setAdapter(checkBoxAreaAdapter);

        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> areasNames = new ArrayList<>();
                OfferArea offerArea;
                offerAreas = new ArrayList<>();
                categoriesIds = "";
                areasIds ="";
                if (isConn()) {

                    for (int i = 0; i < lv_areas.getChildCount(); i++) {
                        checkBox = lv_areas.getChildAt(i).findViewById(R.id.chbox_category);
                        if (checkBox.isChecked()) {
                            areasNames.add(checkBox.getText().toString());
                        }
                    }
                    if(!areasNames.isEmpty()) {

                        for (int j = 0; j < settingsPreferences.getInt("numberOfAreas", 0); j++) {
                            offerArea = new OfferArea();
                            offerArea.setAreaid(settingsPreferences.getInt("offerAreaId " + j, 0));
                            offerArea.setTitle(settingsPreferences.getString("offerAreaTitle " + j, ""));
                            for (String name : areasNames) {
                                if (name.equals(settingsPreferences.getString("offerAreaTitle " + j, ""))) {
                                    offerAreas.add(offerArea);
                                }
                            }
                        }

                        for (int j = 0; j < settingsPreferences.getInt("numberOfAreas", 0); j++) {
                            settingsPreferences.edit().remove("checkedAreaId " + j).apply();
                            settingsPreferences.edit().remove("checkedAreaTitle " + j).apply();
                        }
                        for (int j = 0; j < settingsPreferences.getInt("numberOfAreas", 0); j++) {
                            for (OfferArea oc : offerAreas) {
                                if (oc.getAreaid() == settingsPreferences.getInt("offerAreaId " + j, 0)) {

                                    settingsPreferences.edit().putInt("checkedAreaId " + j, oc.getAreaid()).apply();
                                    settingsPreferences.edit().putString("checkedAreaTitle " + j, oc.getTitle()).apply();
                                }
                            }
                        }


                        for (OfferArea oc : offerAreas) {
                            if (areasIds.equals("")) {
                                areasIds += oc.getAreaid();
                            } else {
                                areasIds += "," + oc.getAreaid();
                            }

                        }
                        for (int i = 0; i < settingsPreferences.getInt("numberOfCheckedCategories", 0); i++) {
                            if (categoriesIds.equals("")) {
                                categoriesIds += settingsPreferences.getInt("checkedCategoryId " + i, 0);
                            } else {
                                categoriesIds += "," + settingsPreferences.getInt("checkedCategoryId " + i, 0);

                            }
                        }

                        volleySaveOffers(categoriesIds, areasIds);
                    }else {
                        Toast.makeText(getContext(),"Πρέπει να επιλέξετε τουλάχιστον μία περιοχή",Toast.LENGTH_LONG).show();
                    }
                }
            }
        });


        return rootView;
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




    public void volleySaveOffers(final String param, final String param2) {

        String url = Utils.getUrl()+"jobAdsArray.php?";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        ArrayList<OfferCategory> offerCategories = new ArrayList<>();

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


                                i++;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }


                        for (int j = 0; j < settingsPreferences.getInt("numberOfAreas", 0); j++) {
                            settingsPreferences.edit().remove("checkedAreaId " + j).apply();
                            settingsPreferences.edit().remove("checkedAreaTitle " + j).apply();
                        }

                        for (OfferArea oa : offerAreas) {

                            settingsPreferences.edit().putInt("checkedAreaId " + offerAreas.indexOf(oa), oa.getAreaid()).apply();
                            settingsPreferences.edit().putString("checkedAreaTitle " + offerAreas.indexOf(oa), oa.getTitle()).apply();

                        }

                        for (OfferArea oa : offerAreas) {
                            OfferArea offerCategory1 = new OfferArea();
                            offerCategory1.setAreaid(settingsPreferences.getInt("checkedAreaId " + offerAreas.indexOf(oa), oa.getAreaid()));
                            offerCategory1.setTitle(settingsPreferences.getString("checkedAreaTitle " + offerAreas.indexOf(oa), oa.getTitle()));
                        }

                        for (int j = 0; j < settingsPreferences.getInt("numberOfCheckedCategories", 0); j++) {
                            OfferCategory oc = new OfferCategory();
                            oc.setCatid(settingsPreferences.getInt("checkedCategoryId "+j,0));
                            oc.setTitle(settingsPreferences.getString("checkedCategoryTitle "+j,""));
                            offerCategories.add(oc);
                        }


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
                                    settingsPreferences.edit().putInt("numberOfOffers", asyncOffers.size()).apply();
                                } else
                                    settingsPreferences.edit().putInt("numberOfOffers", 5).apply();
                            }

                            settingsPreferences.edit().putLong("lastSeenDate", asyncOffers.get(0).getDate().getTime()).apply();
                            settingsPreferences.edit().putLong("lastNotDate", asyncOffers.get(0).getDate().getTime()).apply();
                        } else {
                            settingsPreferences.edit().putInt("numberOfOffers", 0).apply();
                        }

                        HideProgressDialog();


//
//                        activityCommander.changeOffers2();


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
                HideProgressDialog();
                Toast.makeText(MyApplication.getAppContext(), Utils.getServerError(), Toast.LENGTH_LONG).show();

            }
        }
        )
        {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("jacat_id", param);
                params.put("jloc_id", param2);

                return params;
            }
        }

        ;
        Volley.newRequestQueue(MyApplication.getAppContext()).add(stringRequest);

        ShowProgressDialog();

    }


    public void ShowProgressDialog() {
        dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View dialogView = inflater.inflate(R.layout.progress_dialog_layout, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    public void HideProgressDialog(){

        alertDialog.dismiss();
    }
}