package com.nikki.torrents.urls;

/**
 * Created by Nishi Sahlot on 2/14/2016.
 */
public class Apis {

    public static String BASE_URL="http://torrentz.eu";
    public static String BASE_URL_TMDB="http://www.themoviedb.org";

    public static String movieQuery=BASE_URL+"/suggestions.php?";
    public static String emptySearch=BASE_URL+"/search?q=";
    public static String torrentSearchPage=BASE_URL+"/search?f=&p=";




    public static String movieListApi=BASE_URL_TMDB+"/discover/movie";
    public static String tvListApi=BASE_URL_TMDB+"/discover/tv";
    public static String searchMovies=BASE_URL_TMDB+"/search/remote/multi?query=";
    public static String movieSearch=BASE_URL_TMDB+"/search/movie";
    public static String tvSearch=BASE_URL_TMDB+"/search/tv";

}
