package net.beaconfun.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.widget.TextView;

public class SupSnapActivity extends AppCompatActivity {

    private final int COUNT_TIME = 5;

    Handler myHandler = new Handler();
    Timer myTimer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sup_snap);

        ((TextView)findViewById(R.id.countView)).setText(String.valueOf(COUNT_TIME));

        startCountDown();
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
