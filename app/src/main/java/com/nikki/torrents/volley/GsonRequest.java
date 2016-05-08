package com.nikki.torrents.volley;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;

/**
 * Created by ws005 on 12/20/2014.
 */
public class GsonRequest<T> extends Request<T> {

    Response.Listener<T> responseListener;

    private final Gson gson = new Gson();
    private final Class<T> clazz;



    public GsonRequest(int method, String url, Response.Listener<T> responseListener,
                       Response.ErrorListener errorListener, Class<T> clazz) {
        super(method, url, errorListener);


        this.clazz = clazz;
        this.responseListener = responseListener;

    }



    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers));

            return Response.success(
                    gson.fromJson(json, clazz),
                    HttpHeaderParser.parseCacheHeaders(response));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void deliverResponse(T response) {
        responseListener.onResponse(response);

    }


}
