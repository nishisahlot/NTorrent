package com.nikki.torrents.models;

import android.text.TextUtils;

/**
 * Created by Nishi Sahlot on 4/3/2016.
 */
public class TmdbMovieModel {
    public String adult;
    public String backdrop_path;
    public  String[] genre_ids;
    public String id;
    public String original_language;
    public String original_title;
    public String overview;
    public String release_date;
    public String first_air_date;
    public String popularity;
    public String title;
    public String media_type;
    public String name;

    public String getReleaseDate(){
        if(!TextUtils.isEmpty(release_date)){
            return "("+release_date+")";
        }else if(!TextUtils.isEmpty(first_air_date)){
            return "("+first_air_date+")";
        }else return "";
    }
}
