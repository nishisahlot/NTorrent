package com.nikki.torrents.volley;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.Map;

/**
 * Created by Nishi on 10/8/2015.
 */
public class StringOverRidedRequest extends StringRequest {
    Map<String, String> requestHeaderParams,requestPostParams;

    public StringOverRidedRequest(int method, String url, Response.Listener<String> listener,
                                  Response.ErrorListener errorListener, Map<String, String> requestHeaderParams,
                                  Map<String, String> requestPostParams) {
        super(method, url, listener, errorListener);
        this.requestHeaderParams=requestHeaderParams;
        this.requestPostParams=requestPostParams;
    }


    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return requestPostParams!=null?requestPostParams:super.getParams();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return requestHeaderParams!=null?requestHeaderParams:super.getHeaders();
    }
}
