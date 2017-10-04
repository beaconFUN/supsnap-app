package net.beaconfun.myapplication;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.app.DialogFragment;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
    }

    public void onTapped(View view) {
        Intent intent = new Intent(this, SupSnapActivity.class);
        startActivity(intent);
    }

    public void  onTapped2(View view){
        ImageView iv = new ImageView(this);
        iv.setImageResource(R.drawable.qrcode);
        iv.setAdjustViewBounds(true);
        new AlertDialog.Builder(this)
                .setView(iv)
                .show();
    }

    public void dialog(View view){
        DialogFragment dialog = new dialog();
        dialog.show(getFragmentManager(), "dialog_basic");
    }
}
