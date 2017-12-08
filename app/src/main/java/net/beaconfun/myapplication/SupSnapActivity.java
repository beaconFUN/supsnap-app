package net.beaconfun.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupSnapActivity extends AppCompatActivity {

    private final int COUNT_TIME = 25;


    Handler myHandler = new Handler();
    Timer myTimer = new Timer();
    Timer imageTimer = new Timer();
    ImageUpdater updater;
    Realm realm;
    private HistoryAdapter adapter2;
    AsyncNetwork task = new AsyncNetwork();
    String visitorString;
    String location;
    long historyId = 0;
    String uuid;
    String major;
    String minor;
    Bitmap thumBmp;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sup_snap);

        Intent intent = getIntent();
        uuid = intent.getStringExtra("uuid");
        major = intent.getStringExtra("major");
        minor = intent.getStringExtra("minor");
        realm = Realm.getDefaultInstance();


        // 新しいHisotryオブジェクトを作成する
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Number maxid = realm.where(History.class).max("id");
                if (maxid != null) {
                    historyId = maxid.longValue() + 1;
                }
                History history = realm.createObject(History.class, historyId);
                history.setCreatedAt(new Date());
            }
        });

        getVisitor();

        getLocation();

        ((TextView) findViewById(R.id.countView)).setText(String.valueOf(COUNT_TIME));

        startCountDown();

    }

    private void getVisitor() {
        String url = "http://35.200.2.51:5000/get_visiter";
        Random rnd = new Random();
        int ran = rnd.nextInt(10000000);
        Log.d("RANDOM", "" + ran);
        String json = "{\"beacon\": {\"minor\": 2, \"uuid\": \"4F215AA1-3904-47D5-AD5A-3B6AA89542AE\", \"major\": 1}, \"user\": \"testuser" + ran + "\"}";

        OkHttpClient client = new OkHttpClient();
        MediaType MIMEType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(MIMEType, json);
        Request request = null;
        try {
            request = new Request.Builder().url(new URL(url)).post(requestBody).build();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "getVisitor failed.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).start();
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "getVisitor success.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).start();

                try {
                    String str = response.body().string();
                    JSONObject resObject = new JSONObject(str);
                    visitorString = resObject.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                realm = Realm.getDefaultInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        History history = realm.where(History.class).equalTo("id", historyId).findFirst();
                        history.setVisitor(visitorString);

                        imageTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                ImageUpdater updater = new ImageUpdater((ImageView) findViewById(R.id.streamImage));
                                updater.execute(visitorString);
                            }
                        }, 0, 100);

                    }
                });
            }
        });
    }

    private void getLocation() {
        String url = "http://35.200.2.51:5000/get_place";
        String json = "{\"minor\": 2, \"uuid\": \"4F215AA1-3904-47D5-AD5A-3B6AA89542AE\", \"major\": 1, \"id\": 2}";

        OkHttpClient client = new OkHttpClient();
        MediaType MIMEType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(MIMEType, json);
        Request request = null;
        try {
            request = new Request.Builder().url(new URL(url)).post(requestBody).build();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "getLocation failed.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).start();
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "getLocation success.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).start();

                try {
                    String str = response.body().string();
                    JSONObject resObject = new JSONObject(str);
                    location = resObject.getString("name");
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                realm = Realm.getDefaultInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        History history = realm.where(History.class).equalTo("id", historyId).findFirst();
                        history.setLocation(location);
                    }
                });
            }
        });

    }

    private void saveBmp(){
        final String urlSt = "http://35.200.2.51:5000/get_thum";
        OkHttpClient client = new OkHttpClient();
        MediaType MIMEType= MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create (MIMEType, visitorString);
        Request request = null;
        try {
            request = new Request.Builder().url(new URL(urlSt)).post(requestBody).build();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                thumBmp = BitmapFactory.decodeStream(response.body().byteStream());

                realm = Realm.getDefaultInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        thumBmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
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
        });

    }

    private void waitWhileSnapping() {
        realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Number maxid = realm.where(History.class).max("id");
                if(maxid != null){
                    historyId = maxid.longValue();
                }
                History history = realm.where(History.class).equalTo("id", historyId).findFirst();
                visitorString = history.getVisitor();
            }
        });

        OkHttpClient client = new OkHttpClient();
        MediaType MIMEType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(MIMEType, visitorString);
        Request request = null;
        try {
            request = new Request.Builder().url(new URL("http://35.200.2.51:5000/get_snap_state")).post(requestBody).build();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String str = response.body().string();
                    JSONObject resObject = new JSONObject(str);
                    if (resObject.getBoolean("done")) {

                        saveBmp();
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
        });

    }

    private void startCountDown() {
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        TextView countView = (TextView) findViewById(R.id.countView);
                        int n = Integer.valueOf(countView.getText().toString());
                        if (n - 1 == 0) {
                            task.execute();
                            countView.setText("0");
                            waitWhileSnapping();
                            // サムネイルを保存
                            finish();
                        } else {
                            countView.setText(String.valueOf(n - 1));
                        }
                    }
                });
            }
        }, 0, 1000);

        Log.d("imageTimer", "start");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        myTimer.cancel();
        imageTimer.cancel();
    }
}
