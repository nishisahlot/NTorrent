package com.nikki.torrents.utils;

import android.content.Context;
import android.os.Bundle;

import com.nikki.torrents.models.EntryItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nishi Sahlot on 3/20/2016.
 */
public class Futils {

    public static boolean canListFiles(File f) {
        try {
            if (f.canRead() && f.isDirectory())
                return true;
            else
                return false;
        } catch (Exception e) {
            return false;
        }
    }
    public static Bundle getPaths(String path, Context c,List<EntryItem> entryItems) {
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<String> paths = new ArrayList<String>();
        Bundle b = new Bundle();
        while (path.contains("/")) {

            paths.add(path);
            names.add(path.substring(1 + path.lastIndexOf("/"), path.length()));
            if(isStorage(entryItems,path))
                break;
            path = path.substring(0, path.lastIndexOf("/"));

        }
        names.remove("");
        paths.remove("/");
        names.add("root");
        paths.add("/");
        // Toast.makeText(c,paths.get(0)+"\n"+paths.get(1)+"\n"+paths.get(2),Toast.LENGTH_LONG).show();
        b.putStringArrayList("names", names);
        b.putStringArrayList("paths", paths);
        return b;
    }

    public static boolean  isStorage(List<EntryItem> entryItems,String path) {
        for (EntryItem s:entryItems)
            if (s.subtitle.equals(path)) return true;
        return false;
    }

}
