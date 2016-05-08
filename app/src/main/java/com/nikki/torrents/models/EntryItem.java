package com.nikki.torrents.models;

import android.graphics.drawable.Drawable;

/**
 * Created by Nishi Sahlot on 3/20/2016.
 */
public class EntryItem {
    final public String title;
    final public String subtitle;
    public Drawable icon1;
    public EntryItem(String title, String path,Drawable icon1) {
        this.title = title;
        this.subtitle = path;
        this.icon1=icon1;
    }
}
