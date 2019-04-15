package com.example.apptestvalidationandroid44.model;

import android.os.Parcel;
import android.os.Parcelable;

public class FileDataObject implements Parcelable {

    private String fileName;

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public FileDataObject createFromParcel(Parcel in) {
            return new FileDataObject(in);
        }

        public FileDataObject[] newArray(int size) {
            return new FileDataObject[size];
        }
    };

    public FileDataObject(String fileName) {
        this.fileName = fileName;
    }

    private FileDataObject(Parcel in){
        this.fileName = in.readString();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
