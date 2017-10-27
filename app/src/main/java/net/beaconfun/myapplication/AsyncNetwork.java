package net.beaconfun.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import io.realm.Realm;


/**
 * Created by masanobuozaki on 2017/10/12.
 */

public class AsyncNetwork extends AsyncTask<String,Integer,byte[]> {
    private ImageView imageView;
    private long historyId;
    byte[] bmp;
    private Realm realm;
    private String visitorJsonString;
    private JSONObject visitorJson;

    @Override
    protected byte[] doInBackground(String... strings) {
        bmp = downloadImage();
        realm = Realm.getDefaultInstance();

        historyId = 3;// FIXME: 2017/10/27 サムネイルを保存したいHistoryのIDに書き換え　
        Log.d("historyId", String.valueOf(historyId));

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                History history = realm.where(History.class).equalTo("id", historyId).findFirst();
                history.setThumbnail(bmp);
                history.setCreatedAt(new Date());
            }
        });

        return null;
    }

    @Override
    protected void onPostExecute(byte[] byteArrayBuffers) {
        Log.d("onPost", "Start");
        super.onPostExecute(byteArrayBuffers);

        byte[] bytes = bmp;
        if (bytes != null) {
            Bitmap bit = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            // Log.d("tag", bit.toString());
        }
    }

    public byte[] downloadImage() {
        byte[] fd = new byte[5000];
        String urlSt = "http://35.200.2.51:5000/get_thum";

        /*
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                History history = realm.where(History.class).equalTo("id", historyId).findFirst();
                visitorJsonString = history.getVisitor();
            }
        });
        */

        // FIXME: 2017/10/27 データベースから getVisitor で visotorJsonString に値を代入するように修正
        visitorJsonString = "{\"snap\": 21, \"pass_phrase\": \"86763e164ea5275546f6d5a21f03f03f\", \"user\": \"testuser\", \"date\": \"2017-10-27T08:28:02\", \"place\": 2, \"id\": 26}";

        try {
            URL url = new URL(urlSt);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json charset=utf-8");
            con.setDoInput(true);
            con.connect();
            OutputStream os = con.getOutputStream();
            PrintStream ps = new PrintStream(os);
            ps.print(visitorJsonString);
            ps.close();
            int resp = con.getResponseCode();
            switch (resp) {
                case HttpURLConnection.HTTP_OK:
                    InputStream is = con.getInputStream();
                    is.read(fd);
                    is.close();
                    break;
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    break;
                default:
                    break;
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }


        return fd;
    }


}






