package com.example.smartpantry.data;

import java.util.List;

/**
 * Output of the matcher for the whole recipe collection.
 *
 * {@link #cookable} is the strict list (every ingredient present in enough quantity).
 * {@link #almostThere} is the optional bonus list (exactly one ingredient short) and is kept
 * completely separate so it can never leak into the strict suggestions.
 */
public class Suggestions {
    public final List<RecipeMatch> cookable;
    public final List<RecipeMatch> almostThere;
    public final int pantrySize;

    public Suggestions(List<RecipeMatch> cookable, List<RecipeMatch> almostThere, int pantrySize) {
        this.cookable = cookable;
        this.almostThere = almostThere;
        this.pantrySize = pantrySize;
    }
}
