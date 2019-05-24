package edu.uoc.mistic.tfm.jherranzm.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class LocalSymKey implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "user")
    private String user;

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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
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
        sb.append(", user='").append(user).append('\'');
        sb.append(", f='").append(f).append('\'');
        sb.append(", k='").append(k).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
