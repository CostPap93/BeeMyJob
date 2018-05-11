package com.example.mastermind.testapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nex3z.notificationbadge.NotificationBadge;
import com.txusballesteros.bubbles.BubbleLayout;
import com.txusballesteros.bubbles.BubblesManager;
import com.txusballesteros.bubbles.OnInitializedCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;


public class AlarmReceiver extends BroadcastReceiver {


    Date offerDB;
    NotificationCompat.Builder notification;
    private static final int uniqueID = 45612;
    NotificationManager nm;
    Date currentTime = new Date();
    Date lastUpdate;
    static BubbleLayout bubbleView;

    ArrayList<JobOffer> asyncOffers = new ArrayList<>();
    SharedPreferences settingsPreferences;
BubblesManager bubblesManager;

    NotificationBadge mBadge;
    ArrayList<Integer> idArray = new ArrayList<>();
    String message = "";
    RequestQueue queue;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Boolean bubbleInit = false;


    @Override
    public void onReceive(final Context context, final Intent intent) {
        settingsPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());
        queue = Volley.newRequestQueue(MyApplication.getAppContext());

        settingsPreferences.edit().putBoolean("makeRequest",true).apply();



    }

    public int checkForOffers() {
        int notCount = 0;
        for (int i = 0; i < settingsPreferences.getInt("numberOfOffers", 0); i++) {
        System.out.println(settingsPreferences.getInt("numberOfOffers", 0));
            System.out.println(settingsPreferences.getLong("offerDate " + i,0) > settingsPreferences.getLong("lastSeenDate", 0));
            if (settingsPreferences.getLong("offerDate " + i,0) > settingsPreferences.getLong("lastSeenDate", 0)) {
                notCount++;

            }

            System.out.println(settingsPreferences.getLong("lastSeenDate", 0) + " at the end of alarmreceiver ");

        }
        return notCount;

    }

    public boolean isConn(){
        ConnectivityManager connectivityManager = (ConnectivityManager) MyApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isWifiConn = networkInfo.isConnected();
        networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isMobileConn = networkInfo.isConnected();
        Log.d("connection", "Wifi connected: " + isWifiConn);
        Log.d("connection", "Mobile connected: " + isMobileConn);
        return isWifiConn || isMobileConn;
    }





    /*public class TaskShowOffersFromCategories extends AsyncTask<String,Integer,ArrayList<JobOffer>> {
        SharedPreferences settingsPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


        protected void onPreExecute() {

            super.onPreExecute();

        }

        @Override
        protected void onPostExecute(ArrayList<JobOffer> fiveOffers) {

            if(fiveOffers!= null) {


                for (int j = 0; j < 5; j++) {
                    settingsPreferences.edit().remove("offerId " + j).apply();
                    settingsPreferences.edit().remove("offerCatid " + j).apply();
                    settingsPreferences.edit().remove("offerTitle " + j).apply();
                    settingsPreferences.edit().remove("offerDate " + j).apply();
                    settingsPreferences.edit().remove("offerDownloaded " + j).apply();
                }
                for (int i = 0; i < fiveOffers.size(); i++) {
                    if (i < 5) {

                        settingsPreferences.edit().putInt("offerId " + i, fiveOffers.get(i).getId()).apply();
                        settingsPreferences.edit().putInt("offerCatid " + i, fiveOffers.get(i).getCatid()).apply();
                        settingsPreferences.edit().putString("offerTitle " + i, fiveOffers.get(i).getTitle()).apply();
                        settingsPreferences.edit().putLong("offerDate " + i, fiveOffers.get(i).getDate().getTime()).apply();
                        settingsPreferences.edit().putString("offerDownloaded " + i, fiveOffers.get(i).getDownloaded()).apply();
                        System.out.println(settingsPreferences.getLong("offerDate " + i, 0));
                        System.out.println(settingsPreferences.getString("offerTitle " + i, ""));
                        settingsPreferences.edit().putInt("numberOfOffers", fiveOffers.size()).apply();
                    } else
                        settingsPreferences.edit().putInt("numberOfOffers", 5).apply();
                }
                System.out.println(settingsPreferences.getLong("lastSeenDate", 0));


                notCount = checkForOffers();

                if (notCount > 0) {
                    initBubble();
                    settingsPreferences.edit().putInt("numberOfUnseenOffers", notCount).apply();
                    settingsPreferences.edit().putLong("lastSeenDate", settingsPreferences.getLong("offerDate " + 0, 0)).apply();

                    Intent intentBackToMain = new Intent(MyApplication.getAppContext(), MainActivity.class);
                    intentBackToMain.putExtra("source", "alarm");
                    intentBackToMain.putExtra("notificationCount", notCount);
                    PendingIntent pendingIntent = PendingIntent.getActivity(MyApplication.getAppContext(), 0, intentBackToMain, PendingIntent.FLAG_UPDATE_CURRENT);
                    notification.setContentTitle("You have " + notCount + " unseen offers!!");
                    notification.setNumber(notCount);
                    notification.setContentIntent(pendingIntent);

                    NotificationManager nm = (NotificationManager) MyApplication.getAppContext().getSystemService(NOTIFICATION_SERVICE);
                    nm.notify(uniqueID, notification.build());
                    settingsPreferences.edit().putBoolean("makeRequest", false).apply();

                }
            } else{
                Toast.makeText(MyApplication.getAppContext(),"There is some problem with the server",Toast.LENGTH_LONG);
            }



        }

        @Override
        protected ArrayList<JobOffer> doInBackground(String... params) {

            Map<String, String> postParam = new HashMap<>();
            postParam.put("action", "showOffersFromCategory");
            postParam.put("jacat_id",params[0]);

            try {
                String jsonString = m_AccessServiceAPI.getJSONStringWithParam_POST("http://10.0.2.2/android/jobAds.php?", postParam);
                if(jsonString==""){
                    return null;
                }
                JSONObject jsonObjectAll = new JSONObject(jsonString);
                JSONArray jsonArray = jsonObjectAll.getJSONArray("offers");
                int i = 0;

                while(i<jsonArray.length() && i<5) {

                    JSONObject jsonObjectCategory = jsonArray.getJSONObject(i);

                    JobOffer offer = new JobOffer();
                    offer.setId(Integer.valueOf(jsonObjectCategory.getString("jad_id")));
                    offer.setCatid(Integer.valueOf(jsonObjectCategory.getString("jad_catid")));
                    offer.setTitle(jsonObjectCategory.getString("jad_title"));
                    offer.setDate(format.parse(jsonObjectCategory.getString("jad_date")));
                    offer.setDownloaded(jsonObjectCategory.getString("jad_downloaded"));
                    System.out.println(offer.getTitle() + " first time");

                    asyncOffers.add(offer);


                    Collections.sort(asyncOffers, new Comparator<JobOffer>() {
                        @Override
                        public int compare(JobOffer jobOffer, JobOffer t1) {
                            if(jobOffer.getDate().getTime()-t1.getDate().getTime()<0)
                                return 1;
                            else if(jobOffer.getDate().getTime()-t1.getDate().getTime()==0)
                                return 0;
                            else
                                return -1;
                        }
                    });
                    for(int x=0;x<asyncOffers.size();x++) {
                        System.out.println(asyncOffers.get(x).getTitle());
                    }

                    i++;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return asyncOffers;
        }

        private void initBubble(){
            bubblesManager = new BubblesManager.Builder(MyApplication.getAppContext())
                    .setTrashLayout(R.layout.bubble_remove)
                    .setInitializationCallback(new OnInitializedCallback() {
                        @Override
                        public void onInitialized() {
                            addNewBubble();
                        }
                    }).build();
            bubblesManager.initialize();
        }

        private void addNewBubble(){
            final BubbleLayout bubbleView = (BubbleLayout) LayoutInflater.from(MyApplication.getAppContext())
                    .inflate(R.layout.bubble_layout,null);
            mBadge = (NotificationBadge) bubbleView.findViewById(R.id.badge);
            mBadge.setNumber(notCount);

            bubbleView.setOnBubbleRemoveListener(new BubbleLayout.OnBubbleRemoveListener() {
                @Override
                public void onBubbleRemoved(BubbleLayout bubble) {
                    Toast.makeText(MyApplication.getAppContext(), "Removed", Toast.LENGTH_SHORT).show();
                }
            });

            bubbleView.setOnBubbleClickListener(new BubbleLayout.OnBubbleClickListener() {
                @Override
                public void onBubbleClick(BubbleLayout bubble) {
                    Toast.makeText(MyApplication.getAppContext(),"Clicked",Toast.LENGTH_SHORT).show();
                    Intent intentBubbleToMain = new Intent(MyApplication.getAppContext(),MainActivity.class);
                    intentBubbleToMain.putExtra("source","alarm");
                    MyApplication.getAppContext().startActivity(intentBubbleToMain);
                    bubblesManager.removeBubble(bubbleView);
                    bubblesManager.recycle();
                }
            });


            bubblesManager.addBubble(bubbleView,60,60);


        }

    }*/

    public StringRequest volleySetCheckedCategories(final String param) {
        String url = "http://10.0.2.2/android/jobAds.php?";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // Display the first 500 characters of the response string.
                        message = "Response is: " + response;
                        System.out.println("Volley: " + message);
                        try {
                            JSONObject jsonObjectAll = new JSONObject(response);
                            JSONArray jsonArray = jsonObjectAll.getJSONArray("offers");
                            int i = 0;

                            while (i < jsonArray.length() && i < 5) {


                                JSONObject jsonObjectCategory = jsonArray.getJSONObject(i);

                                if(!idArray.contains(Integer.valueOf(jsonObjectCategory.getString("jad_id")))) {
                                    idArray.add(Integer.valueOf(jsonObjectCategory.getString("jad_id")));


                                    JobOffer offer = new JobOffer();
                                    offer.setId(Integer.valueOf(jsonObjectCategory.getString("jad_id")));
                                    offer.setCatid(Integer.valueOf(jsonObjectCategory.getString("jad_catid")));
                                    offer.setTitle(jsonObjectCategory.getString("jad_title"));
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
                                settingsPreferences.edit().remove("offerCatid " + j).apply();
                                settingsPreferences.edit().remove("offerTitle " + j).apply();
                                settingsPreferences.edit().remove("offerDate " + j).apply();
                                settingsPreferences.edit().remove("offerDownloaded " + j).apply();
                            }
                            for (int i = 0; i < asyncOffers.size(); i++) {
                                if (i < 5) {

                                    settingsPreferences.edit().putInt("offerId " + i, asyncOffers.get(i).getId()).apply();
                                    settingsPreferences.edit().putInt("offerCatid " + i, asyncOffers.get(i).getCatid()).apply();
                                    settingsPreferences.edit().putString("offerTitle " + i, asyncOffers.get(i).getTitle()).apply();
                                    settingsPreferences.edit().putLong("offerDate " + i, asyncOffers.get(i).getDate().getTime()).apply();
                                    settingsPreferences.edit().putString("offerDownloaded " + i, asyncOffers.get(i).getDownloaded()).apply();
                                    System.out.println(settingsPreferences.getLong("offerDate " + i, 0));
                                    System.out.println(settingsPreferences.getString("offerTitle " + i, ""));
                                    settingsPreferences.edit().putInt("numberOfOffers", asyncOffers.size()).apply();
                                } else
                                    settingsPreferences.edit().putInt("numberOfOffers", 5).apply();
                            }
                            System.out.println(settingsPreferences.getLong("lastSeenDate", 0));



                            if (checkForOffers() > 0 && asyncOffers.get(0).getDate().getTime() > settingsPreferences.getLong("lastNotDate",0)) {

                                addNewBubble();


                                settingsPreferences.edit().putInt("numberOfUnseenOffers", checkForOffers()).apply();
                                System.out.println(settingsPreferences.getInt("numberOfUnseenOffers",0));

                                notification = new NotificationCompat.Builder(MyApplication.getAppContext(), "notification");
                                notification.setAutoCancel(true);

                                notification.setSmallIcon(R.drawable.ic_not);
                                notification.setTicker("This is the ticker");
                                notification.setWhen(System.currentTimeMillis());
                                notification.setCategory(NotificationCompat.CATEGORY_REMINDER);
                                notification.setDefaults(Notification.DEFAULT_ALL);
                                notification.setPriority(Notification.PRIORITY_MAX);
                                notification.setVibrate(new long[0]);
                                notification.setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL);

                                Intent intentBackToMain = new Intent(MyApplication.getAppContext(), UnseenActivity.class);
                                intentBackToMain.putExtra("source", "alarm");
                                PendingIntent pendingIntent = PendingIntent.getActivity(MyApplication.getAppContext(), 0, intentBackToMain, PendingIntent.FLAG_UPDATE_CURRENT);
                                notification.setContentTitle("You have " + checkForOffers() + " unseen offers!!");
                                notification.setContentIntent(pendingIntent);

                                nm = (NotificationManager) MyApplication.getAppContext().getSystemService(NOTIFICATION_SERVICE);
                                nm.notify(uniqueID, notification.build());

                                settingsPreferences.edit().putBoolean("makeRequest", false).apply();
                                settingsPreferences.edit().putLong("lastNotDate", asyncOffers.get(0).getDate().getTime()).apply();


                            } else{
                            Toast.makeText(MyApplication.getAppContext(),"There is some problem with the server",Toast.LENGTH_LONG);
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
                if(!message.equals("")){
                    Toast.makeText(MyApplication.getAppContext(),"There is some problem with the server ("+message+")",Toast.LENGTH_LONG).show();
                }
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "showOffersFromCategory");
                params.put("jacat_id",param);

                return params;
            }
        };
        return stringRequest;
    }

    private void initBubble(){

        bubblesManager = new BubblesManager.Builder(MyApplication.getAppContext())
                .setTrashLayout(R.layout.bubble_remove)
                .setInitializationCallback(new OnInitializedCallback() {
                    @Override
                    public void onInitialized() {
                    }
                }).build();
        bubblesManager.initialize();
        bubbleInit = true;

    }

    private void addNewBubble(){

        AlarmReceiver.bubbleView = (BubbleLayout) LayoutInflater.from(MyApplication.getAppContext())
                .inflate(R.layout.bubble_layout,null);
        mBadge = bubbleView.findViewById(R.id.badge);
        mBadge.setNumber(checkForOffers());

        bubbleView.setOnBubbleRemoveListener(new BubbleLayout.OnBubbleRemoveListener() {
            @Override
            public void onBubbleRemoved(BubbleLayout bubble) {
                Toast.makeText(MyApplication.getAppContext(), "Removed", Toast.LENGTH_SHORT).show();
            }
        });

        bubbleView.setOnBubbleClickListener(new BubbleLayout.OnBubbleClickListener() {
            @Override
            public void onBubbleClick(BubbleLayout bubble) {
                Toast.makeText(MyApplication.getAppContext(),"Clicked",Toast.LENGTH_SHORT).show();
                Intent intentBubbleToMain = new Intent(MyApplication.getAppContext(),UnseenActivity.class);
                intentBubbleToMain.putExtra("source","alarm");
                MyApplication.getAppContext().startActivity(intentBubbleToMain);
                bubblesManager.removeBubble(bubble);
                bubblesManager.recycle();
                nm.cancel(uniqueID);

            }
        });



        bubblesManager.addBubble(bubbleView,60,60);

    }


}

