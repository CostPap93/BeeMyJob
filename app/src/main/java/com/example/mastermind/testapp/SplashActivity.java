package com.example.mastermind.testapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.health.SystemHealthManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.squareup.picasso.Picasso;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.PatternSyntaxException;

/**
 * Created by mastermind on 2/5/2018.
 */

public class SplashActivity extends AppCompatActivity {
    private int MY_PERMISSION = 1000;
    String message = "";
    RequestQueue queue;

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    ArrayList<JobOffer> asyncOffers = new ArrayList<>();
    String categoriesIds;
    String areasIds;
    SharedPreferences settingsPreferences;
    PendingIntent pendingIntentA;
    int t = 0;
    int areaid = 0;
    int counter;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        areasIds = "";
        categoriesIds = "";
        counter = 0;

        if(queue == null) {
            queue = Volley.newRequestQueue(this);
        }


        if(Build.VERSION.SDK_INT>=23){
            if(!Settings.canDrawOverlays(SplashActivity.this)){
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()));
                startActivityForResult(intent,MY_PERMISSION);
            }
        }
        else{
            Intent intent = new Intent(SplashActivity.this, Service.class);
            startService(intent);
        }


        settingsPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        settingsPreferences.edit().clear().apply();
        System.out.println(settingsPreferences.getInt("numberOfCategories", 0) == 0);
        System.out.println(settingsPreferences.getInt("numberOfCheckedCategories", 0) == 0);



        System.out.println(settingsPreferences.getBoolean("checkIsChanged", false));

        start();

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                if (settingsPreferences.getInt("numberOfCategories", 0) == 0 && settingsPreferences.getInt("numberOfAreas", 0) == 0 && isConn()) {
                    settingsPreferences.edit().putLong("interval", 6000).apply();
                    settingsPreferences.edit().putBoolean("makeRequest",false).apply();
                    System.out.println(settingsPreferences.getLong("interval",0));
                    start();

                    volleySetDefault();

                } else if (settingsPreferences.getInt("numberOfCategories", 0) == 0 && settingsPreferences.getInt("numberOfAreas", 0) == 0 && !isConn()) {

                    Toast.makeText(SplashActivity.this, "Πρέπει να είστε συνδεδεμένος την πρώτη φορά!", Toast.LENGTH_LONG).show();

                    settingsPreferences.edit().putLong("interval", 6000).apply();
                    settingsPreferences.edit().putBoolean("makeRequest",false).apply();
                    System.out.println(settingsPreferences.getLong("interval",0));
                    start();

                    Intent intent = new Intent(SplashActivity.this,MainActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(SplashActivity.this,MainActivity.class);
                    startActivity(intent);
                }
            }
        },2000 );




    }


    public boolean isConn(){
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

        Intent alarmIntent = new Intent(SplashActivity.this, AlarmReceiver.class);
        pendingIntentA = PendingIntent.getBroadcast(SplashActivity.this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),settingsPreferences.getLong("interval",0), pendingIntentA);

    }

    public void volleySetDefault(){
        String url =Utils.getUrl()+"jobOfferCategories.php";

    // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {




                        // Display the first 500 characters of the response string.
                        try {
                            categoriesIds = "";
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

                                if(categoriesIds.equals("")) {
                                    categoriesIds += jsonObjectCategory.getString("jacat_id");
                                }else
                                    categoriesIds += "," + jsonObjectCategory.getString("jacat_id");


                                System.out.println(categoriesIds);
                                System.out.println(jsonObjectCategory.toString());
                                System.out.println(settingsPreferences.getInt("checkedCategoryId " + i, 0) + "In The Task set Default");
                                System.out.println(settingsPreferences.getString("checkedCategoryTitle " + i, ""));
                            }

                            settingsPreferences.edit().putString("categoriesIds",categoriesIds).apply();
                            System.out.println(settingsPreferences.getInt("numberOfCheckedCategories", 0));
                            volleySetDefaultAreas();



                        } catch (JSONException e) {

                            e.printStackTrace();
                            Intent intentError = new Intent(SplashActivity.this,MainActivity.class);
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
                System.out.println("Volley: "+ message);
                if(!message.equals("")){
                    Toast.makeText(SplashActivity.this,Utils.getServerError(),Toast.LENGTH_LONG).show();
                    Intent intentError = new Intent(SplashActivity.this,MainActivity.class);
                    startActivity(intentError);
                }
            }
        }
        );
        Volley.newRequestQueue(MyApplication.getAppContext()).add(stringRequest);
    }

    public void volleySetCheckedCategories(final String param,final String param2) {
        final String url = Utils.getUrl()+"jobAdsArray.php?";



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
                                areaid = Integer.valueOf(jsonObjectCategory.getString("jloc_id"));
                                offer.setTitle(jsonObjectCategory.getString("jad_title"));
                                offer.setCattitle(jsonObjectCategory.getString("jacat_title"));
                                offer.setAreatitle(jsonObjectCategory.getString("jloc_title"));

                                offer.setDesc(jsonObjectCategory.getString("jad_desc"));
                                offer.setDate(format.parse(jsonObjectCategory.getString("jad_date")));
                                offer.setDownloaded(jsonObjectCategory.getString("jad_downloaded"));
                                offer.setLink(jsonObjectCategory.getString("jad_link"));
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


                        System.out.println(t);
                        System.out.println(settingsPreferences.getInt("numberOfCheckedCategories", 0));

                        volleyImageNames();

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
                    Toast.makeText(SplashActivity.this,Utils.getServerError(),Toast.LENGTH_LONG).show();
                    Intent intentError = new Intent(SplashActivity.this,MainActivity.class);
                    startActivity(intentError);
                }
            }
        })
        {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("jacat_id",param);
                params.put("jloc_id",param2);

                return params;
            }
        };
        Volley.newRequestQueue(SplashActivity.this).add(stringRequest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        queue.stop();
    }

    public void volleySetDefaultAreas(){
        String url =Utils.getUrl()+"jobOfferAreas.php";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {




                        // Display the first 500 characters of the response string.
                        try {
                            areasIds = "";
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

                                if(areasIds.equals("")) {
                                    areasIds += jsonObjectCategory.getString("jloc_id");
                                }else
                                    areasIds += ","+ jsonObjectCategory.getString("jloc_id");
                                System.out.println(areasIds.toString());

                                System.out.println(jsonObjectCategory.toString());
                                System.out.println(settingsPreferences.getInt("checkedAreaId " + i, 0) + "In The Task set Default");
                                System.out.println(settingsPreferences.getString("checkedAreaTitle " + i, ""));
                            }
                            settingsPreferences.edit().putString("areasIds",areasIds).apply();
                            System.out.println(settingsPreferences.getInt("numberOfCheckedAreas", 0));


                            System.out.println(settingsPreferences.getString("categoriesIds",""));
                            System.out.println(settingsPreferences.getString("areasIds",""));

                            String url = Utils.getUrl()+"jobAdsArray.php?jacat_id="+categoriesIds+"&jloc_id="+areasIds;
                            System.out.println(url);

                            volleySetCheckedCategories(categoriesIds,areasIds);




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
                System.out.println("Volley: "+ message);
                if(!message.equals("")){
                    Toast.makeText(SplashActivity.this,Utils.getServerError(),Toast.LENGTH_LONG).show();
                    Intent intentError = new Intent(SplashActivity.this,MainActivity.class);
                    startActivity(intentError);
                }
            }
        }
        );
        Volley.newRequestQueue(SplashActivity.this).add(stringRequest);
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
                        System.out.println("Volley: " + message);
                        System.out.println(response);

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
                System.out.println("Volley: " + message);
                if (!message.equals("")) {
                    Toast.makeText(SplashActivity.this, Utils.getServerError(), Toast.LENGTH_LONG).show();
                    Intent intentError = new Intent(SplashActivity.this, SettingActivity.class);
                    startActivity(intentError);
                }
            }
        }
        );
        Volley.newRequestQueue(SplashActivity.this).add(stringRequest);
    }

    private class DownloadTask extends AsyncTask<String,Void,ArrayList<Bitmap>>{

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
                Toast.makeText(SplashActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
            }finally{
                // Disconnect the http url connection
                connection.disconnect();
            }
            return null;
        }

        // When all async task done
        protected void onPostExecute(ArrayList<Bitmap> result){
            counter =0;
            // Hide the progress dialog
            for(Bitmap bitmap:result) {
                counter++;
                Uri uri = saveImageToInternalStorage(bitmap,counter);
                System.out.println(uri.toString());
                settingsPreferences.edit().putString("imageUri"+counter,uri.toString()).apply();
            }
            settingsPreferences.edit().putInt("numberOfImages",counter).apply();

            Intent intent = new Intent(SplashActivity.this,MainActivity.class);
            startActivity(intent);


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
