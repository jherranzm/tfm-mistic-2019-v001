package com.example.apptestvalidationandroid44.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.apptestvalidationandroid44.model.FileDataObject;

import java.util.List;

@Dao
public interface FileDataObjectDao {

    @Query("SELECT * FROM FileDataObject")
    List<FileDataObject> getAll();

    @Query("SELECT * FROM FileDataObject WHERE fileName = :theFilename LIMIT 1")
    FileDataObject findByFilename(String theFilename);

    @Insert
    long insert(FileDataObject localSimKey);

    @Delete
    void delete(FileDataObject localSimKey);

    @Update
    void update(FileDataObject localSimKey);

    @Query("DELETE FROM FileDataObject WHERE user = :user")
    void deleteAllByUser(String user);

    @Query("SELECT * FROM FileDataObject WHERE user = :theUser")
    List<FileDataObject> findByUser(String theUser);
}

