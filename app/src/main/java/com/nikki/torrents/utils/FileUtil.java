package com.nikki.torrents.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.nikki.torrents.models.BaseFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nishi Sahlot on 3/20/2016.
 */
public class FileUtil {
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String[] getExtSdCardPathsForActivity(Context context) {
        List<String> paths = new ArrayList<String>();
        for (File file : context.getExternalFilesDirs("external")) {
            if (file != null) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.w("AmazeFileUtils", "Unexpected external file dir: " + file.getAbsolutePath());
                }
                else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    }
                    catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        if(paths.isEmpty())paths.add("/storage/sdcard1");
        return paths.toArray(new String[0]);
    }
    public static List<BaseFile> getFilesList(String path, boolean showHidden) {
        File f = new File(path);
        ArrayList<BaseFile> files = new ArrayList<>();
        try {
            if (f.exists() && f.isDirectory()) {
                for (File x : f.listFiles()) {
                    long size = 0;
                    if (!x.isDirectory()) size = x.length();
                    BaseFile baseFile=new BaseFile(x.getPath(), parseFilePermission(x), x.lastModified() , size, x.isDirectory());
                    baseFile.name=x.getName();
                    if (showHidden) {
                        if(x.isDirectory())
                            files.add(baseFile);
                    } else {
                        if (!x.isHidden()) {
                            if(x.isDirectory())
                                files.add(baseFile);
                        }
                    }
                }
            }
        } catch (Exception e) {
        }


        return files;
    }
    public static String parseFilePermission(File f) {
        String per = "";
        if (f.canRead()) {
            per = per + "r";
        }
        if (f.canWrite()) {
            per = per + "w";
        }
        if (f.canExecute()) {
            per = per + "x";
        }
        return per;
    }
}
