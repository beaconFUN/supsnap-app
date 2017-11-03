package net.beaconfun.myapplication;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by akiya on 2017/11/03.
 */

public class ImageUpdater extends AsyncTask<Integer, Integer, Bitmap> {
    private ImageView targetImageView;
    public final String API_URL = "http://lorempixel.com/400/200/";


    public ImageUpdater(ImageView v) {
        targetImageView = v;
    }

    /**
     * Defines work to perform on the background thread.
     */
    @Override
    protected Bitmap doInBackground(Integer... urls) {
        Bitmap bmp = null;
        if (!isCancelled() && urls != null && urls.length > 0) {
            try {
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
        InputStream stream = null;
        HttpURLConnection connection = null;
        Bitmap result = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            // Timeout for reading InputStream arbitrarily set to 3000ms.
            connection.setReadTimeout(3000);
            // Timeout for connection.connect() arbitrarily set to 3000ms.
            connection.setConnectTimeout(3000);
            // For this use case, set HTTP method to GET.
            connection.setRequestMethod("GET");
            // Already true by default but setting just in case; needs to be true since this request
            // is carrying an input (response) body.
            connection.setDoInput(true);
            // Open communications link (network traffic occurs here).
            connection.connect();

            // publishProgress(DownloadCallback.Progress.CONNECT_SUCCESS);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpsURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }
            // Retrieve the response body as an InputStream.
            stream = connection.getInputStream();

            // publishProgress(DownloadCallback.Progress.GET_INPUT_STREAM_SUCCESS, 0);

            if (stream != null) {
                // Converts Stream to String with max length of 500.
                result = readStream(stream);
            }
        } finally {
            // Close Stream and disconnect HTTPS connection.
            if (stream != null) {
                stream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        Log.d("ImageUpdater", "Doing");
        return result;
    }

    /**
     * Updates the DownloadCallback with the result.
     */
    @Override
    protected void onPostExecute(Bitmap bmp) {
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
