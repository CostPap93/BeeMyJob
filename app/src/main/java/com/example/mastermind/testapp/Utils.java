package com.example.mastermind.testapp;

/**
 * Created by mastermind on 30/5/2018.
 */

public final class Utils {

    private final static String url = "http://data.des.edu.gr/beemyjob/";

    private final static String serverError = "Υπάρχει κάποιο πρόβλημα στον Server!";
    private final static String connectionError = "Δεν είστε συνδεδεμένος στο ίντερνετ!";

    public static String getUrl(){
        return url;
    }

    public static String getServerError(){
        return serverError;
    }

    public static String getConnectionError(){
        return connectionError;
    }


}
