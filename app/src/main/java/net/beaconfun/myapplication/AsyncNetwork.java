package net.beaconfun.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import io.realm.Realm;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by masanobuozaki on 2017/10/12.
 */

public class AsyncNetwork extends AsyncTask<String,Integer,Bitmap> {
    private long historyId;
    Bitmap bmp;
    private Realm realm;
    private String visitorJsonString;

    @Override
    protected Bitmap doInBackground(String... strings) {
        final String urlSt = "http://35.200.2.51:5000/get_thum";
        realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Number maxid = realm.where(History.class).max("id");
                if(maxid != null){
                    historyId = maxid.longValue();
                }
                History history = realm.where(History.class).equalTo("id", historyId).findFirst();
                visitorJsonString = history.getVisitor();
            }
        });


        while(true) {
            OkHttpClient client = new OkHttpClient();
            MediaType MIMEType= MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create (MIMEType, visitorJsonString);
            Request request = null;
            try {
                request = new Request.Builder().url(new URL("http://35.200.2.51:5000/get_snap_state")).post(requestBody).build();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            Response response = null;
            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                String str = response.body().string();
                JSONObject resObject = new JSONObject(str);
                if (resObject.getBoolean("done")){
                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }


        OkHttpClient client = new OkHttpClient();
        MediaType MIMEType= MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create (MIMEType, visitorJsonString);
        Request request = null;
        try {
            request = new Request.Builder().url(new URL(urlSt)).post(requestBody).build();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return BitmapFactory.decodeStream(response.body().byteStream());
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        Log.d("onPost", "Start");
        super.onPostExecute(bitmap);
        realm = Realm.getDefaultInstance();

        historyId = 1;
        Log.d("historyId", String.valueOf(historyId));
        bmp = bitmap;

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                Number maxid = realm.where(History.class).max("id");
                if(maxid != null){
                    historyId = maxid.longValue();
                }
                History history = realm.where(History.class).equalTo("id", historyId).findFirst();
                history.setThumbnail(bos.toByteArray());
                history.setCreatedAt(new Date());
            }
        });
    }

}






