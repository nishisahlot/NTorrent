package com.nikki.torrents.models;

/**
 * Created by Nishi Sahlot on 3/28/2016.
 */
public class LimitSettingsPreference extends PreferenceStorage{

    public int downloadLimit;
    public int uploadLimit;
    public float shareRatioLimit=1.0f;
    public int seedingTimeLimit=120;//  minutes

    public boolean enableShareRatio;
    public boolean stopSeedingAfter;

}
