package com.nikki.torrents.models;

import android.text.TextUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nishi Sahlot on 2/19/2016.
 */
public class Torrentz {

    public String torrentzSiteName;
    public String movieName;
    public String torrentzLink;
    public String time;


    public static List<Torrentz> parseTorrentz(String htmlText){
        Document document= Jsoup.parse(htmlText);
        Elements results=document.select("div.download").select("dl");

        List<Torrentz> torrentzs=new ArrayList<>();

        for(Element element:results){
            try {
                Torrentz torrentz=new Torrentz();

                Elements elements=element.select("span");
                Elements attr=element.select("a");

                if(elements.size()>1){
                    torrentz.torrentzSiteName=elements.get(0).text();
                    torrentz.movieName=elements.get(1).text();
                }else{
                    torrentz.movieName=attr.text();
                }
                torrentz.torrentzLink=attr.attr("href");
                torrentz.time=element.select("dd").text();

                if(!TextUtils.isEmpty(torrentz.torrentzLink)&&torrentz.torrentzLink.contains("ads.ad"))
                    continue;


                torrentzs.add(torrentz);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return  torrentzs;
    }


}
