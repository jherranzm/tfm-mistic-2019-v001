package com.example.apptestvalidationandroid44.util;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import com.example.apptestvalidationandroid44.InvoiceApp;
import com.example.apptestvalidationandroid44.R;

public class UtilScreenMessages {

    public static final String TAG = "UtilScreenMessages";

    public static void infoShow( String message ) {
        AlertDialog alertDialog = new AlertDialog.Builder(InvoiceApp.getContext()).create();
        alertDialog.setTitle("Info");
        alertDialog.setMessage(message);
        alertDialog.setIcon(R.drawable.ic_launcher_background);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "You clicked on OK");
            }
        });

        alertDialog.show();
    }

    public static  void alertShow( String message ) {
        AlertDialog alertDialog = new AlertDialog.Builder(InvoiceApp.getContext()).create();
        alertDialog.setTitle("Alert!");
        alertDialog.setMessage(message);
        alertDialog.setIcon(R.drawable.ic_launcher_background);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "You clicked on OK");
            }
        });

        alertDialog.show();
    }


}
