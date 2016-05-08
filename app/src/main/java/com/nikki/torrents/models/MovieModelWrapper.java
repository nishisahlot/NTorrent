package com.nikki.torrents.models;

import android.text.TextUtils;

import com.nikki.torrents.databaseModels.MovieModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Nishi Sahlot on 4/3/2016.
 */
public class MovieModelWrapper implements Serializable{
    public List<MovieModel> movieModels;
    public List<YearModel> yearModels;
    public List<GenreModel> genreModels;
    public String selectedYearKey;
    public String selectedGenreKey;
    public int selecteYear;



    public static MovieModelWrapper parseMovieResults(String htmlText){

        Document document= Jsoup.parse(htmlText);
        Elements elements=document.select("div.results").select("div.item");
        if(elements.size()==0)
            elements=document.select("div.item");
        List<MovieModel> movieModels=new ArrayList<>();

        for(Element element:elements){
            try {
                MovieModel movieModel=new MovieModel();
                Elements elements1=element.select("a");

                String[] images=elements1.select("img").attr("srcset").split(",");
                Collections.addAll(movieModel.poster, images);

                movieModel.MovieId =elements1.select("a.result").attr("href");
                movieModel.movieName=elements1.get(1).text();
                movieModel.movieRating=element.select("span.vote_average").text();
                movieModel.movieCategory=element.select("span.genres").text();
                movieModel.movieReleaseYear=element.select("span.release_date").text();
                movieModels.add(movieModel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        MovieModelWrapper movieModelWrapper=new MovieModelWrapper();
        movieModelWrapper.movieModels=movieModels;

        elements=document.select("select#year");
        Elements options=elements.select("option");
        movieModelWrapper.selectedYearKey =elements.attr("name");
        String selecteYear=null;
        try {
            Element selecteYearElement=elements.select("option[selected]").get(0);
            selecteYear=selecteYearElement.attr("value");
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<YearModel> yearModels=new ArrayList<>();
        int index=0;

        for(Element element:options){
            try {
                YearModel yearModel=new YearModel();
                yearModel.yearValue=element.attr("value");
                yearModel.yearText=element.text();
                if(!TextUtils.isEmpty(selecteYear)&&yearModel.yearValue.equals(selecteYear)){
                    movieModelWrapper.selecteYear=index;
                }

                yearModels.add(yearModel);
                index++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        List<GenreModel> genreModels =new ArrayList<>();
        movieModelWrapper.yearModels=yearModels;

        elements=document.select("select#with_genres");
        movieModelWrapper.selectedGenreKey = elements.attr("name");
        options=elements.select("option");
        for(Element element:options){
            GenreModel genreModel=new GenreModel();
            genreModel.genreId=element.attr("value");
            genreModel.genreText=element.text();
            genreModels.add(genreModel);
        }
        movieModelWrapper.genreModels=genreModels;
        return movieModelWrapper;
    }
}
