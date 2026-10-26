package com.example.smartpantry.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.example.smartpantry.matching.MatchResult;
import com.example.smartpantry.matching.PantryIndex;
import com.example.smartpantry.matching.RecipeMatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Single entry point to the database. All disk work happens on a background thread and results
 * are posted back to the main thread, so the UI never blocks.
 */
public class Repository {

    private static volatile Repository instance;

    private final AppDatabase db;
    private final PantryDao pantryDao;
    private final RecipeDao recipeDao;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    private Repository(Context context) {
        db = AppDatabase.getInstance(context);
        pantryDao = db.pantryDao();
        recipeDao = db.recipeDao();
    }

    public static Repository get(Context context) {
        if (instance == null) {
            synchronized (Repository.class) {
                if (instance == null) {
                    instance = new Repository(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    /** Pre-loads the recipe collection on first run (also called from the app class). */
    public void ensureSeeded() {
        io.execute(this::seedIfEmpty);
    }

    // ------------------------------------------------------------------ pantry (CRUD)

    public LiveData<List<PantryItem>> observePantry() {
        return pantryDao.observeAll();
    }

    /** Create or update, depending on whether the item already has an id. */
    public void savePantryItem(PantryItem item, Runnable onDone) {
        io.execute(() -> {
            if (item.id == 0) {
                pantryDao.insert(item);
            } else {
                pantryDao.update(item);
            }
            if (onDone != null) {
                main.post(onDone);
            }
        });
    }

    public void deletePantryItem(PantryItem item, Runnable onDone) {
        io.execute(() -> {
            pantryDao.delete(item);
            if (onDone != null) {
                main.post(onDone);
            }
        });
    }

    public void loadPantryItem(long id, Callback<PantryItem> callback) {
        io.execute(() -> {
            PantryItem item = pantryDao.getById(id);
            main.post(() -> callback.onResult(item));
        });
    }

    // ------------------------------------------------------------------ recipes + matching

    /** Runs the strict-matching rule against the current pantry for every recipe. */
    public void loadSuggestions(Callback<Suggestions> callback) {
        io.execute(() -> {
            seedIfEmpty();
            List<PantryItem> pantry = pantryDao.getAll();
            PantryIndex index = PantryIndex.from(pantry);

            Map<Long, List<RecipeIngredient>> byRecipe = new HashMap<>();
            for (RecipeIngredient ri : recipeDao.getAllIngredients()) {
                List<RecipeIngredient> list = byRecipe.get(ri.recipeId);
                if (list == null) {
                    list = new ArrayList<>();
                    byRecipe.put(ri.recipeId, list);
                }
                list.add(ri);
            }

            List<RecipeMatch> cookable = new ArrayList<>();
            List<RecipeMatch> almost = new ArrayList<>();
            for (Recipe recipe : recipeDao.getAll()) {
                List<RecipeIngredient> needs = byRecipe.get(recipe.id);
                MatchResult result = RecipeMatcher.evaluate(index, needs);
                if (result.canCook()) {
                    cookable.add(new RecipeMatch(recipe, result));
                } else if (result.isAlmostThere()) {
                    almost.add(new RecipeMatch(recipe, result));
                }
            }
            Suggestions s = new Suggestions(cookable, almost, pantry.size());
            main.post(() -> callback.onResult(s));
        });
    }

    public void loadRecipeDetail(long recipeId, Callback<RecipeDetail> callback) {
        io.execute(() -> {
            seedIfEmpty();
            Recipe recipe = recipeDao.getById(recipeId);
            if (recipe == null) {
                main.post(() -> callback.onResult(null));
                return;
            }
            List<RecipeIngredient> needs = recipeDao.getIngredientsFor(recipeId);
            MatchResult match = RecipeMatcher.evaluate(pantryDao.getAll(), needs);
            RecipeDetail detail = new RecipeDetail(recipe, needs, match);
            main.post(() -> callback.onResult(detail));
        });
    }

    // ------------------------------------------------------------------ seeding

    /** Runs on the io thread only (it is single-threaded, so there is no race). */
    private void seedIfEmpty() {
        if (recipeDao.count() > 0) {
            return;
        }
        // One transaction: either the whole collection is stored or nothing is.
        db.runInTransaction(this::insertSeedRecipes);
    }

    private void insertSeedRecipes() {
        for (SeedData.SeedRecipe seed : SeedData.recipes()) {
            long id = recipeDao.insertRecipe(
                    new Recipe(seed.name, seed.description, seed.steps, seed.minutes, seed.servings));
            List<RecipeIngredient> rows = new ArrayList<>();
            for (SeedData.Ing ing : seed.ingredients) {
                rows.add(new RecipeIngredient(id, ing.name, ing.quantity, ing.unit));
            }
            recipeDao.insertIngredients(rows);
        }
    }
}
