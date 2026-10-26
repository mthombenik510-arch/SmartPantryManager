package com.example.smartpantry.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.smartpantry.matching.IngredientLike;
import com.example.smartpantry.matching.IngredientNormalizer;
import com.example.smartpantry.matching.MatchResult;
import com.example.smartpantry.matching.RecipeMatcher;
import com.example.smartpantry.matching.UnitConverter;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Sanity checks on the pre-loaded recipe collection. */
public class SeedDataTest {

    private static class Stock implements IngredientLike {
        final String name;
        final double qty;
        final String unit;

        Stock(String name, double qty, String unit) {
            this.name = name;
            this.qty = qty;
            this.unit = unit;
        }

        @Override public String getName() { return name; }
        @Override public double getQuantity() { return qty; }
        @Override public String getUnit() { return unit; }
    }

    @Test
    public void hasAtLeastFifteenRecipes_withUniqueNames() {
        List<SeedData.SeedRecipe> recipes = SeedData.recipes();
        assertTrue(recipes.size() >= 15);
        Set<String> names = new HashSet<>();
        for (SeedData.SeedRecipe r : recipes) {
            assertTrue("duplicate name " + r.name, names.add(r.name));
        }
    }

    @Test
    public void everyRecipe_hasIngredientsStepsAndValidUnits() {
        for (SeedData.SeedRecipe r : SeedData.recipes()) {
            assertTrue(r.name + " needs ingredients", r.ingredients.size() >= 3);
            assertTrue(r.name + " needs steps", r.steps.trim().length() > 20);
            for (SeedData.Ing i : r.ingredients) {
                assertTrue(r.name + ": " + i.name, !IngredientNormalizer.normalize(i.name).isEmpty());
                assertTrue(r.name + ": bad unit " + i.unit,
                        UnitConverter.isPresenceOnly(i.unit) || UnitConverter.dimensionOf(i.unit) != null);
            }
        }
    }

    @Test
    public void fullyStockedPantry_cooksEveryRecipe() {
        List<Stock> pantry = new ArrayList<>();
        for (SeedData.SeedRecipe r : SeedData.recipes()) {
            for (SeedData.Ing i : r.ingredients) {
                pantry.add(new Stock(i.name + "s", 100000, UnitConverter.isPresenceOnly(i.unit) ? "g"
                        : (UnitConverter.dimensionOf(i.unit) == UnitConverter.Dimension.MASS ? "kg"
                        : UnitConverter.dimensionOf(i.unit) == UnitConverter.Dimension.VOLUME ? "l" : "pcs")));
            }
        }
        for (SeedData.SeedRecipe r : SeedData.recipes()) {
            assertTrue(r.name, RecipeMatcher.evaluate(pantry, r.ingredients).canCook());
        }
    }

    @Test
    public void removingAnyOneIngredient_makesThatRecipeNotCookable() {
        for (SeedData.SeedRecipe r : SeedData.recipes()) {
            for (int skip = 0; skip < r.ingredients.size(); skip++) {
                List<Stock> pantry = new ArrayList<>();
                for (int k = 0; k < r.ingredients.size(); k++) {
                    if (k == skip) continue;
                    SeedData.Ing i = r.ingredients.get(k);
                    pantry.add(new Stock(i.name, Math.max(i.quantity, 1), UnitConverter.isPresenceOnly(i.unit) ? "g" : i.unit));
                }
                MatchResult res = RecipeMatcher.evaluate(pantry, r.ingredients);
                assertFalse(r.name + " without " + r.ingredients.get(skip).name, res.canCook());
                assertEquals(1, res.getMissingCount());
            }
        }
    }
}
