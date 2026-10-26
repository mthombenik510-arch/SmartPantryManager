package com.example.smartpantry.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/** A recipe in the pre-loaded collection. Its ingredients live in {@link RecipeIngredient}. */
@Entity(tableName = "recipes")
public class Recipe {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public String description;
    /** Preparation steps, one per line. */
    public String steps;
    public int minutes;
    public int servings;

    public Recipe() {
    }

    @Ignore
    public Recipe(String name, String description, String steps, int minutes, int servings) {
        this.name = name;
        this.description = description;
        this.steps = steps;
        this.minutes = minutes;
        this.servings = servings;
    }
}
