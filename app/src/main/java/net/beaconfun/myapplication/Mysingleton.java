package net.beaconfun.myapplication;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by masanobuozaki on 2017/10/06.
 */

class Mysingleton {
    private  static  Mysingleton ourInstance;
    private RequestQueue mRequestQueue;
    private  static Context mCtx;

    public static synchronized Mysingleton getInstance(Context context) {
        if(ourInstance==null){
            ourInstance = new Mysingleton(context);
        }
        return ourInstance;
    }

    private Mysingleton(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }
    public RequestQueue getRequestQueue(){
        if(mRequestQueue == null){
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
    return mRequestQueue;
    }
    public <T> void addToRequestQueue(Request<T> req){
        getRequestQueue().add(req);
    }

}
