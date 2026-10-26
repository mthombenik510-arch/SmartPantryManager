package com.example.smartpantry.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PantryDao {

    /** Live list for the Pantry screen; Room re-emits whenever the table changes. */
    @Query("SELECT * FROM pantry_items ORDER BY name COLLATE NOCASE ASC")
    LiveData<List<PantryItem>> observeAll();

    @Query("SELECT * FROM pantry_items")
    List<PantryItem> getAll();

    @Query("SELECT * FROM pantry_items WHERE id = :id LIMIT 1")
    PantryItem getById(long id);

    @Insert
    long insert(PantryItem item);

    @Update
    void update(PantryItem item);

    @Delete
    void delete(PantryItem item);
}
