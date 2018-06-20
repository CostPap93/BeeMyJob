package com.example.mastermind.testapp;


import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.view.ViewGroup;
import android.widget.ActionMenuView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Ref;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends FragmentActivity implements CategoriesFragment.CategoriesFragmentListener
{
    SharedPreferences settingsPreferences;
    ArrayList<JobOffer> offers;
    ArrayList<JobOffer> asyncOffers;

    AlertDialog.Builder dialogBuilder;
    AlertDialog alertDialog;

    ImageButton imgBtn_ad;

    ListView lv;
    MyListView lv_categories;
    MyListView lv_areas;
    DateFormat format;
    String message = "";
    RequestQueue queue;

    String areaIds;
    String categoriesIds;
    String[] paths;


    private static final int NUM_PAGES = 4;
    private ViewPager mPager;
    private ScreenSlidePagerAdapter mPagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("BeeMyJob");
        toolbar.inflateMenu(R.menu.menu_main);
        ActionMenuItemView menuItem = findViewById(R.id.menu_refresh);
        menuItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RefreshOperation();



            }
        });


        lv = findViewById(R.id.listView);
        lv_categories = findViewById(R.id.lv_categories);
        lv_areas = findViewById(R.id.lv_areas);
        asyncOffers = new ArrayList<>();
        offers = new ArrayList<>();
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        settingsPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        imgBtn_ad = findViewById(R.id.imgBtn_ad);

        TabLayout tabLayout = findViewById(R.id.tabs);


        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);

        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mPager));

        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(0);


    }

    @Override
    public void changeOffers() {

        ShowProgressDialog();
        refreshOffers();
        HideProgressDialog();

    }

    @Override
    protected void onStop() {
        // A service can be "started" and/or "bound". In this case, it's "started" by this Activity
        // and "bound" to the JobScheduler (also called "Scheduled" by the JobScheduler). This call
        // to stopService() won't prevent scheduled jobs to be processed. However, failing
        // to call stopService() would keep it alive indefinitely.
        stopService(new Intent(this, NetworkSchedulerService.class));
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Start service and provide it a way to communicate with this class.
        Intent startServiceIntent = new Intent(this, NetworkSchedulerService.class);
        startService(startServiceIntent);
    }

    public void RefreshOperation() {


        if (isConn() && settingsPreferences.getInt("numberOfCategories",0)!=0 && settingsPreferences.getInt("numberOfAreas",0)!=0  ) {
            categoriesIds = "";
            areaIds = "";

            if (queue == null) {
                queue = Volley.newRequestQueue(this);
            }

            volleyUpdate();


        }else if(isConn() && settingsPreferences.getInt("numberOfCategories",0)==0 && settingsPreferences.getInt("numberOfAreas",0)==0  ){
            if (queue == null) {
                queue = Volley.newRequestQueue(this);
            }
            volleySetDefault();

        }
        else {
            Toast.makeText(MainActivity.this, "Πρέπει να είστε συνδεδεμένος στο ίντερνετ για να κάνετε ανανέωση!", Toast.LENGTH_LONG).show();
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




    public void volleySetCheckedCategories(final String param, final String param2) {
        String url = Utils.getUrl() + "jobAdsArray.php?";


        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ArrayList<JobOffer> offersRefresh = new ArrayList<>();
                        asyncOffers.clear();


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
                        if (asyncOffers.size() > 0) {
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


                        }


                        for (int i = 0; i < settingsPreferences.getInt("numberOfOffers", 0); i++) {

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
                            offersRefresh.add(jobOffer);

                        }


                        if (mPager.getCurrentItem() == 0) {
                            Fragment fragment = mPagerAdapter.getRegisteredFragment(0);
                            OffersFragment offersFragment1 = (OffersFragment) fragment;

                            ListView listView = offersFragment1.getView().findViewById(R.id.listView);
                            if (listView == null) {
                                Toast.makeText(MainActivity.this, String.valueOf(listView == null), Toast.LENGTH_LONG).show();
                            } else {
                                JobOfferAdapter jobOfferAdapter = new JobOfferAdapter(getApplicationContext(), offersRefresh);

                                listView.setAdapter(jobOfferAdapter);

                                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                        Intent intentToDetail = new Intent(MainActivity.this, DetailActivity.class);
                                        intentToDetail.putExtra("jobOffer", (Serializable) adapterView.getItemAtPosition(i));
                                        startActivity(intentToDetail);

                                    }
                                });

                                jobOfferAdapter.notifyDataSetChanged();
                                if(settingsPreferences.getInt("numberOfImages",0)!=0) {
                                    imgBtn_ad = offersFragment1.getView().findViewById(R.id.imgBtn_ad);


                                    if (settingsPreferences.getInt("numberOfImages", 0) > 0) {
                                        paths = new String[settingsPreferences.getInt("numberOfImages", 0)];

                                        for (int i = 1; i <= paths.length; i++) {
                                            paths[i - 1] = settingsPreferences.getString("imageUri" + i, "");
                                        }
                                        ArrayList<Bitmap> bitmaps = new ArrayList<>();
                                        for (String path : paths) {


                                            try {
                                                File d = new File(path);
                                                bitmaps.add(BitmapFactory.decodeStream(new FileInputStream(d)));
                                            } catch (FileNotFoundException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        Random r = new Random();
                                        int rnum = r.nextInt(paths.length);
                                        imgBtn_ad.setVisibility(View.VISIBLE);
                                        imgBtn_ad.setImageBitmap(bitmaps.get(rnum));
                                    }
                                }else{
                                    volleyImageNames();
                                }

                            }
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
                Toast.makeText(MainActivity.this, Utils.getServerError(), Toast.LENGTH_LONG).show();
                Intent intentError = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intentError);

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
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {


        SparseArray<Fragment> allFragments = new SparseArray<>();
        SparseArray<Fragment> registeredFragments = new SparseArray<>();

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            allFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    OffersFragment offersFragment = new OffersFragment();
                    allFragments.put(0, offersFragment);
                    return new OffersFragment();
                case 1:
                    CategoriesFragment categoriesFragment = new CategoriesFragment();
                    allFragments.put(1, categoriesFragment);
                    return new CategoriesFragment();
                case 2:
                    AreasFragment areasFragment = new AreasFragment();
                    allFragments.put(2, areasFragment);
                    return new AreasFragment();
                case 3:
                    TimesFragment timesFragment = new TimesFragment();
                    allFragments.put(3, timesFragment);
                    return new TimesFragment();
                default:
                    return null;
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }

        public SparseArray<Fragment> getAllFragments() {
            return allFragments;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

    }

    public void volleyUpdate() {
        String url = Utils.getUrl() + "jobOfferCategories.php";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ArrayList<OfferCategory> categoriesRefresh = new ArrayList<>();
                        Fragment fragment = new CategoriesFragment();

                        // Display the first 500 characters of the response string.
                        try {
                            JSONObject jsonObjectAll = new JSONObject(response);

                            JSONArray jsonArray = jsonObjectAll.getJSONArray("joboffercategories");

                            settingsPreferences.edit().putInt("numberOfCategories", jsonArray.length()).apply();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObjectCategory = jsonArray.getJSONObject(i);
                                settingsPreferences.edit().putInt("offerCategoryId " + i, Integer.valueOf(jsonObjectCategory.getString("jacat_id"))).apply();
                                settingsPreferences.edit().putString("offerCategoyTitle " + i, jsonObjectCategory.getString("jacat_title")).apply();

                            }

                            if (settingsPreferences.getInt("numberOfCategories", 0) != 0) {
                                for (int i = 0; i < settingsPreferences.getInt("numberOfCategories", 0); i++) {
                                    OfferCategory category = new OfferCategory();
                                    category.setCatid(settingsPreferences.getInt("offerCategoryId " + i, 0));
                                    category.setTitle(settingsPreferences.getString("offerCategoryTitle " + i, ""));
                                    categoriesRefresh.add(category);
                                }
                                if (mPager.getCurrentItem() == 1) {

                                    Fragment fragment1 = mPagerAdapter.getRegisteredFragment(1);
                                    CategoriesFragment categoriesFragment = (CategoriesFragment) fragment1;

                                    lv_categories = categoriesFragment.getView().findViewById(R.id.lv_categories);
                                    CheckBoxAdapter checkBoxAdapter = new CheckBoxAdapter(MainActivity.this,categoriesRefresh);
                                    lv_categories.setAdapter(checkBoxAdapter);

                                    checkBoxAdapter.notifyDataSetChanged();

                                }

                            }

                            volleyUpdateAreas();
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
                Toast.makeText(MainActivity.this, Utils.getServerError(), Toast.LENGTH_LONG).show();
            }
        }
        );
        Volley.newRequestQueue(MyApplication.getAppContext()).add(stringRequest);
    }

    public void volleyUpdateAreas() {
        String url = Utils.getUrl() + "jobOfferAreas.php";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ArrayList<OfferArea> areasRefresh = new ArrayList<>();
                        Fragment fragment = new AreasFragment();
                        // Display the first 500 characters of the response string.
                        try {
                            JSONObject jsonObjectAll = new JSONObject(response);

                            JSONArray jsonArray = jsonObjectAll.getJSONArray("jobofferareas");

                            settingsPreferences.edit().putInt("numberOfAreas", jsonArray.length()).apply();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObjectCategory = jsonArray.getJSONObject(i);
                                settingsPreferences.edit().putInt("offerAreaId " + i, Integer.valueOf(jsonObjectCategory.getString("jloc_id"))).apply();
                                settingsPreferences.edit().putString("offerAreaTitle " + i, jsonObjectCategory.getString("jloc_title")).apply();

                            }

                            if (settingsPreferences.getInt("numberOfAreas", 0) != 0) {
                                for (int i = 0; i < settingsPreferences.getInt("numberOfAreas", 0); i++) {
                                    OfferArea area = new OfferArea();
                                    area.setAreaid(settingsPreferences.getInt("offerAreaId " + i, 0));
                                    area.setTitle(settingsPreferences.getString("offerAreaTitle " + i, ""));
                                    areasRefresh.add(area);
                                }
                                if (mPager.getCurrentItem() == 2) {

                                    Fragment fragment2 = mPagerAdapter.getRegisteredFragment(2);
                                    AreasFragment areasFragment = (AreasFragment) fragment2;
                                    lv_areas = areasFragment.getView().findViewById(R.id.lv_areas);
                                    CheckBoxAreaAdapter checkBoxAreaAdapter = new CheckBoxAreaAdapter(MainActivity.this, areasRefresh);
                                    lv_areas.setAdapter(checkBoxAreaAdapter);

                                    checkBoxAreaAdapter.notifyDataSetChanged();

                                }

                            }

                            categoriesIds="";
                            areaIds="";
                            for (int v = 0; v < (settingsPreferences.getInt("numberOfCheckedCategories", 0)); v++) {
                                if (categoriesIds.equals("")) {
                                    categoriesIds += settingsPreferences.getInt("checkedCategoryId " + v, 0);
                                } else
                                    categoriesIds += "," + settingsPreferences.getInt("checkedCategoryId " + v, 0);
                            }
                            for (int v = 0; v < (settingsPreferences.getInt("numberOfCheckedAreas", 0)); v++) {
                                if (areaIds.equals("")) {
                                    areaIds += settingsPreferences.getInt("checkedAreaId " + v, 0);
                                } else
                                    areaIds += "," + settingsPreferences.getInt("checkedAreaId " + v, 0);
                            }
                            volleySetCheckedCategories(areaIds, categoriesIds);
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
                    Toast.makeText(MainActivity.this, Utils.getServerError(), Toast.LENGTH_LONG).show();
            }
        }
        );
        Volley.newRequestQueue(MyApplication.getAppContext()).add(stringRequest);
    }

    public void refreshOffers() {
        ArrayList<JobOffer> offersRefresh = new ArrayList<>();
        JobOfferAdapter jobOfferAdapter = new JobOfferAdapter(getApplicationContext(), offersRefresh);
        jobOfferAdapter.clear();
        for (int i = 0; i < settingsPreferences.getInt("numberOfOffers", 0); i++) {

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
            offersRefresh.add(jobOffer);

        }


        OffersFragment offersFragment2 = (OffersFragment) mPagerAdapter.getAllFragments().get(0);

        ListView listView = offersFragment2.getView().findViewById(R.id.listView);
        if (listView == null) {
            Toast.makeText(MainActivity.this, String.valueOf(listView == null), Toast.LENGTH_LONG).show();
        } else {



            listView.setAdapter(jobOfferAdapter);
            jobOfferAdapter.notifyDataSetChanged();


            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intentToDetail = new Intent(MainActivity.this, DetailActivity.class);
                    intentToDetail.putExtra("jobOffer", (Serializable) adapterView.getItemAtPosition(i));
                    startActivity(intentToDetail);

                }
            });


            imgBtn_ad = offersFragment2.getView().findViewById(R.id.imgBtn_ad);


            if (settingsPreferences.getInt("numberOfImages", 0) > 0) {
                paths = new String[settingsPreferences.getInt("numberOfImages", 0)];

                for (int i = 1; i <= paths.length; i++) {
                    paths[i - 1] = settingsPreferences.getString("imageUri" + i, "");
                }
                ArrayList<Bitmap> bitmaps = new ArrayList<>();
                for (String path : paths) {


                    try {
                        File d = new File(path);
                        bitmaps.add(BitmapFactory.decodeStream(new FileInputStream(d)));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                Random r = new Random();
                int rnum = r.nextInt(paths.length);
                imgBtn_ad.setVisibility(View.VISIBLE);
                imgBtn_ad.setImageBitmap(bitmaps.get(rnum));
            }

        }
    }


    public void ShowProgressDialog() {
        dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View dialogView = inflater.inflate(R.layout.progress_dialog_layout, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    public void HideProgressDialog(){

        alertDialog.dismiss();
    }

    public void volleySetDefault(){
        String url =Utils.getUrl()+"jobOfferCategories.php";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ArrayList<OfferCategory> categoriesRefresh = new ArrayList();


                        // Display the first 500 characters of the response string.
                        try {
                            JSONObject jsonObjectAll = new JSONObject(response);

                            JSONArray jsonArray = jsonObjectAll.getJSONArray("joboffercategories");
                            settingsPreferences.edit().putInt("numberOfCategories", jsonArray.length()).apply();
                            settingsPreferences.edit().putInt("numberOfCheckedCategories", jsonArray.length()).apply();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObjectCategory = jsonArray.getJSONObject(i);
                                settingsPreferences.edit().putInt("offerCategoryId " + i, Integer.valueOf(jsonObjectCategory.getString("jacat_id"))).apply();
                                settingsPreferences.edit().putInt("checkedCategoryId " + i, Integer.valueOf(jsonObjectCategory.getString("jacat_id"))).apply();
                                settingsPreferences.edit().putString("offerCategoryTitle " + i, jsonObjectCategory.getString("jacat_title")).apply();
                                settingsPreferences.edit().putString("checkedCategoryTitle " + i, jsonObjectCategory.getString("jacat_title")).apply();

                            }

                            for(int i=0;i<settingsPreferences.getInt("numberOfCategories",0);i++){
                                OfferCategory oa = new OfferCategory();
                                oa.setCatid(settingsPreferences.getInt("offerCategoryId " +i,0));
                                oa.setTitle(settingsPreferences.getString("offerCategoryTitle " +i,""));
                                categoriesRefresh.add(oa);
                            }

                            if (mPager.getCurrentItem() == 0 || mPager.getCurrentItem() == 1 || mPager.getCurrentItem() == 2) {

                                Fragment fragment1 = mPagerAdapter.getRegisteredFragment(1);
                                CategoriesFragment categoriesFragment = (CategoriesFragment) fragment1;

                                lv_categories = categoriesFragment.getView().findViewById(R.id.lv_categories);
                                CheckBoxAdapter checkBoxAdapter = new CheckBoxAdapter(MainActivity.this,categoriesRefresh);
                                lv_categories.setAdapter(checkBoxAdapter);

                            }


                            volleySetDefaultAreas();





                        } catch (JSONException e) {

                            e.printStackTrace();
                            Intent intentError = new Intent(MainActivity.this,MainActivity.class);
                            startActivity(intentError);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse (VolleyError error){

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
                    Toast.makeText(MainActivity.this,Utils.getServerError(),Toast.LENGTH_LONG).show();
                    Intent intentError = new Intent(MainActivity.this,MainActivity.class);
                    startActivity(intentError);

            }
        }
        );
        Volley.newRequestQueue(MyApplication.getAppContext()).add(stringRequest);
    }

    public void volleySetDefaultAreas(){
        String url =Utils.getUrl()+"jobOfferAreas.php";
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ArrayList<OfferArea> areasRefresh = new ArrayList<>();



                        // Display the first 500 characters of the response string.
                        try {
                            JSONObject jsonObjectAll = new JSONObject(response);

                            JSONArray jsonArray = jsonObjectAll.getJSONArray("jobofferareas");
                            settingsPreferences.edit().putInt("numberOfAreas", jsonArray.length()).apply();
                            settingsPreferences.edit().putInt("numberOfCheckedAreas", jsonArray.length()).apply();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObjectCategory = jsonArray.getJSONObject(i);
                                settingsPreferences.edit().putInt("offerAreaId " + i, Integer.valueOf(jsonObjectCategory.getString("jloc_id"))).apply();
                                settingsPreferences.edit().putInt("checkedAreaId " + i, Integer.valueOf(jsonObjectCategory.getString("jloc_id"))).apply();
                                settingsPreferences.edit().putString("offerAreaTitle " + i, jsonObjectCategory.getString("jloc_title")).apply();
                                settingsPreferences.edit().putString("checkedAreaTitle " + i, jsonObjectCategory.getString("jloc_title")).apply();

                            }


                            for (int i = 0; i < settingsPreferences.getInt("numberOfAreas", 0); i++) {
                                OfferArea area = new OfferArea();
                                area.setAreaid(settingsPreferences.getInt("offerAreaId " + i, 0));
                                area.setTitle(settingsPreferences.getString("offerAreaTitle " + i, ""));
                                areasRefresh.add(area);
                            }
                            if (mPager.getCurrentItem() == 1 || mPager.getCurrentItem() == 2 || mPager.getCurrentItem() == 3 ) {

                                Fragment fragment2 = mPagerAdapter.getRegisteredFragment(2);
                                AreasFragment areasFragment = (AreasFragment) fragment2;

                                lv_areas = areasFragment.getView().findViewById(R.id.lv_areas);
                                CheckBoxAreaAdapter checkBoxAreaAdapter = new CheckBoxAreaAdapter(MainActivity.this,areasRefresh);
                                lv_areas.setAdapter(checkBoxAreaAdapter);

                                checkBoxAreaAdapter.notifyDataSetChanged();

                            }

                            categoriesIds="";
                            areaIds="";
                            for (int v = 0; v < (settingsPreferences.getInt("numberOfCheckedCategories", 0)); v++) {
                                if (categoriesIds.equals("")) {
                                    categoriesIds += settingsPreferences.getInt("checkedCategoryId " + v, 0);
                                } else
                                    categoriesIds += "," + settingsPreferences.getInt("checkedCategoryId " + v, 0);
                            }
                            for (int v = 0; v < (settingsPreferences.getInt("numberOfCheckedAreas", 0)); v++) {
                                if (areaIds.equals("")) {
                                    areaIds += settingsPreferences.getInt("checkedAreaId " + v, 0);
                                } else
                                    areaIds += "," + settingsPreferences.getInt("checkedAreaId " + v, 0);
                            }
                            settingsPreferences.edit().putString("categoriesIds",categoriesIds).apply();
                            settingsPreferences.edit().putString("areasIds",areaIds).apply();






                            volleySetCheckedCategories(categoriesIds,areaIds);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse (VolleyError error){

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

                    Toast.makeText(MainActivity.this,Utils.getServerError(),Toast.LENGTH_LONG).show();
                    Intent intentError = new Intent(MainActivity.this,MainActivity.class);
                    startActivity(intentError);

            }
        }
        );
        Volley.newRequestQueue(MainActivity.this).add(stringRequest);
    }

    public void volleyImageNames() {

        final String url = Utils.getUrl()+"images.php";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ArrayList<Bitmap> myBitmaps = new ArrayList<>();




                        // Display the first 500 characters of the response string.

                        try {
                            JSONObject jsonObjectAll = new JSONObject(response);
                            JSONArray jsonArray = jsonObjectAll.getJSONArray("images");

                            String[] imageNames = new String[jsonArray.length()];
                            for(int i=0;i<jsonArray.length();i++) {

                                JSONObject jsonObjectCategory = jsonArray.getJSONObject(i);
                                imageNames[i] = jsonObjectCategory.getString("image_title");

                            }
                            if(jsonArray.length()>0) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(0);
                                settingsPreferences.edit().putLong("lastImageDate", (format.parse(jsonObject1.getString("image_date"))).getTime()).apply();
                                new DownloadTask().execute(imageNames);
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
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

                    Toast.makeText(MainActivity.this, Utils.getServerError(), Toast.LENGTH_LONG).show();

            }
        }
        );
        Volley.newRequestQueue(MainActivity.this).add(stringRequest);
    }

    private class DownloadTask extends AsyncTask<String,Void,ArrayList<Bitmap>> {

        // Before the tasks execution
        protected void onPreExecute(){
            // Display the progress dialog on async task start
        }

        // Do the task in background/non UI thread
        protected ArrayList<Bitmap> doInBackground(String...names){
            HttpURLConnection connection = null;
            ArrayList<Bitmap> bitmaps = new ArrayList<>();

            try{
                for(String name:names) {
                    // Initialize a new http url connection
                    String stringUrl = Utils.getUrl()+"images/"+name;
                    URL url = stringToURL(stringUrl);
                    connection = (HttpURLConnection) url.openConnection();

                    // Connect the http url connection
                    connection.connect();

                    // Get the input stream from http url connection
                    InputStream inputStream = connection.getInputStream();

                /*
                    BufferedInputStream
                        A BufferedInputStream adds functionality to another input stream-namely,
                        the ability to buffer the input and to support the mark and reset methods.
                */
                /*
                    BufferedInputStream(InputStream in)
                        Creates a BufferedInputStream and saves its argument,
                        the input stream in, for later use.
                */
                    // Initialize a new BufferedInputStream from InputStream
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                /*
                    decodeStream
                        Bitmap decodeStream (InputStream is)
                            Decode an input stream into a bitmap. If the input stream is null, or
                            cannot be used to decode a bitmap, the function returns null. The stream's
                            position will be where ever it was after the encoded data was read.

                        Parameters
                            is InputStream : The input stream that holds the raw data
                                              to be decoded into a bitmap.
                        Returns
                            Bitmap : The decoded bitmap, or null if the image data could not be decoded.
                */
                    // Convert BufferedInputStream to Bitmap object
                    bitmaps.add(BitmapFactory.decodeStream(bufferedInputStream));
                }

                // Return the downloaded bitmap
                return bitmaps;

            }catch(IOException e){
                e.printStackTrace();
                Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
            }finally{
                // Disconnect the http url connection
                connection.disconnect();
            }
            return null;
        }

        // When all async task done
        protected void onPostExecute(ArrayList<Bitmap> result){
            int counter =0;
            // Hide the progress dialog
            for(Bitmap bitmap:result) {
                counter++;
                Uri uri = saveImageToInternalStorage(bitmap,counter);
                settingsPreferences.edit().putString("imageUri"+counter,uri.toString()).apply();
            }
            settingsPreferences.edit().putInt("numberOfImages",counter).apply();

            if(mPager.getCurrentItem()==0 || mPager.getCurrentItem()==1){
                Fragment fragment = mPagerAdapter.getRegisteredFragment(0);
                OffersFragment offersFragment1 = (OffersFragment) fragment;
                imgBtn_ad = offersFragment1.getView().findViewById(R.id.imgBtn_ad);


                if (settingsPreferences.getInt("numberOfImages", 0) > 0) {
                    paths = new String[settingsPreferences.getInt("numberOfImages", 0)];

                    for (int i = 1; i <= paths.length; i++) {
                        paths[i - 1] = settingsPreferences.getString("imageUri" + i, "");
                    }
                    ArrayList<Bitmap> bitmaps = new ArrayList<>();
                    for (String path : paths) {


                        try {
                            File d = new File(path);
                            bitmaps.add(BitmapFactory.decodeStream(new FileInputStream(d)));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }

                    Random r = new Random();
                    int rnum = r.nextInt(paths.length);
                    imgBtn_ad.setVisibility(View.VISIBLE);
                    imgBtn_ad.setImageBitmap(bitmaps.get(rnum));
                }
            }

        }

    }

    // Custom method to convert string to url
    protected URL stringToURL(String urlString){
        try{
            URL url = new URL(urlString);
            return url;
        }catch(MalformedURLException e){
            e.printStackTrace();
        }
        return null;
    }

    // Custom method to save a bitmap into internal storage
    protected Uri saveImageToInternalStorage(Bitmap bitmap,int number){
        // Initialize ContextWrapper
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());

        // Initializing a new file
        // The bellow line return a directory in internal storage
        File file = wrapper.getDir("Images",MODE_PRIVATE);

        // Create a file to save the image
        file = new File(file, "image"+number+".jpg");

        try{
            // Initialize a new OutputStream
            OutputStream stream = null;

            // If the output file exists, it can be replaced or appended to it
            stream = new FileOutputStream(file);

            // Compress the bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);

            // Flushes the stream
            stream.flush();

            // Closes the stream
            stream.close();

        }catch (IOException e) // Catch the exception
        {
            e.printStackTrace();
        }

        // Parse the gallery image url to uri
        Uri savedImageURI = Uri.parse(file.getAbsolutePath());

        // Return the saved image Uri
        return savedImageURI;
    }

}

