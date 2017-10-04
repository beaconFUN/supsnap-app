package net.beaconfun.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by masanobuozaki on 2017/10/03.
 */

public class dialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
       AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
               return builder.setTitle("kon")
                       .setMessage("konnni")
                       .setPositiveButton("画像", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               // YES button pressed
                           }
                       })
                       .setNeutralButton("QRコード", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               // Later button pressed
                           }
                       })
                       .setNegativeButton("戻る", null)
                       .create();
    }
}
