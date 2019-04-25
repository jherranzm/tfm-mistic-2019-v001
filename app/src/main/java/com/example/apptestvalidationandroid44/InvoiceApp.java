package com.example.apptestvalidationandroid44;

import android.app.Application;
import android.content.Context;

public class InvoiceApp  extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext(){
        return mContext;
    }
}