package com.example.smartpantry.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/** The on-device SQLite database (via Room). Data persists across app restarts. */
@Database(entities = {PantryItem.class, Recipe.class, RecipeIngredient.class},
        version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract PantryDao pantryDao();

    public abstract RecipeDao recipeDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "smart_pantry.db").build();
                }
            }
        }
        return instance;
    }
}
