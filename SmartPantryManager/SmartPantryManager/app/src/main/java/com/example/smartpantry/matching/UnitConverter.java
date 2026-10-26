package com.example.smartpantry.matching;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Converts quantities into a base unit per "dimension" so that 1 kg can satisfy a recipe
 * that asks for 500 g, or 2 cups can satisfy 400 ml.
 *
 *  MASS   -> grams
 *  VOLUME -> millilitres
 *  COUNT  -> pieces
 *
 * Quantities in different dimensions are deliberately NOT converted (we cannot know how many
 * grams one onion weighs), so the strict rule treats them as "not enough".
 */
public final class UnitConverter {

    public enum Dimension { MASS, VOLUME, COUNT }

    /** The units offered in the Add/Edit spinner. */
    public static final String[] DISPLAY_UNITS = {
            "g", "kg", "ml", "l", "tsp", "tbsp", "cup", "pcs"
    };

    private static final Map<String, Dimension> DIMENSIONS = new HashMap<>();
    private static final Map<String, Double> FACTORS = new HashMap<>();
    private static final Map<String, String> ALIASES = new HashMap<>();

    static {
        add("g", Dimension.MASS, 1.0);
        add("kg", Dimension.MASS, 1000.0);
        add("mg", Dimension.MASS, 0.001);
        add("oz", Dimension.MASS, 28.3495);
        add("lb", Dimension.MASS, 453.592);

        add("ml", Dimension.VOLUME, 1.0);
        add("l", Dimension.VOLUME, 1000.0);
        add("tsp", Dimension.VOLUME, 5.0);
        add("tbsp", Dimension.VOLUME, 15.0);
        add("cup", Dimension.VOLUME, 240.0);

        add("pcs", Dimension.COUNT, 1.0);

        alias("gram", "g");
        alias("grams", "g");
        alias("gr", "g");
        alias("kilogram", "kg");
        alias("kilograms", "kg");
        alias("kgs", "kg");
        alias("milligram", "mg");
        alias("ounce", "oz");
        alias("ounces", "oz");
        alias("pound", "lb");
        alias("pounds", "lb");
        alias("lbs", "lb");

        alias("millilitre", "ml");
        alias("milliliter", "ml");
        alias("millilitres", "ml");
        alias("milliliters", "ml");
        alias("mls", "ml");
        alias("litre", "l");
        alias("liter", "l");
        alias("litres", "l");
        alias("liters", "l");
        alias("lt", "l");
        alias("teaspoon", "tsp");
        alias("teaspoons", "tsp");
        alias("tablespoon", "tbsp");
        alias("tablespoons", "tbsp");
        alias("tbs", "tbsp");
        alias("cups", "cup");

        alias("pc", "pcs");
        alias("piece", "pcs");
        alias("pieces", "pcs");
        alias("whole", "pcs");
        alias("unit", "pcs");
        alias("units", "pcs");
        alias("each", "pcs");
        alias("x", "pcs");
        alias("clove", "pcs");
        alias("cloves", "pcs");
        alias("slice", "pcs");
        alias("slices", "pcs");
        alias("can", "pcs");
        alias("cans", "pcs");
        alias("tin", "pcs");
        alias("tins", "pcs");
        alias("pack", "pcs");
        alias("packs", "pcs");
        alias("bunch", "pcs");
        alias("loaf", "pcs");
    }

    private UnitConverter() {
    }

    private static void add(String unit, Dimension d, double factor) {
        DIMENSIONS.put(unit, d);
        FACTORS.put(unit, factor);
    }

    private static void alias(String alias, String canonical) {
        ALIASES.put(alias, canonical);
    }

    /** Lower-cases, trims and maps aliases ("Grams" -> "g"). Unknown units are returned as typed. */
    public static String canonicalUnit(String unit) {
        if (unit == null) {
            return "";
        }
        String u = unit.trim().toLowerCase(Locale.ROOT);
        if (u.endsWith(".")) {
            u = u.substring(0, u.length() - 1);
        }
        String mapped = ALIASES.get(u);
        return mapped != null ? mapped : u;
    }

    /** True when the unit means "no measurable amount" (pinch, to taste ...). */
    public static boolean isPresenceOnly(String unit) {
        String u = unit == null ? "" : unit.trim().toLowerCase(Locale.ROOT);
        return u.equals("to taste") || u.equals("pinch") || u.equals("dash") || u.equals("as needed");
    }

    /** Returns the dimension of a unit, or null if the unit is unknown. */
    public static Dimension dimensionOf(String unit) {
        return DIMENSIONS.get(canonicalUnit(unit));
    }

    /** Converts a quantity into the base unit of its dimension (g, ml or pcs). */
    public static double toBase(double quantity, String unit) {
        Double f = FACTORS.get(canonicalUnit(unit));
        return f == null ? quantity : quantity * f;
    }
}
