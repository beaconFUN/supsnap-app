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


/**
 * Created by masanobuozaki on 2017/10/03.
 */

public class dialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.thum_dialog,null);
        ImageView imageView = layout.findViewById(R.id.modalThum);

        int positionInt = getArguments().getInt("position");
        Log.d("position" ,"ダイアログが受け取った"+positionInt);

        byte[] b = getArguments().getByteArray("thum"); // TODO: 2017/10/27 画像を受けとる
        if (b != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(b , 0, b .length);
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.drawable.p350x150);
        }
        builder.setView(layout);

        return builder.create();
    }
}
