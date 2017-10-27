package net.beaconfun.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import io.realm.Realm;

public class SupSnapActivity extends AppCompatActivity {

    private final int COUNT_TIME = 8;


    Handler myHandler = new Handler();
    Timer myTimer = new Timer();
    Realm realm;
    private HistoryAdapter adapter2;
    AsyncNetwork task = new AsyncNetwork();



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

        /* todo
        {"minor": 2, "uuid": "4F215AA1-3904-47D5-AD5A-3B6AA89542AE", "major": 1, "id": 2}
        Place を {"minor": 2, "uuid": "4F215AA1-3904-47D5-AD5A-3B6AA89542AE", "major": 1} で http://35.200.2.51:5000/get_place
        に投げることで場所を取得し、historyID　の History のデータに追記
        */

        /* todo
        ビーコンのuuid, minor, majorを取得し、ユーザー名を追加してjson形式に変更し http://35.200.2.51:5000/get_visiter に POST する。
        POSTが完了し、visitor を取得したら、それをHistoryに追記　
        */


        ((TextView)findViewById(R.id.countView)).setText(String.valueOf(COUNT_TIME));

        startCountDown();

        realm = Realm.getDefaultInstance();
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
                            task.execute();
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
