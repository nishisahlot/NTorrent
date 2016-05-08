package com.nikki.torrents.okhttp;

import android.os.AsyncTask;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nishi on 10/15/2015.
 */
public class OkHttpAsyncStringRequest  extends AsyncTask<Void,Void,String> {

    SetCallbacks<String> setCallbacks;
    Map<String,String> headers;
    Map<String,String> postParams;
    String url;

    public OkHttpAsyncStringRequest(String url, SetCallbacks<String> setCallbacks,Map<String, String> headers,
                                    Map<String,String> postParams){
        this.url=url;
        this.setCallbacks=setCallbacks;
        this.headers=headers;
        this.postParams=postParams;
        execute();
    }
    @Override
    protected String doInBackground(Void... voids) {
        synchronized (OkHttpLock.lock) {
            try {
                Request.Builder builder = new Request.Builder().url(url);
                if (headers != null) {
                    for (Map.Entry<String, String> entry : headers.entrySet()){
                        builder.addHeader(entry.getKey(), entry.getValue());

                    }

                }
                if (postParams != null) {
                    FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
                    for (Map.Entry<String, String> entry : postParams.entrySet())
                        formEncodingBuilder.add(entry.getKey(), entry.getValue());
                    builder.post(formEncodingBuilder.build());
                }

                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(15, TimeUnit.SECONDS);
                client.setReadTimeout(15, TimeUnit.SECONDS);
                client.setWriteTimeout(15, TimeUnit.SECONDS);


                String response= client.newCall(builder.build()).execute().body().string();
                return response;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        try {

            if(s!=null){
                if(setCallbacks!=null){
                    setCallbacks.onSuccess(s);
                }
            }else{
                if(setCallbacks!=null){
                    setCallbacks.onError();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface SetCallbacks<T>{
        void onError();
        void onSuccess(T response);
    }



}
