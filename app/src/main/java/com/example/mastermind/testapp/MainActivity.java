package com.example.mastermind.testapp;


import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
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

public class MainActivity extends FragmentActivity {
    SharedPreferences settingsPreferences;
    ArrayList<JobOffer> offers;
    ArrayList<JobOffer> asyncOffers;

    ImageButton imgBtn_ad;

    ListView lv;
    RecyclerView categoriesRecyclerView;
    RecyclerView areasRecyclerView;
    DateFormat format;
    String message = "";
    RequestQueue queue;

    String areaIds;
    String categoriesIds;
    String[] paths;


    private static final int NUM_PAGES = 4;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        getSupportActionBar().setTitle("BeeMyJob");
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("BeeMyJob");
        toolbar.inflateMenu(R.menu.menu_main);
        ActionMenuItemView menuItem = findViewById(R.id.menu_refresh);
        menuItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new OffersFragment().RefreshOperation();
                new CategoriesFragment().RefreshOperation();
                new AreasFragment().RefreshOperation();
            }
        });


        lv = findViewById(R.id.listView);
        categoriesRecyclerView = findViewById(R.id.lv_categories);
        areasRecyclerView = findViewById(R.id.lv_areas);
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


        if(isConn()) {
            categoriesIds = "";
            areaIds = "";

            if (queue == null) {
                queue = Volley.newRequestQueue(this);
            }

            for (int v = 0; v < (settingsPreferences.getInt("numberOfCheckedCategories", 0)); v++) {
                if (v < settingsPreferences.getInt("numberOfCheckedCategories", 0) - 1) {
                    categoriesIds += settingsPreferences.getInt("checkedCategoryId " + v, 0) + ",";
                } else
                    categoriesIds += settingsPreferences.getInt("checkedCategoryId " + v, 0);
            }
            for (int v = 0; v < (settingsPreferences.getInt("numberOfCheckedAreas", 0)); v++) {
                if (v < settingsPreferences.getInt("numberOfCheckedAreas", 0) - 1) {
                    areaIds += settingsPreferences.getInt("checkedAreaId " + v, 0) + ",";
                } else
                    areaIds += settingsPreferences.getInt("checkedAreaId " + v, 0);
            }




            queue.add(volleySetCheckedCategories(categoriesIds, areaIds));

            if (settingsPreferences.getInt("numberOfImages", 0) > 0) {
                paths = new String[settingsPreferences.getInt("numberOfImages", 0)];

                for (int i = 1; i <= paths.length; i++) {
                    paths[i - 1] = settingsPreferences.getString("imageUri" + i, "");
                }
                loadImageFromStorage(paths);
            }

        }else{
            Toast.makeText(MainActivity.this,"Πρέπει να είστε συνδεδεμένος στο ίντερνετ για να κάνετε ανανέωση!",Toast.LENGTH_LONG).show();
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this,SettingActivity.class);
            startActivity(intent);
            return true;
        }else{
            RefreshOperation();

        }

        return super.onOptionsItemSelected(item);
    }

    public StringRequest volleySetCheckedCategories(final String param,final String param2) {
        String url = Utils.getUrl()+"jobAdsArray.php?";


        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ArrayList<JobOffer> offersRefresh = new ArrayList<>();
                        asyncOffers.clear();
                        JobOfferAdapter jobOfferAdapter= new JobOfferAdapter(getApplicationContext(), offersRefresh);
                        ListView lv = new OffersFragment().lv;

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
                        if(asyncOffers.size()>0) {
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



                        System.out.println(offers.toString());
//
//                        lv.setAdapter(jobOfferAdapter);
//                        System.out.println(settingsPreferences.getLong("interval",0));
//
//
//                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                        @Override
//                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                            Intent intentToDetail = new Intent(MainActivity.this,DetailActivity.class);
//                            intentToDetail.putExtra("jobOffer", (Serializable) adapterView.getItemAtPosition(i));
//                            startActivity(intentToDetail);
//
//                            }
//                        });



                        queue.add(volleyUpdateDefault());

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
                if(!message.equals("")){
                    Toast.makeText(MainActivity.this,Utils.getServerError(),Toast.LENGTH_LONG).show();
                    Intent intentError = new Intent(MainActivity.this,MainActivity.class);
                    startActivity(intentError);
                }
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("jacat_id",param);
                params.put("jloc_id",param2);

                return params;
            }
        };
        return stringRequest;
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



    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new OffersFragment();
                case 1:
                    return new CategoriesFragment();
                case 2:
                    return new AreasFragment();
                case 3:
                    return new TimesFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
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
                        Fragment fragment = new CategoriesFragment();
                        RecyclerView categoriesRecyclerView = fragment.getView().findViewById(R.id.lv_categories);



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
                                LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(MainActivity.this);
                                categoriesRecyclerView.setLayoutManager(recyclerLayoutManager);

                                //RecyclerView item decorator
                                DividerItemDecoration dividerItemDecoration =
                                        new DividerItemDecoration(categoriesRecyclerView.getContext(),
                                                recyclerLayoutManager.getOrientation());
                                categoriesRecyclerView.addItemDecoration(dividerItemDecoration);

                                //RecyclerView adapater
                                CheckRecycleAdapter recyclerViewAdapter = new
                                        CheckRecycleAdapter(categoriesRefresh,MainActivity.this);
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
                    Toast.makeText(MainActivity.this, Utils.getServerError(), Toast.LENGTH_LONG).show();
                    Intent intentError = new Intent(MainActivity.this, SettingActivity.class);
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
                        Fragment fragment = new AreasFragment();
                        RecyclerView areasRecyclerView = fragment.getView().findViewById(R.id.lv_areas);


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
                                LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(MainActivity.this);
                                areasRecyclerView.setLayoutManager(recyclerLayoutManager);

                                //RecyclerView item decorator
                                DividerItemDecoration dividerItemDecoration =
                                        new DividerItemDecoration(areasRecyclerView.getContext(),
                                                recyclerLayoutManager.getOrientation());
                                areasRecyclerView.addItemDecoration(dividerItemDecoration);

                                //RecyclerView adapater
                                CheckAreaRecycleAdapter recyclerViewAdapter = new
                                        CheckAreaRecycleAdapter(areasRefresh,MainActivity.this);
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
                    Toast.makeText(MainActivity.this, Utils.getServerError(), Toast.LENGTH_LONG).show();
                    Intent intentError = new Intent(MainActivity.this, SettingActivity.class);
                    startActivity(intentError);
                }
            }
        }
        );
        return stringRequest;
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
        imgBtn_ad = findViewById(R.id.imgBtn_ad);
        imgBtn_ad.setVisibility(View.VISIBLE);
        imgBtn_ad.setImageBitmap(bitmaps.get(rnum));

    }


}

