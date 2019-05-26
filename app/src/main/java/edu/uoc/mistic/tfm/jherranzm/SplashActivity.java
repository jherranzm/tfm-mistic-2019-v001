package edu.uoc.mistic.tfm.jherranzm;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
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


        Bitmap myBitmap = QRCode.from("MISTIC TFM Invoices by jherranzm@uoc.edu").bitmap();
        ImageView myImage = findViewById(R.id.imageView);
        myImage.setImageBitmap(myBitmap);

        Thread logoTimer = new Thread() {
            public void run() {
                try {

                    TFMSecurityManager tfmSecurityManager = TFMSecurityManager.getInstance(SplashActivity.this);

                    sleep(1000);
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
