package net.beaconfun.myapplication;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.app.DialogFragment;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.LinkedList;

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

    class ID {
        public String major;
        public String minor;
        public ID(String major, String minor){
            this.major = major;
            this.minor = minor;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            ID other = (ID)obj;
            return other.major.equals(this.major) && other.minor.equals(this.minor);
        }
    }
    LinkedList<ID> idList = new LinkedList<ID>();


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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DialogFragment dialog = new dialog();
                Bundle args = new Bundle();
                args.putInt("position", position); // TODO: 2017/10/27 画像データをうけわたす
                dialog.setArguments(args);
                dialog.show(getFragmentManager(), "dialog_basic");
                Log.d("position", "ビューは「" + position + "」");
            }
        });

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

    private void enterBeaconRange(String uuid, String major, final String minor) {
        Intent intent = new Intent(getApplicationContext(), SupSnapActivity.class);
        for(ID id: idList) {
            if (id.equals(new ID(major, minor))) {
                return;
            }
        }
        idList.add(new ID(major, minor));
        intent.putExtra("uuid", uuid);
        intent.putExtra("major", major);
        intent.putExtra("minor", minor);
        startActivity(intent);
    }

    private void createMockData() {
        myRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                Resources res = getResources();
                Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.qr);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] bytes = stream.toByteArray();

                History history = realm.createObject(History.class, 1);
                history.setLocation("未来大");
                history.setCreatedAt(new Date());
                history.setThumbnail(bytes);

                History history2 = realm.createObject(History.class, 2);
                history2.setLocation("函館山");
                history2.setCreatedAt(new Date());
                history2.setThumbnail(bytes);
            }
        });
    }

    public void onTapped(View view) {

        enterBeaconRange("", "1", "2");


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myRealm.close();
    }

    @Override
    public void onBeaconServiceConnect() {
        Identifier uuid = Identifier.parse(BEACON_UUID);
        final Region mRegion = new Region("ibeacon", uuid, null, null);

        mBeaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                for (Beacon beacon : collection) {
                    Log.d("ID1", beacon.getId1().toString());
                    Log.d("ID2", beacon.getId2().toString());
                    final String major = beacon.getId2().toString();
                    final String minor = beacon.getId3().toString();

                    Log.d("ID3", beacon.getId3().toString());
                    enterBeaconRange(beacon.getId1().toString(), beacon.getId2().toString(), beacon.getId3().toString());
                }

                Log.i(TAG, "ビーコンを検出");
            }
        });

        mBeaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "ビーコンが検出範囲内へ");
                //レンジングの開始
                try {
                    mBeaconManager.startRangingBeaconsInRegion(mRegion);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "ビーコンが検出範囲外へ");
                try {
                    mBeaconManager.stopRangingBeaconsInRegion(mRegion);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: " + state);
            }
        });
        try {
            mBeaconManager.startMonitoringBeaconsInRegion(mRegion);
        } catch (RemoteException e) {
            Log.e(TAG, e.toString());
        }
    }
}