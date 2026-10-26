package com.example.smartpantry.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RecipeDao {

    @Query("SELECT COUNT(*) FROM recipes")
    int count();

    @Query("SELECT * FROM recipes ORDER BY name COLLATE NOCASE ASC")
    List<Recipe> getAll();

    @Query("SELECT * FROM recipes WHERE id = :id LIMIT 1")
    Recipe getById(long id);

    @Query("SELECT * FROM recipe_ingredients")
    List<RecipeIngredient> getAllIngredients();

    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId ORDER BY id ASC")
    List<RecipeIngredient> getIngredientsFor(long recipeId);

    @Insert
    long insertRecipe(Recipe recipe);

    @Insert
    void insertIngredients(List<RecipeIngredient> ingredients);
}
