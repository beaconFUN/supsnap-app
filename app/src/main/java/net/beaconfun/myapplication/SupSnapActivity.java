package net.beaconfun.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
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

    private final int COUNT_TIME = 8;


    Handler myHandler = new Handler();
    Timer myTimer = new Timer();
    Realm realm;
    private HistoryAdapter adapter2;
    AsyncNetwork task = new AsyncNetwork();
    long historyId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) { // FIXME: 2017/10/20 作成したhistoryのデータを受け取る
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sup_snap);

        Intent intent = getIntent();
        String uuid = intent.getStringExtra("uuid"); //// FIXME: 2017/10/20
        String major = intent.getStringExtra("major"); //// FIXME: 2017/10/20
        String minor = intent.getStringExtra("minor"); //// FIXME: 2017/10/20
        Log.d("UUID", uuid); // 1
        Log.d("major", major); // 2
        Log.d("minor", minor); // 3

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
        String url = "http://35.200.2.51:5000/get_visiter";
        String json = "{\"user\": \"testuser\", \"beacon\": {\"minor\": 2, \"uuid\": \"4F215AA1-3904-47D5-AD5A-3B6AA89542AE\", \"major\": 1, \"id\": 2}}";

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
        String url = "http://35.200.2.51:5000/get_visiter";
        String json = "{\"user\": \"testuser\", \"beacon\": {\"minor\": 2, \"uuid\": \"4F215AA1-3904-47D5-AD5A-3B6AA89542AE\", \"major\": 1, \"id\": 2}}";

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
        String url = "http://35.200.2.51:5000/get_place";
        String json = "{\"minor\": 2, \"uuid\": \"4F215AA1-3904-47D5-AD5A-3B6AA89542AE\", \"major\": 1, \"id\": 2}";

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
            System.out.println(jsonObject.getString("screen_name"));
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
                // FIXME: 2017/10/11 撮影が
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        TextView countView = (TextView)findViewById(R.id.countView);
                        int n = Integer.valueOf(countView.getText().toString());
                        if (n - 1 == 0) {
                            task.execute();  // FIXME: 2017/10/27 historyIdを渡す　
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        myTimer.cancel();
    }
}
