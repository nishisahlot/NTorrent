package com.nikki.torrents.databaseModels;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nikki.torrents.enums.DownloadState;
import com.nikki.torrents.enums.TorrentHandleState;
import com.nikki.torrents.interfaces.RefreshInterface;
import com.nikki.torrents.models.FileMetadata;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * Created by Nishi Sahlot on 3/6/2016.
 */
@Table(name = "TorrentModel")
public class TorrentModel extends Model implements Serializable{

    @Column(name = "FilePath",unique = true,onUniqueConflict = Column.ConflictAction.REPLACE,notNull = true)
    public String FilePath;
    @Column(name = "FastResume")
    public String FastResume;
    @Column(name = "DownloadState")
    public DownloadState DownloadState;
    @Column(name = "TorrentName")
    public String TorrentName;
    @Column(name = "FileSize")
    public long FileSize;
    @Column(name = "FileDownloaded")
    public long FileDownloaded;
    @Column(name = "FileUploaded")
    public long FileUploaded;
    @Column(name = "TotalSeeds")
    public int TotalSeeds;
    @Column(name = "TotalPeers")
    public int TotalPeers;
    @Column(name = "AddedTime")
    public long AddedTime;
    @Column(name = "HandleState")
    public TorrentHandleState torrentHandleState;
    @Column(name = "CreatedDate")
    public Long CreatedDate;
    @Column(name = "DownloadLimit")
    public Integer DownloadLimit;
    @Column(name = "UploadLimit")
    public Integer UploadLimit;
    @Column(name = "FileMetaData")
    public String fileMetaData;

    public List<FileMetadata> fileMetadataList;

    public boolean indeterminateProgressBar;
    public float progress;
    public int connectedSeeders;
    public int connectedPeers;
    public String downloadingSpeed="0.0 KB/s";
    public String uploadingSpeed="0.0 KB/s";
    public transient RefreshInterface refreshInterface;

    public int getProgress(){
        return (int)(progress*100);
    }
    public void serializeFileMetaData(){
        if(fileMetadataList!=null){
            fileMetaData=new Gson().toJson(fileMetadataList);
        }
    }
    public void deserializeFileMetaData(){
        if(fileMetaData!=null){
            fileMetadataList=new Gson().fromJson(fileMetaData,new TypeToken<List<FileMetadata>>(){}.getType());
        }
    }


    public void initializeSpeeds(){
        downloadingSpeed="0.0 KB/s";
        uploadingSpeed="0.0 KB/s";
    }

    public static TorrentModel getMe(String projection,Object... args){
        return new Select().from(TorrentModel.class).where(projection,args).executeSingle();
    }

    public static List<TorrentModel> getAll(){
        List<TorrentModel> torrentModels=new Select().from(TorrentModel.class).orderBy("Id DESC").execute();

        removeRedundantData(torrentModels);
        return torrentModels;
    }

    public static List<TorrentModel> getCompletedDownloads(){
        List<TorrentModel> torrentModels=new Select().from(TorrentModel.class).where("DownloadState=?",
                com.nikki.torrents.enums.DownloadState.COMPLTE_DOWNLOADED).orderBy("Id DESC").execute();
        removeRedundantData(torrentModels);
        return torrentModels;
    }
    public static List<TorrentModel> getPartiallyDownloads(){
        List<TorrentModel> torrentModels=new Select().from(TorrentModel.class).where("DownloadState=?",
                com.nikki.torrents.enums.DownloadState.PARTIAL_DOWNLOADED).orderBy("Id DESC").execute();
        removeRedundantData(torrentModels);
        return torrentModels;
    }


    private static void removeRedundantData(List<TorrentModel> torrentModels){
        for(int i=torrentModels.size()-1;i>-1;i--){
            TorrentModel torrentModel=torrentModels.get(i);
            File file=new File(torrentModel.FilePath);
            if(!file.exists()){
                torrentModel.delete();
                torrentModels.remove(i);
            }
        }
    }


}
