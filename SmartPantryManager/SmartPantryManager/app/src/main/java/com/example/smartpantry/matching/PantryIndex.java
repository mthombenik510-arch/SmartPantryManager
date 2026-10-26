package com.example.smartpantry.matching;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A pre-computed view of the pantry: for every normalised ingredient name, the total amount
 * on hand in each measurement dimension (grams, millilitres, pieces, or an unknown custom unit).
 *
 * Several pantry rows for the same ingredient (e.g. two bags of rice) are added together.
 */
public final class PantryIndex {

    /** ingredient key -> (dimension key -> amount in base units). */
    private final Map<String, Map<String, Double>> stock = new HashMap<>();

    private PantryIndex() {
    }

    public static PantryIndex from(List<? extends IngredientLike> pantry) {
        PantryIndex index = new PantryIndex();
        if (pantry == null) {
            return index;
        }
        for (IngredientLike item : pantry) {
            String key = IngredientNormalizer.normalize(item.getName());
            if (key.isEmpty() || item.getQuantity() <= 0) {
                continue;
            }
            String dim = dimensionKey(item.getUnit());
            double base = UnitConverter.toBase(item.getQuantity(), item.getUnit());
            Map<String, Double> perDim = index.stock.get(key);
            if (perDim == null) {
                perDim = new HashMap<>();
                index.stock.put(key, perDim);
            }
            Double current = perDim.get(dim);
            perDim.put(dim, (current == null ? 0.0 : current) + base);
        }
        return index;
    }

    /** True if the pantry holds any positive amount of this ingredient, in any unit. */
    public boolean has(String ingredientKey) {
        return stock.containsKey(ingredientKey);
    }

    /** Amount on hand in the same dimension as {@code unit}, in base units. 0 if none. */
    public double amountOf(String ingredientKey, String unit) {
        Map<String, Double> perDim = stock.get(ingredientKey);
        if (perDim == null) {
            return 0.0;
        }
        Double v = perDim.get(dimensionKey(unit));
        return v == null ? 0.0 : v;
    }

    static String dimensionKey(String unit) {
        UnitConverter.Dimension d = UnitConverter.dimensionOf(unit);
        if (d != null) {
            return d.name();
        }
        return "unit:" + UnitConverter.canonicalUnit(unit);
    }
}
