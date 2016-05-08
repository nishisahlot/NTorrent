package com.nikki.torrents.models;

/**
 * Created by Nishi Sahlot on 3/26/2016.
 */
public class DownloadLocationModel {
    public String basePath;
    public String downloadLocationPath;
    public DownloadLocationModel(){}
    public DownloadLocationModel(String basePath,String downloadLocationPath){
        this.basePath=basePath;
        this.downloadLocationPath=downloadLocationPath;
    }

}
