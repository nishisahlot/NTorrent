package com.nikki.torrents.databaseModels;

import android.support.v7.graphics.Palette;
import android.text.TextUtils;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nikki.torrents.enums.MovieTv;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nishi Sahlot on 3/13/2016.
 */
@Table(name = "Movies")
public class MovieModel extends Model implements Serializable{
    @Column(name = "MovieId",unique = true)
    public String MovieId;
    @Column(name = "MovieName")
    public String movieName;
    @Column(name = "MovieCategory")
    public String movieCategory;
    @Column(name = "MovieRating")
    public String movieRating;
    @Column(name = "MovieReleaseYear")
    public String movieReleaseYear;
    @Column(name = "MovieOrTv")
    public MovieTv movieTv;
    @Column(name = "PosterImages")
    public String posterImages;

    public transient Palette palette;
    public boolean notMajorData;
    public List<String> poster=new ArrayList<>();

    public static List<MovieModel> getAll(){
        List<MovieModel> movieModels= new Select().from(MovieModel.class).orderBy("Id DESC").execute();
        if(movieModels.size()>0){
            for(MovieModel movieModel:movieModels)movieModel.deserializeImages();
        }
        return movieModels;
    }
    public static MovieModel getMovieModel(String projection,String... projectionArgs){
        return new Select().from(MovieModel.class).where(projection,projectionArgs).executeSingle();
    }



    public void serializeImages(){
        if(poster.size()>0){
            posterImages=new Gson().toJson(poster);
        }
    }
    public void deserializeImages(){
        if(!TextUtils.isEmpty(posterImages)){
            poster=new Gson().fromJson(posterImages,new TypeToken<List<String>>(){}.getType());
        }
    }




}
