package net.beaconfun.myapplication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.RemoteException;
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

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;

public class HistoryActivity extends AppCompatActivity implements BeaconConsumer {
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final String TAG = "HistoryActivity";
    private static final String BEACON_UUID = "acdd0d58-e9e2-4899-b183-86b765c61009";
    private Realm myRealm;
    private HistoryAdapter adapter;
    BeaconManager mBeaconManager;
    Region region;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }

        myRealm = Realm.getDefaultInstance();


        createMockData();

        RealmResults<History> histories = myRealm.where(History.class).findAll();
        adapter = new HistoryAdapter(histories);
        ListView listView = (ListView) findViewById(R.id.HistoryList);
        listView.setAdapter(adapter);

        mBeaconManager = BeaconManager.getInstanceForApplication(this);
        String IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_FORMAT));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBeaconManager.bind(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBeaconManager.unbind(this);
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

    @Override
    public void onBeaconServiceConnect() {
        mBeaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Intent intent = new Intent(getApplicationContext(), SupSnapActivity.class);
                startActivity(intent);

                Log.i(TAG, "ビーコンを検出");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "ビーコンが検出範囲外へ");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: "+state);
            }
        });

        try {
            region = new Region(BEACON_UUID, null, null, null);
            mBeaconManager.startMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
            Log.e(TAG, e.toString());
        }
    }
}