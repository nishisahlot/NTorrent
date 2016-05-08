package com.nikki.torrents.utils;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by Nishi Sahlot on 2/14/2016.
 */
public class CheckConnection {

    public static boolean checkConnection(Context context){
        ConnectivityManager cm = (ConnectivityManager)context. getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

}
