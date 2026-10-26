package com.example.smartpantry.matching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for the strict-matching rule (assignment section 2.3).
 * Run with: ./gradlew testDebugUnitTest
 */
public class RecipeMatcherTest {

    /** Minimal IngredientLike used only by the tests. */
    private static class Item implements IngredientLike {
        final String name;
        final double qty;
        final String unit;

        Item(String name, double qty, String unit) {
            this.name = name;
            this.qty = qty;
            this.unit = unit;
        }

        @Override public String getName() { return name; }
        @Override public double getQuantity() { return qty; }
        @Override public String getUnit() { return unit; }
    }

    private static Item i(String name, double qty, String unit) {
        return new Item(name, qty, unit);
    }

    private static List<Item> list(Item... items) {
        return new ArrayList<>(Arrays.asList(items));
    }

    // ---- The headline rule: 5 needed, 4 in pantry -> NOT suggested -------------------------

    @Test
    public void fiveNeededFourOwned_isNotCookable() {
        List<Item> recipe = list(i("rice", 200, "g"), i("onion", 1, "pcs"), i("egg", 2, "pcs"),
                i("oil", 2, "tbsp"), i("soy sauce", 2, "tbsp"));
        List<Item> pantry = list(i("rice", 500, "g"), i("onion", 3, "pcs"), i("egg", 6, "pcs"),
                i("oil", 500, "ml"));
        MatchResult r = RecipeMatcher.evaluate(pantry, recipe);
        assertFalse(r.canCook());
        assertEquals(1, r.getMissingCount());
        assertTrue(r.isAlmostThere());
        assertEquals("soy sauce", r.getMissingLines().get(0).name);
    }

    @Test
    public void fiveNeededFiveOwned_isCookable() {
        List<Item> recipe = list(i("rice", 200, "g"), i("onion", 1, "pcs"), i("egg", 2, "pcs"),
                i("oil", 2, "tbsp"), i("soy sauce", 2, "tbsp"));
        List<Item> pantry = list(i("rice", 500, "g"), i("onion", 3, "pcs"), i("egg", 6, "pcs"),
                i("oil", 500, "ml"), i("soy sauce", 250, "ml"));
        assertTrue(RecipeMatcher.evaluate(pantry, recipe).canCook());
    }

    @Test
    public void twoMissing_isNotCookableAndNotAlmostThere() {
        List<Item> recipe = list(i("rice", 200, "g"), i("onion", 1, "pcs"), i("egg", 2, "pcs"));
        List<Item> pantry = list(i("rice", 500, "g"));
        MatchResult r = RecipeMatcher.evaluate(pantry, recipe);
        assertFalse(r.canCook());
        assertFalse(r.isAlmostThere());
        assertEquals(2, r.getMissingCount());
    }

    @Test
    public void emptyPantry_matchesNothing() {
        List<Item> recipe = list(i("rice", 200, "g"));
        assertFalse(RecipeMatcher.evaluate(new ArrayList<Item>(), recipe).canCook());
    }

    @Test
    public void recipeWithNoIngredients_isNeverCookable() {
        assertFalse(RecipeMatcher.evaluate(list(i("rice", 1, "kg")), new ArrayList<Item>()).canCook());
    }

    // ---- Quantity ---------------------------------------------------------------------------

    @Test
    public void insufficientQuantity_isNotCookable() {
        MatchResult r = RecipeMatcher.evaluate(list(i("rice", 150, "g")), list(i("rice", 200, "g")));
        assertFalse(r.canCook());
        assertEquals(MatchResult.Status.INSUFFICIENT, r.getLines().get(0).status);
        assertEquals(150.0, r.getLines().get(0).haveQuantity, 1e-6);
    }

    @Test
    public void exactQuantity_isCookable() {
        assertTrue(RecipeMatcher.evaluate(list(i("rice", 200, "g")), list(i("rice", 200, "g"))).canCook());
    }

    @Test
    public void duplicatePantryRows_areSummed() {
        List<Item> pantry = list(i("Rice", 120, "g"), i("rice", 100, "g"));
        assertTrue(RecipeMatcher.evaluate(pantry, list(i("rice", 200, "g"))).canCook());
    }

