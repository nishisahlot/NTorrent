package com.nikki.torrents.models;

import android.text.TextUtils;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import org.jsoup.Jsoup;

import java.io.Serializable;

/**
 * Created by Nishi Sahlot on 2/28/2016.
 */
public class TorrentzWebsite implements Serializable{

    public String magnetUrl;



    public static TorrentzWebsite parseWebsite(String url){

        try {
            String website=new OkHttpClient().newCall(new Request.Builder().url(url).build()).execute().body().string();

            TorrentzWebsite torrentzWebsite=new TorrentzWebsite();
            torrentzWebsite.magnetUrl=Jsoup.parse(website).select("a[href~=magnet:\\?xt=urn:btih:]").attr("href");
            if(!TextUtils.isEmpty(torrentzWebsite.magnetUrl))
            return torrentzWebsite;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }




}
