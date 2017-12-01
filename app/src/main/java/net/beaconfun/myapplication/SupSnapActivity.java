package net.beaconfun.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;

public class SupSnapActivity extends AppCompatActivity {

    private final int COUNT_TIME = 25;


    Handler myHandler = new Handler();
    Timer myTimer = new Timer();
    Timer imageTimer = new Timer();
    Realm realm;
    private HistoryAdapter adapter2;
    AsyncNetwork task = new AsyncNetwork();
    long historyId = 0;
    String uuid;
    String major;
    String minor;

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
                if(maxid != null){
                    historyId = maxid.longValue() + 1;
                }
                History history = realm.createObject(History.class,historyId);
                history.setCreatedAt(new Date());
            }
        });

        getLocation();

        getVisitor();


        ((TextView)findViewById(R.id.countView)).setText(String.valueOf(COUNT_TIME));

        startCountDown();

    }

    private void getImageURL() {
        String url = "http://35.202.128.133:5000/get_visiter";
        String json = "{\"user\": \"testuser\", \"beacon\": {\"minor\": " + minor + ", \"uuid\": \"4F215AA1-3904-47D5-AD5A-3B6AA89542AE\", \"major\": " + major + ", \"id\": 2}}";

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
            System.out.println(jsonObject.getString("user"));
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
                                    Log.d("Visitor", response.toString());
                                    history.setVisitor(response.toString());
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
        Mysingleton.getInstance(getApplicationContext()).addToRequestQueue(jsObjRequest);
    }

    private void getVisitor() {
        String url = "http://35.202.128.133:5000/get_visiter";
        Random rnd = new Random();
        int ran = rnd.nextInt(10000000);
        Log.d("RANDOM", "" + ran);
        String json = "{\"beacon\": {\"minor\": 2, \"uuid\": \"4F215AA1-3904-47D5-AD5A-3B6AA89542AE\", \"major\": 1}, \"user\": \"testuser" + ran + "\"}";


        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
            System.out.println(jsonObject.getString("user"));
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
                                    Log.d("Visitor", response.toString());
                                    history.setVisitor(response.toString());
                                }
                            });

                            imageTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    ImageUpdater updater = new ImageUpdater((ImageView) findViewById(R.id.streamImage));
                                    updater.execute(response.toString());
                                }
                            }, 0, 100);

                        } catch (JSONException e) {
                            Log.d("JSON EXCEPTION", "EXCEPTION");
                        }

                        getImageURL();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("TAG", error.toString());
                    }
                }
        );
        Mysingleton.getInstance(getApplicationContext()).addToRequestQueue(jsObjRequest);
    }

    private void getLocation() {
        String url = "http://35.202.128.133:5000/get_place";
        String json = "{\"minor\": 2, \"uuid\": \"4F215AA1-3904-47D5-AD5A-3B6AA89542AE\", \"major\": 1, \"id\": 2}";

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
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
        Mysingleton.getInstance(getApplicationContext()).addToRequestQueue(jsObjRequest);

    }

    private void startCountDown(){
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        TextView countView = (TextView)findViewById(R.id.countView);
                        int n = Integer.valueOf(countView.getText().toString());
                        if (n - 1 == 0) {
                            task.execute();
                            countView.setText("0");
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