    // ---- Singular / plural / case / whitespace ----------------------------------------------

    @Test
    public void tomatoVsTomatoes_matches() {
        assertTrue(RecipeMatcher.evaluate(list(i("Tomatoes", 4, "pcs")), list(i("tomato", 3, "pcs"))).canCook());
        assertTrue(RecipeMatcher.evaluate(list(i("tomato", 4, "pcs")), list(i("tomatoes", 3, "pcs"))).canCook());
    }

    @Test
    public void pluralsAndCase_matchAcrossTheBoard() {
        assertEquals(IngredientNormalizer.normalize("Potatoes"), IngredientNormalizer.normalize("potato"));
        assertEquals(IngredientNormalizer.normalize("Eggs"), IngredientNormalizer.normalize("egg"));
        assertEquals(IngredientNormalizer.normalize("Cherries"), IngredientNormalizer.normalize("cherry"));
        assertEquals(IngredientNormalizer.normalize("Onions"), IngredientNormalizer.normalize("  ONION "));
        assertEquals(IngredientNormalizer.normalize("olives"), IngredientNormalizer.normalize("olive"));
        assertEquals(IngredientNormalizer.normalize("peaches"), IngredientNormalizer.normalize("peach"));
        assertEquals(IngredientNormalizer.normalize("bay leaves"), IngredientNormalizer.normalize("bay leaf"));
    }

    @Test
    public void descriptorsAndSynonyms_areIgnored() {
        assertEquals(IngredientNormalizer.normalize("fresh chopped onions"), IngredientNormalizer.normalize("onion"));
        assertEquals(IngredientNormalizer.normalize("Capsicum"), IngredientNormalizer.normalize("bell pepper"));
        assertEquals(IngredientNormalizer.normalize("mielie meal"), IngredientNormalizer.normalize("maize meal"));
    }

    @Test
    public void differentIngredients_doNotMatch() {
        assertFalse(RecipeMatcher.evaluate(list(i("peanut butter", 500, "g")), list(i("butter", 50, "g"))).canCook());
        assertFalse(RecipeMatcher.evaluate(list(i("sweet potato", 4, "pcs")), list(i("potato", 2, "pcs"))).canCook());
    }

    // ---- Units ------------------------------------------------------------------------------

    @Test
    public void kilogramsSatisfyGrams() {
        assertTrue(RecipeMatcher.evaluate(list(i("flour", 1, "kg")), list(i("flour", 500, "g"))).canCook());
        assertFalse(RecipeMatcher.evaluate(list(i("flour", 0.4, "kg")), list(i("flour", 500, "g"))).canCook());
    }

    @Test
    public void litresCupsAndSpoons_convertWithinVolume() {
        assertTrue(RecipeMatcher.evaluate(list(i("milk", 1, "l")), list(i("milk", 2, "cup"))).canCook());
        assertTrue(RecipeMatcher.evaluate(list(i("oil", 100, "ml")), list(i("oil", 2, "tbsp"))).canCook());
        assertFalse(RecipeMatcher.evaluate(list(i("oil", 20, "ml")), list(i("oil", 2, "tbsp"))).canCook());
    }

    @Test
    public void unitAliases_areUnderstood() {
        assertTrue(RecipeMatcher.evaluate(list(i("sugar", 1, "Kilogram")), list(i("sugar", 100, "grams"))).canCook());
    }

    @Test
    public void incompatibleDimensions_areNotGuessed() {
        // 2 onions vs a recipe that wants 200 g onion: we cannot know, so strict rule says no.
        assertFalse(RecipeMatcher.evaluate(list(i("onion", 2, "pcs")), list(i("onion", 200, "g"))).canCook());
    }

    // ---- "to taste" -------------------------------------------------------------------------

    @Test
    public void toTasteIngredient_needsOnlyPresence() {
        assertTrue(RecipeMatcher.evaluate(list(i("salt", 1, "g")), list(i("salt", 0, "to taste"))).canCook());
        assertFalse(RecipeMatcher.evaluate(list(i("pepper", 1, "g")), list(i("salt", 0, "to taste"))).canCook());
    }
}
