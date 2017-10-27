package net.beaconfun.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import io.realm.Realm;


/**
 * Created by masanobuozaki on 2017/10/03.
 */

public class dialog extends DialogFragment {
    Realm realm;
    long historyId = 1;
    byte[] thum = new byte[100000];


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.thum_dialog,null);
        ImageView imageView = layout.findViewById(R.id.modalThum);

        int positionInt = getArguments().getInt("position") + 1;
        Log.d("position" ,"ダイアログが受け取った" + positionInt);
        historyId = positionInt;

        realm = Realm.getDefaultInstance();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                History history = realm.where(History.class).equalTo("id", historyId).findFirst();
                thum = history.getThumbnail();
            }
        });

        if (thum != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(thum , 0, thum.length);
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.drawable.p350x150);
        }
        builder.setView(layout);

        return builder.create();
    }

    private void getImage() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                History history = realm.where(History.class).equalTo("id", historyId).findFirst();
                // TODO: 2017/10/27 画像ダウンロード機能を実装する
            }
        });
    }
}
