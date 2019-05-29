package edu.uoc.mistic.tfm.jherranzm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import net.glxn.qrgen.android.QRCode;

import edu.uoc.mistic.tfm.jherranzm.util.TFMSecurityManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 2019-03-30
        // Check whether this app has write external storage permission or not.
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // If do not grant write external storage permission.
        if(writeExternalStoragePermission!= PackageManager.PERMISSION_GRANTED) {
            // Request user to grant write external storage permission.
            ActivityCompat.requestPermissions(SplashActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }




        Bitmap myBitmap = QRCode.from("MISTIC TFM Invoices by jherranzm@uoc.edu").bitmap();
        ImageView myImage = findViewById(R.id.imageView);
        myImage.setImageBitmap(myBitmap);


        Thread logoTimer = new Thread() {
            public void run() {
                try {

                    sleep(3000);

                    TFMSecurityManager tfmSecurityManager = TFMSecurityManager.getInstance(SplashActivity.this);
                } catch (InterruptedException e) {
                    Log.d("Exception", "Exception" + e);
                } finally {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                }
                finish();
            }
        };
        logoTimer.start();
    }
}
