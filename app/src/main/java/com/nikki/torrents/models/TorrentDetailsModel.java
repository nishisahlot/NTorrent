package com.nikki.torrents.models;

import android.content.Context;

import com.activeandroid.Cache;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.nikki.torrents.R;
import com.nikki.torrents.databaseModels.TorrentModel;
import com.nikki.torrents.utils.Constant;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Nishi Sahlot on 3/12/2016.
 */
public class TorrentDetailsModel {
    public String name;
    public String storagePath;
    public String totalSize;
    public String numberOfFiles;
    public String downloadLimit;
    public String uploadLimit;
    public String dateAdded;
    public String hash;
    public String torrentCreatedDate;



    public static TorrentDetailsModel getTorrentDetailsModel(TorrentModel torrentModel){
        File torrentFile=new File(torrentModel.FilePath);
        TorrentInfo torrentInfo=null;
        if(torrentFile.exists())
            torrentInfo=new TorrentInfo(torrentFile);

        if(torrentInfo!=null){
            Context context= Cache.getContext();
            TorrentDetailsModel torrentDetailsModel=new TorrentDetailsModel();
            torrentDetailsModel.name=torrentInfo.getName();
            torrentDetailsModel.storagePath=torrentFile.getParent();
            torrentDetailsModel.totalSize= Constant.byteCountToDisplaySize(torrentInfo.getTotalSize());
            torrentDetailsModel.numberOfFiles=""+torrentInfo.getNumFiles();

            if(torrentModel.DownloadLimit!=null){
                torrentDetailsModel.downloadLimit = Constant.byteCountToDisplaySize(torrentModel.DownloadLimit)+"/s";
            }else{
                torrentDetailsModel.downloadLimit =context.getString(R.string.unlimited);
            }
            if(torrentModel.UploadLimit!=null){
                torrentDetailsModel.uploadLimit = Constant.byteCountToDisplaySize(torrentModel.UploadLimit)+"/s";
            }else{
                torrentDetailsModel.uploadLimit =context.getString(R.string.unlimited);
            }
            SimpleDateFormat simpleDateFormat= new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.US);
            if(torrentModel.CreatedDate!=null){
                Calendar calendar=Calendar.getInstance();
                calendar.setTimeInMillis(torrentModel.CreatedDate);
                torrentDetailsModel.dateAdded=simpleDateFormat.format(calendar.getTime());
            }
            torrentDetailsModel.hash=torrentInfo.getInfoHash().toHex();
            if(torrentModel.AddedTime!=0){
                Calendar calendar=Calendar.getInstance();
                calendar.setTimeInMillis(torrentModel.AddedTime);
                torrentDetailsModel.torrentCreatedDate=""+simpleDateFormat.format(calendar.getTime());
            }

            return torrentDetailsModel;

        }
        return null;
    }




}
