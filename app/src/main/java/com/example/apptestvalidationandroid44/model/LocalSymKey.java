package com.example.apptestvalidationandroid44.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class LocalSymKey implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "f")
    private String f;

    @ColumnInfo(name = "k")
    private String k;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getF() {
        return f;
    }

    public void setF(String f) {
        this.f = f;
    }

    public String getK() {
        return k;
    }

    public void setK(String k) {
        this.k = k;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("LocalSymKey{");
        sb.append("id=").append(id);
        sb.append(", f='").append(f).append('\'');
        sb.append(", k='").append(k).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
