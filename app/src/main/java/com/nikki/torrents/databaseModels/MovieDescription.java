package com.nikki.torrents.databaseModels;

import android.text.TextUtils;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nikki.torrents.enums.MovieTv;
import com.nikki.torrents.models.Review;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Nishi Sahlot on 3/15/2016.
 */
@Table(name = "MovieDescription")
public class MovieDescription extends Model{
    @Column(name = "Overview")
    public String Overview;
    @Column(name = "Tagline")
    public String Tagline;
    @Column(name = "Videos")
    public String videosSerialized;
    @Column(name = "Review")
    public String reviewData;
    @Column(name = "PosterPath")
    public String posterPath;
    @Column(name = "MovieModel",onDelete = Column.ForeignKeyAction.CASCADE,
            onUpdate = Column.ForeignKeyAction.CASCADE)
    public MovieModel movieModel;
    public Review review;


    public List<String > videos=new ArrayList<>();


    public void serialize(){
        if(videos.size()>0){
            videosSerialized=new Gson().toJson(videos);
        }
        if(review!=null)
            reviewData=new Gson().toJson(review);
    }
    public void deserialize(){
        if(!TextUtils.isEmpty(videosSerialized)){
            videos=new Gson().fromJson(videosSerialized, new TypeToken<List<String>>() {
            }.getType());
        }
        if(!TextUtils.isEmpty(reviewData)){
            review=new Gson().fromJson(reviewData,Review.class);
        }
    }
    public static MovieDescription getDescriptionFromParent(MovieModel movieModel){
        if(movieModel.getId()==null){
            movieModel=MovieModel.getMovieModel("MovieId=?",movieModel.MovieId);
        }
        if(movieModel!=null){
            MovieDescription description= new Select().from(MovieDescription.class).where("MovieModel=?",
                    movieModel.getId()).executeSingle();
            if(description!=null)
                description.deserialize();
            return description;
        }
        return null;

    }





    public static MovieDescription getMovieDescription(String htmlText,MovieTv movieTv){
        try {
            Document document= Jsoup.parse(htmlText);
            MovieDescription movieDescription=new MovieDescription();
            if(movieTv==MovieTv.MOVIE){
                Pattern pattern=Pattern.compile("h3.*Reviews.*([\\n\\r\\t]|[\\n\\r\\t].*)*.*<\\/.*p>");
                Matcher matcher=pattern.matcher(htmlText);
                if(matcher.find()){
                    try {
                        Document parse=Jsoup.parse(matcher.group());
                        Elements elements=parse.select("a");
                        if(elements.size()>1){
                            Review review=new Review();
                            review.author=elements.get(2).text();
                            review.content=document.select("div.content").text();
                            movieDescription.review=review;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                movieDescription.Overview=document.select("p#overview").text();
                movieDescription.Tagline=document.select("p#tagline").text();
            }else{
                Pattern pattern=Pattern.compile("<.*Overview.*([\\n\\r\\t]|[\\n\\r\\t].*)*.*<\\/.*p>");
                Matcher matcher=pattern.matcher(htmlText);
                if(matcher.find()){
                    movieDescription.Overview=Jsoup.parse(matcher.group()).select("p").text();
                }
            }


            Elements elements=document.select("a.video");
            for (Element videoElement:elements){
                String videoLink=videoElement.attr("href");
                if(!TextUtils.isEmpty(videoLink))
                movieDescription.videos.add(videoLink);
            }

            try {
                elements=document.select("div.image_carousel").select("img");
                if(elements.size()>0){
                    Collections.shuffle(elements);
                    movieDescription.posterPath=elements.get(0).attr("src");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            return  movieDescription;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
