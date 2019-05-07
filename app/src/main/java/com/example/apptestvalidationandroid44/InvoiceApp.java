package com.example.apptestvalidationandroid44;

import android.app.Application;
import android.content.Context;

import com.example.apptestvalidationandroid44.util.TFMSecurityManager;

import java.io.File;

public class InvoiceApp extends Application {

    private static Context mContext;

    public static File getAppDir() {
        return mContext.getFilesDir();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        TFMSecurityManager tfmSecurityManager = TFMSecurityManager.getInstance();
    }

    public static Context getContext(){
        return mContext;
    }
}