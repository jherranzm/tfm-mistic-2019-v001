package edu.uoc.mistic.tfm.jherranzm.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class FileDataObject implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "user")
    private String user;

    @ColumnInfo(name = "fileName")
    private String fileName;

    @ColumnInfo(name = "status")
    private int status;


    public FileDataObject(String fileName, String user, int status) {

        this.fileName = fileName;
        this.user = user;
        this.status = status;
    }

    public FileDataObject() {
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
