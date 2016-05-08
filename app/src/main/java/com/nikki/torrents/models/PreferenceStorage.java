package com.nikki.torrents.models;

import android.preference.PreferenceManager;

import com.activeandroid.Cache;
import com.google.gson.Gson;

/**
 * Created by Nishi Sahlot on 3/13/2016.
 */
public class PreferenceStorage {

    public void saveMe(){
        PreferenceManager.getDefaultSharedPreferences(Cache.getContext()).
                edit().putString(getClass().getName(),new Gson().toJson(this)).apply();
    }
    public static <T>T getMe(Class<T> clazz){
        T t= new Gson().fromJson(PreferenceManager.getDefaultSharedPreferences(Cache.getContext()).
                getString(clazz.getName(), null), clazz);
        if(t==null){
            try {
                t=clazz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return t;
    }

}
