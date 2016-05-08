package com.nikki.torrents.models;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nishi Sahlot on 2/18/2016.
 */
public class EmptyTorrentz implements Serializable{

    public String softwareName;
    public String rating;
    public String date;
    public String size;
    public String seeders;
    public String leechers;
    public String link;



    public static List<EmptyTorrentz> parseTorrentz(String htmlText){
        Document document=Jsoup.parse(htmlText);
        Elements results=document.select("div.results").select("dl");

        List<EmptyTorrentz> emptyTorrentzs=new ArrayList<>();

        for(Element element:results){
            try {
                EmptyTorrentz emptyTorrentz=new EmptyTorrentz();

                Element linkWithName=element.select("dt").select("a").get(0);
                emptyTorrentz.softwareName=linkWithName.text();
                emptyTorrentz.link=linkWithName.attr("href");

                Elements elements=element.select("dd").select("span");

                emptyTorrentz.rating=elements.get(0).text();
                emptyTorrentz.date=elements.get(2).text();
                emptyTorrentz.size=elements.get(3).text();
                emptyTorrentz.seeders=elements.get(4).text();
                emptyTorrentz.leechers=elements.get(5).text();
                emptyTorrentzs.add(emptyTorrentz);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return  emptyTorrentzs;
    }

}
