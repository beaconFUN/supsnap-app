package net.beaconfun.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by akiya on 2017/11/03.
 */

public class ImageUpdater extends AsyncTask<String, Integer, Bitmap> {
    private ImageView targetImageView;
    private String json;

    public final String API_URL = "http://35.202.128.133:5000/get_live_view";

    public ImageUpdater(ImageView v) {
        targetImageView = v;
    }

    /**
     * Defines work to perform on the background thread.
     */
    @Override
    protected Bitmap doInBackground(String... jsonString) {
        Bitmap bmp = null;
        if (!isCancelled() && jsonString != null && jsonString.length > 0) {
            try {
                json = jsonString[0];
                URL url = new URL(API_URL);
                bmp = downloadUrl(url);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return bmp;
    }

    /**
     * Given a URL, sets up a connection and gets the HTTP response body from the server.
     * If the network request is successful, it returns the response body in String form. Otherwise,
     * it will throw an IOException.
     */
    private Bitmap downloadUrl(URL url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        MediaType MIMEType= MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create (MIMEType, json);
        Request request = new Request.Builder().url(url).post(requestBody).build();
        Response response = client.newCall(request).execute();
        if (response.code() == 404)
            return null;
        return BitmapFactory.decodeStream(response.body().byteStream());
    }

    /**
     * Updates the DownloadCallback with the result.
     */
    @Override
    protected void onPostExecute(Bitmap bmp) {
        if (bmp == null)
            return;
        targetImageView.setImageBitmap(bmp);
    }

    /**
     * Converts the contents of an InputStream to a String.
     */
    public Bitmap readStream(InputStream stream)
            throws IOException, UnsupportedEncodingException {
        return BitmapFactory.decodeStream(stream);
    }
}
