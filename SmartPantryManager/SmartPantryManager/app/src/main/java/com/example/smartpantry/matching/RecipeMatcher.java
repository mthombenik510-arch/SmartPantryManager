package com.example.smartpantry.matching;

import java.util.ArrayList;
import java.util.List;

/**
 * THE strict-matching rule (assignment section 2.3).
 *
 * A recipe is cookable only if EVERY required ingredient is present in the pantry in at least
 * the required quantity. One missing or insufficient ingredient means the recipe is NOT cookable.
 *
 * Robustness against real-world messiness:
 *  - names are normalised (case, whitespace, plurals, descriptors, synonyms)
 *  - units are converted within a dimension (kg/g, l/ml, cup/tbsp/tsp, ...)
 *  - several pantry rows of the same ingredient are summed
 *  - "to taste"/"pinch" ingredients only need to be present in some quantity
 */
public final class RecipeMatcher {

    /** Tolerance for floating point rounding, e.g. 3 x 0.1 vs 0.3. */
    private static final double EPSILON = 1e-6;

    private RecipeMatcher() {
    }

    /** Convenience overload that indexes the pantry on every call. */
    public static MatchResult evaluate(List<? extends IngredientLike> pantry,
                                       List<? extends IngredientLike> required) {
        return evaluate(PantryIndex.from(pantry), required);
    }

    public static MatchResult evaluate(PantryIndex pantry, List<? extends IngredientLike> required) {
        List<MatchResult.Line> lines = new ArrayList<>();
        if (required != null) {
            for (IngredientLike need : required) {
                lines.add(checkOne(pantry, need));
            }
        }
        return new MatchResult(lines);
    }

    private static MatchResult.Line checkOne(PantryIndex pantry, IngredientLike need) {
        String key = IngredientNormalizer.normalize(need.getName());
        String name = need.getName();
        String unit = need.getUnit();
        double qty = need.getQuantity();

        if (key.isEmpty() || !pantry.has(key)) {
            return new MatchResult.Line(name, qty, unit, MatchResult.Status.MISSING, 0);
        }

        // "to taste" / "pinch" / zero quantity: having any of it is enough.
        if (UnitConverter.isPresenceOnly(unit) || qty <= 0) {
            return new MatchResult.Line(name, qty, unit, MatchResult.Status.OK, 0);
        }

        double requiredBase = UnitConverter.toBase(qty, unit);
        double haveBase = pantry.amountOf(key, unit);

        // Convert what we have back into the recipe's own unit for friendly messages.
        double factor = requiredBase / qty;
        double haveInRecipeUnit = factor == 0 ? 0 : haveBase / factor;

        if (haveBase + EPSILON >= requiredBase) {
            return new MatchResult.Line(name, qty, unit, MatchResult.Status.OK, haveInRecipeUnit);
        }
        return new MatchResult.Line(name, qty, unit, MatchResult.Status.INSUFFICIENT, haveInRecipeUnit);
    }
}
