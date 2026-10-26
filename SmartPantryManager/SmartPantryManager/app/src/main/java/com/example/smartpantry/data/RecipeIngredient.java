package com.example.smartpantry.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.smartpantry.matching.IngredientLike;

/** One ingredient line of a recipe. Deleted automatically when its recipe is deleted. */
@Entity(tableName = "recipe_ingredients",
        foreignKeys = @ForeignKey(entity = Recipe.class,
                parentColumns = "id", childColumns = "recipeId", onDelete = ForeignKey.CASCADE),
        indices = {@Index("recipeId")})
public class RecipeIngredient implements IngredientLike {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long recipeId;
    public String name;
    public double quantity;
    public String unit;

    public RecipeIngredient() {
    }

    @Ignore
    public RecipeIngredient(long recipeId, String name, double quantity, String unit) {
        this.recipeId = recipeId;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    @Override public String getName() { return name; }
    @Override public double getQuantity() { return quantity; }
    @Override public String getUnit() { return unit; }
}
