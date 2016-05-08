package com.nikki.torrents.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.activeandroid.Cache;
import com.frostwire.jlibtorrent.AlertListener;
import com.frostwire.jlibtorrent.Downloader;
import com.frostwire.jlibtorrent.Priority;
import com.frostwire.jlibtorrent.Session;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.AlertType;
import com.frostwire.jlibtorrent.alerts.DhtStatsAlert;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nikki.torrents.activities.MainActivity;
import com.nikki.torrents.downloadService.TorrentService;
import com.nikki.torrents.enums.TorrentHandleState;
import com.nikki.torrents.fragments.DirectoryExplorer;
import com.nikki.torrents.models.FileMetadata;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nishi Sahlot on 2/29/2016.
 */
public class Constant {

    public static String fileDirectory="TorrentzDownload";

    public static final long ONE_KB = 1024;
    public static final long ONE_MB = ONE_KB * ONE_KB;
    public static final long ONE_GB = ONE_KB * ONE_MB;


    public static String getFileDirectory(){

        return Environment.getExternalStorageDirectory()+ File.separator+fileDirectory+File.separator;

    }

    public static boolean createDirectory(String path) {
        File file = new File(path);
        return !file.isDirectory() && file.mkdirs();
    }

    public static boolean createFile(String fileName){
        try {
            File file=new File(fileName);
           return !file.exists()&&file.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean writeBytesToFile(byte[] data,File file){
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bos.write(data);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static void disableEnableViews(ViewGroup viewGroup,boolean enableDisable){
        int lenght=viewGroup.getChildCount();
        for(int i=0;i<lenght;i++){
            View view=viewGroup.getChildAt(i);
            if(view instanceof ViewGroup){
                disableEnableViews((ViewGroup)view,enableDisable);
            }else{
                view.setEnabled(enableDisable);
            }

        }
    }
    public static String hourToMinutes(int time){
        int hours = time / 60;
        int minutes = time % 60;
        return hours+"h "+minutes+"m";
    }

    public static void logData(String message){
        Log.i("Torrent",message);
    }

    public static void showAlertDialog(Context context,String message){
        new AlertDialog.Builder(context).setMessage(message).setPositiveButton(android.R.string.ok,null).create().show();
    }
    public static String byteCountToDisplaySize(long size) {
        String displaySize;

        if (size / ONE_GB > 0) {
            displaySize = String.format("%.2f", (float) size / ONE_GB) + " GB";
        } else if (size / ONE_MB > 0) {
            displaySize = String.format("%.2f",(float)size / ONE_MB) + " MB";
        } else if (size / ONE_KB > 0) {
            displaySize = String.format("%.2f", (float) size / ONE_KB) + " KB";
        } else {
            displaySize = String.valueOf(size) + " bytes";
        }
        return displaySize;
    }

    public static String getFreeSpace(String directoryPath){
        StatFs statFs = new StatFs(directoryPath);

        long blockSize=0;
        long availableBlocks=0;

        if(Build.VERSION.SDK_INT>17){
            blockSize=statFs.getBlockSizeLong();
            availableBlocks=statFs.getAvailableBlocksLong();
        }else{
            blockSize = statFs.getBlockSize();
            availableBlocks = statFs.getAvailableBlocks();
        }
        return Formatter.formatFileSize(Cache.getContext(),blockSize *availableBlocks);
    }


    public static void startTorrent(Activity activity,File torrentFile,TorrentHandleState torrentHandleState){
        Intent intent=new Intent(activity.getBaseContext(), TorrentService.class);
        intent.putExtra("torrent", torrentFile.getAbsolutePath());
        intent.putExtra("torrentHandleState",torrentHandleState);
        activity.startService(intent);
    }

    public static Map<String,String> getRequestHeaders(){
        Map<String, String> stringMap = new HashMap<>();

        stringMap.put("User-Agent", new WebView(Cache.getContext()).getSettings().getUserAgentString());
        stringMap.put("Host", "torrentz.eu");
        stringMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        stringMap.put("Accept-Language", "en-US,en;q=0.8");
        stringMap.put("Referer", "https://torrentz.eu/");
        return stringMap;
    }

    public static Map<String,String> getMovieHeaders(){
        Map<String, String> stringMap = new HashMap<>();

        stringMap.put("User-Agent", new WebView(Cache.getContext()).getSettings().getUserAgentString());
        stringMap.put("Host", "www.themoviedb.org");
        stringMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        stringMap.put("Accept-Language", "en-US,en;q=0.8");

        return stringMap;
    }

    public static String getDownloadLink(Context context){
        return "https://play.google.com/store/apps/details?id="+context.getPackageName()+"&hl=en";
    }
    public static void openFolder(AppCompatActivity appCompatActivity,String path) {
        try {
            Bundle bundle=new Bundle();
            bundle.putString("directory",path);
            MainActivity.bringFragment(appCompatActivity
                    , DirectoryExplorer.newInstance(bundle), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadAds(AdView mAdView){
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    public static void sendSuggestion(Context context,String[] email){
        try {
            PackageManager packageManager=context.getPackageManager();
            String packageName=context.getPackageName();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            String applicationLable=""+packageManager.getApplicationLabel(packageManager.
                    getApplicationInfo(packageName, 0));

            String email_title = "Feedback on"+" "+applicationLable;



            String description="\n\n\n\n\n\n\n\n" +"____________________________"+"\n"+
                    "Device Type:" +" "+
                    android.os.Build.MODEL +"\n"+
                    "Device OS:" +" "+ android.os.Build.VERSION.RELEASE+"\n"+
                    "App Version:"+ " "+packageInfo.versionName;
            Intent eIntent = new Intent(
                    Intent.ACTION_SEND);
            eIntent.setType("text/plain");
            eIntent.putExtra(
                    Intent.EXTRA_EMAIL,
                    email);
            eIntent.putExtra(
                    Intent.EXTRA_SUBJECT,
                    email_title);
            eIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    description);
            context.startActivity(eIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static byte[] getTorrentFromMagnet(String magenetLink){
        try {

            final Session session = new Session();
            final CountDownLatch signal = new CountDownLatch(1);

            AlertListener l = new AlertListener() {
                @Override
                public int[] types() {
                    return new int[]{AlertType.SESSION_STATS.getSwig(), AlertType.DHT_STATS.getSwig()};
                }

                @Override
                public void alert(Alert<?> alert) {
                    if (alert.getType().equals(AlertType.SESSION_STATS)) {
                        session.postDHTStats();
                    }

                    if (alert.getType().equals(AlertType.DHT_STATS)) {

                        long nodes = ((DhtStatsAlert) alert).totalNodes();
                        if (nodes >= 10) {
                            System.out.println("DHT contains " + nodes + " nodes");
                            signal.countDown();
                        }
                    }
                }
            };
            session.addListener(l);
            session.postDHTStats();

            Downloader downloader = new Downloader(session);


            boolean r = signal.await(10, TimeUnit.SECONDS);
            if (!r) {

                return null;
            }

            session.removeListener(l);

            return downloader.fetchMagnet(magenetLink, 30000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static Pair<Integer,String> getSelecteWithSize(List<FileMetadata> fileMetadataList){
        if(fileMetadataList!=null){
            int selectedFiles=0;
            long selectedFileSize=0;
            long fullSize=0;
            for(FileMetadata fileMetadata:fileMetadataList){
                if(fileMetadata.priority!= Priority.IGNORE){
                    selectedFiles++;
                    selectedFileSize+=fileMetadata.size;
                }
                fullSize+=fileMetadata.size;
            }
            return new Pair<>(selectedFiles,
                    String.format("%s / %s", Constant.byteCountToDisplaySize(selectedFileSize),
                            Constant.byteCountToDisplaySize(fullSize)));

        }
        return null;
    }

}
