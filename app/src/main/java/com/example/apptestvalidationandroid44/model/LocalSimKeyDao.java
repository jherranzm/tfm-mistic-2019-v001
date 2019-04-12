package com.example.apptestvalidationandroid44.model;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface LocalSimKeyDao {

    @Query("SELECT * FROM localsimkey")
    LiveData<List<LocalSimKey>> getAll();

    @Query("SELECT * FROM localsimkey WHERE f = :theF LIMIT 1")
    LiveData<LocalSimKey> findLocalSimKeyByF(String theF);

    @Insert
    long insert(LocalSimKey localSimKey);

    @Delete
    void delete(LocalSimKey localSimKey);

    @Update
    void update(LocalSimKey localSimKey);
}

