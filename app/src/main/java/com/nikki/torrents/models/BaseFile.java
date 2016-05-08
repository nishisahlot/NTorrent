package com.nikki.torrents.models;

/**
 * Created by Nishi Sahlot on 3/20/2016.
 */
public class BaseFile {

    public long date,size;
    public boolean isDirectory;
    public String permisson;
    public String name;
    public String path;
    public String link="";
    public BaseFile(String path) {
        this.name = path;
    }
    public BaseFile(){}
    public BaseFile(String path, String permisson, long date, long size, boolean isDirectory) {
        this.date = date;
        this.size = size;
        this.isDirectory = isDirectory;
        this.path = path;
        this.permisson = permisson;

    }
}
