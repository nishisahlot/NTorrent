package com.nikki.torrents.utils;

import android.graphics.drawable.Drawable;

import com.activeandroid.Cache;
import com.nikki.torrents.R;

/**
 * Created by Nishi Sahlot on 3/20/2016.
 */
public class IconUtils{

        public static  Drawable getRootDrawable() {
            return Cache.getContext().getResources().getDrawable(R.mipmap.root);
        }
        public static Drawable getSdDrawable() {
            return Cache.getContext().getResources().getDrawable(R.mipmap.ic_sd_storage_white_56dp);
        }
}
