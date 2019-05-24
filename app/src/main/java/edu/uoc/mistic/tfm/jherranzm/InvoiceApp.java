package edu.uoc.mistic.tfm.jherranzm;

import android.app.Application;
import android.content.Context;

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

    }

    public static Context getContext(){
        return mContext;
    }
}