package com.example.smartpantry.data;

import com.example.smartpantry.matching.MatchResult;

import java.util.List;

/** Everything the Recipe Detail screen needs. */
public class RecipeDetail {
    public final Recipe recipe;
    public final List<RecipeIngredient> ingredients;
    /** Per-ingredient status (same order as {@link #ingredients}). */
    public final MatchResult match;

    public RecipeDetail(Recipe recipe, List<RecipeIngredient> ingredients, MatchResult match) {
        this.recipe = recipe;
        this.ingredients = ingredients;
        this.match = match;
    }
}
