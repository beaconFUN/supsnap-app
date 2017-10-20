package net.beaconfun.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.app.DialogFragment;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;

public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryActivity";
    private Realm myRealm;
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        myRealm = Realm.getDefaultInstance();


        createMockData();

        RealmResults<History> histories = myRealm.where(History.class).findAll();
        adapter = new HistoryAdapter(histories);
        ListView listView = (ListView) findViewById(R.id.HistoryList);
        listView.setAdapter(adapter);
    }

    private void createMockData() {
        myRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                History history = realm.createObject(History.class,1);
                history.setLocation("未来大");
                history.setCreatedAt(new Date());

                History history2 = realm.createObject(History.class, 2);
                history2.setLocation("函館山");
                history2.setCreatedAt(new Date());
            }
        });
    }

    public void onTapped(View view) {

        String url = "http://35.200.2.51:5000/models/Visiter";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, response.toString(2));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, error.toString());
                    }
                }
        );
        Mysingleton.getInstance(getApplicationContext()).addToRequestQueue(jsObjRequest);

        Intent intent = new Intent(this, SupSnapActivity.class);
        startActivity(intent);
    }

    public void dialog(View view){
        DialogFragment dialog = new dialog();
        dialog.show(getFragmentManager(), "dialog_basic");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myRealm.close();
    }
}
