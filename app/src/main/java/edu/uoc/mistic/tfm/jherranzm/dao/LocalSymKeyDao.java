package edu.uoc.mistic.tfm.jherranzm.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import edu.uoc.mistic.tfm.jherranzm.model.LocalSymKey;

@Dao
public interface LocalSymKeyDao {

    @Query("SELECT * FROM LocalSymKey")
    List<LocalSymKey> getAll();

    @Query("SELECT * FROM LocalSymKey WHERE f = :theF LIMIT 1")
    LocalSymKey findLocalSimKeyByF(String theF);

    @Insert
    long insert(LocalSymKey localSimKey);

    @Delete
    void delete(LocalSymKey localSimKey);

    @Update
    void update(LocalSymKey localSimKey);

    @Query("DELETE FROM LocalSymKey WHERE user = :user")
    void deleteAllByUser(String user);
}

