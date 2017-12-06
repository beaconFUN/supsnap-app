package net.beaconfun.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;


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

        return builder.create();
    }

    private void onTapped(View v) {
        Bitmap qrCodeBitmap = this.createQRCode("http://35.200.2.51:5000/get_image?visiter="+visitorjson);
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

    private void getImage() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                History history = realm.where(History.class).equalTo("id", historyId).findFirst();
                // TODO: 2017/10/27 画像ダウンロード機能を実装する
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getLocation() {
        String url = "http://35.200.2.51:5000/get_image";

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
    }

}
