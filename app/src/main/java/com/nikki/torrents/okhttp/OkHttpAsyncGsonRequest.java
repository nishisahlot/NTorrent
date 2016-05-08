package com.nikki.torrents.okhttp;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.util.Map;

/**
 * Created by Nishi on 10/13/2015.
 */
public class OkHttpAsyncGsonRequest<T> extends AsyncTask<Void,Void,T>{

    SetCallbacks<T> setCallbacks;
    Map<String,String> headers;
    private final Gson gson = new Gson();
    private final Class<T> clazz;
    Map<String,String> postParams;
    String url;



    public interface SetCallbacks<T>{
        void onError();
        void onSuccess(T response);
    }


    public OkHttpAsyncGsonRequest(String url, SetCallbacks<T> setCallbacks, Class<T> clazz){
        this(url,null,setCallbacks,clazz,null);
    }
    /***
     * @param url web url link
     */

    public OkHttpAsyncGsonRequest(String url, Map<String, String> headers, SetCallbacks<T> setCallbacks,
                                  Class<T> clazz,Map<String,String> postParams){
        this.clazz=clazz;
        this.setCallbacks=setCallbacks;
        this.headers=headers;
        this.postParams=postParams;
        this.url=url;
        execute();
    }



    @Override
    protected T doInBackground(Void... ks) {


        synchronized (OkHttpLock.lock){
            try {
                Request.Builder builder=new Request.Builder().url(url);
                if(headers!=null){
                    for (Map.Entry<String,String> entry:headers.entrySet())
                        builder.addHeader(entry.getKey(),entry.getValue());
                }
                if (postParams != null) {
                    FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
                    for (Map.Entry<String, String> entry : postParams.entrySet())
                        formEncodingBuilder.add(entry.getKey(), entry.getValue());
                    builder.post(formEncodingBuilder.build());
                }
                Response response=new OkHttpClient().newCall(builder.build()).execute();
                if(response!=null){

                    return gson.fromJson(response.body().string(),clazz);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return null;
    }

    @Override
    protected void onPostExecute(T t) {
        super.onPostExecute(t);

        try {
            if(t!=null){
                if(setCallbacks!=null){
                    setCallbacks.onSuccess(t);
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


}
