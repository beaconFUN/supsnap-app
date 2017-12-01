package net.beaconfun.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static net.beaconfun.myapplication.R.drawable.download;


/**
 * Created by masanobuozaki on 2017/10/03.
 */

public class dialog extends DialogFragment{
    Realm realm;
    long historyId = 1;
    byte[] thum = new byte[100000];
    String visitorJson;
    ImageView imageView;
    String visitorjson;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.thum_dialog,null);
        imageView = layout.findViewById(R.id.modalThum);




        int positionInt = getArguments().getInt("position") + 1;
        Log.d("position" ,"ダイアログが受け取った" + positionInt);
        historyId = positionInt;

        realm = Realm.getDefaultInstance();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                History history = realm.where(History.class).equalTo("id", historyId).findFirst();
                thum = history.getThumbnail();
                visitorjson = history.getVisitor();
            }
        });

        if (thum != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(thum , 0, thum.length);
            Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() * 10, bitmap.getHeight() * 10, false);
            imageView.setImageBitmap(bitmap2);
        } else {
            imageView.setImageResource(R.drawable.p350x150);
        }
        builder.setView(layout);

        layout.findViewById(R.id.qr_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              onTapped(v);
            }
        });

        layout.findViewById(R.id.download_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    getimage();
                }
            }
        });

        return builder.create();
    }

    private void onTapped(View v) {
        Bitmap qrCodeBitmap = this.createQRCode("http://35.202.128.133:5000/get_image?visiter="+visitorjson);
        Bitmap qrcode = Bitmap.createScaledBitmap(qrCodeBitmap, qrCodeBitmap.getWidth() * 5, qrCodeBitmap.getHeight() * 5, false);
        imageView.setImageResource(0);
        imageView.setImageBitmap(qrcode);

    }

    private Bitmap createQRCode(String contents) {
        Bitmap qrBitmap = null;
        try {
            // QRコードの生成
            QRCodeWriter qrcodewriter = new QRCodeWriter();
            BitMatrix qrBitMatrix = qrcodewriter.encode(contents,
                    BarcodeFormat.QR_CODE,
                    300,
                    300);
            qrBitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
            qrBitmap.setPixels(this.createDot(qrBitMatrix), 0, 300, 0, 0, 300, 300);
        }
        catch(Exception ex)
        {
            // エンコード失敗
            Log.d("qrcode","failed");
        }
        finally
        {
            return qrBitmap;
        }
    }
    // ドット単位の判定
    private int[] createDot(BitMatrix qrBitMatrix)
    {
        // 縦幅・横幅の取得
        int width = qrBitMatrix.getWidth();
        int height = qrBitMatrix.getHeight();
        // 枠の生成
        int[] pixels = new int[width * height];
        // データが存在するところを黒にする
        for (int y = 0; y < height; y++)
        {
            // ループ回数盤目のOffsetの取得
            int offset = y * width;
            for (int x = 0; x < width; x++)
            {
                // データが存在する場合
                if (qrBitMatrix.get(x, y))
                {
                    pixels[offset + x] = Color.BLACK;
                }
                else
                {
                    pixels[offset + x] = Color.WHITE;
                }
            }
        }
        // 結果を返す
        return pixels;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getimage(){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("http://35.202.128.133:5000/get_image?visiter="+visitorjson).build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream inputStream = response.body().byteStream();
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, "Hey view/download this image");
        String path = MediaStore.Images.Media.insertImage(this.getContext().getContentResolver(), bitmap, "", null);
        Uri screenshotUri = Uri.parse(path);

        intent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
        intent.setType("image/*");
        startActivity(Intent.createChooser(intent, "Share image via..."));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getLocation() {
        String url = "http://35.202.128.133:5000/get_image";

        JSONObject jsonObject = null;

        realm = Realm.getDefaultInstance();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                History history = realm.where(History.class).equalTo("id", historyId).findFirst();
                visitorJson = history.getVisitor();
            }
        });

        try {
            jsonObject = new JSONObject(visitorJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /*
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {
                        try {
                            Log.d("TAG", response.toString(2));
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    History history = realm.where(History.class).equalTo("id", historyId).findFirst();
                                    try {
                                        history.setLocation(response.get("name").toString());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("TAG", error.toString());
                    }
                }
        );

        Mysingleton.getInstance(this.getContext()).addToRequestQueue(jsObjRequest);
        */
    }

}
