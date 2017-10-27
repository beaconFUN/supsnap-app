package net.beaconfun.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
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
    byte[] bmp;
    private Realm r;
    @Override
    protected byte[] doInBackground(String... strings) {
        bmp = downloadImage();
        r = Realm.getDefaultInstance();


        r.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Number maxid = r.where(History.class).max("id");
                long nextid = 3;
                if(maxid != null){
                    nextid = maxid.longValue() + 1;
                }
                History history = realm.createObject(History.class,nextid);
                history.setLocation("aaaaaa");
                history.setThumbnail(bmp);
                history.setCreatedAt(new Date());


            }
        });

        Bitmap bitmap = BitmapFactory.decodeByteArray(bmp , 0, bmp .length);

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
        final String json = "{\"snap\": 7," +
                "\"pass_phrase\": \"4eb928b5a0955ce9f38615fc980ce627\"," +
                "\"user\": \"testuser\"," +
                "\"date\": \"2017-10-25T08:15:15\"," +
                "\"place\": 2, " +
                "\"id\": 9}";
        try {
            URL url = new URL(urlSt);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json charset=utf-8");
            con.setDoInput(true);
            con.connect();
            OutputStream os = con.getOutputStream();
            PrintStream ps = new PrintStream(os);
            ps.print(json);
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






