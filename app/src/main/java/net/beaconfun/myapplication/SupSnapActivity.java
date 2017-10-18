package net.beaconfun.myapplication;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Date;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.RealmQuery;

public class SupSnapActivity extends AppCompatActivity {

    private final int COUNT_TIME = 5;


    Handler myHandler = new Handler();
    Timer myTimer = new Timer();
    Realm realm;
    private HistoryAdapter adapter2;
    AsyncNetwork task = new AsyncNetwork();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sup_snap);

        ((TextView)findViewById(R.id.countView)).setText(String.valueOf(COUNT_TIME));

        startCountDown();

        realm = Realm.getDefaultInstance();
    }
    private void MockData() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                History history = realm.createObject(History.class, 3);
                history.setLocation("未来");
                history.setCreatedAt(new Date());

            }
        });
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
                            //MockData();
                            task.execute();
                            // FIXME: 2017/10/11 撮影機能を実装
                            countView.setText("Snap!!");
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
