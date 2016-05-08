package com.nikki.torrents.asyncTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.google.common.io.ByteStreams;
import com.nikki.torrents.R;
import com.nikki.torrents.models.Torrentz;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by Nishi Sahlot on 2/21/2016.
 */
public class TorrentzParseAsyncTask extends AsyncTask<Void,String,Void>{

    List<Torrentz> torrentzs;
    Activity activity;
    ProgressDialog progressDialog;

    public TorrentzParseAsyncTask(List<Torrentz> torrentzs,Activity activity){
        this.torrentzs=torrentzs;
        this.activity=activity;
        execute();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog=new ProgressDialog(activity);
        progressDialog.setMessage(activity.getString(R.string.please_wait));
        progressDialog.show();
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancel(false);
            }
        });
    }

    @Override
    protected Void doInBackground(Void... params) {

        int index=1;
        for(Torrentz torrentz:torrentzs){
            publishProgress("Parsing Data "+( index++));
            HttpURLConnection httpURLConnection=null;
            try {
                httpURLConnection=(HttpURLConnection)new URL(torrentz.torrentzLink).openConnection();
                String response=new String(ByteStreams.toByteArray(httpURLConnection.getInputStream()));
                Document document= Jsoup.parse(response);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(httpURLConnection!=null)
                    httpURLConnection.disconnect();
            }

        }



        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }
}
