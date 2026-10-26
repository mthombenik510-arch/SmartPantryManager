package com.example.smartpantry;

import android.app.Application;

import com.example.smartpantry.data.Repository;

/** Opens the database and pre-loads the recipe collection as soon as the app starts. */
public class PantryApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Repository.get(this).ensureSeeded();
    }
}
