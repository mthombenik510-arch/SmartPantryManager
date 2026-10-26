package com.example.smartpantry.data;

import com.example.smartpantry.matching.MatchResult;

/** A recipe together with how well the pantry covers it. */
public class RecipeMatch {
    public final Recipe recipe;
    public final MatchResult result;

    public RecipeMatch(Recipe recipe, MatchResult result) {
        this.recipe = recipe;
        this.result = result;
    }
}
