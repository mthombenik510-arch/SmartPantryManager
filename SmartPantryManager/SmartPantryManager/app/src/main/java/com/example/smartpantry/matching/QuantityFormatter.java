package com.example.smartpantry.matching;

import java.util.Locale;

/** Formats quantities for display: 2.0 -> "2", 0.5 -> "0.5". */
public final class QuantityFormatter {

    private QuantityFormatter() {
    }

    public static String number(double q) {
        if (Math.abs(q - Math.rint(q)) < 1e-9) {
            return String.format(Locale.US, "%d", (long) Math.rint(q));
        }
        String s = String.format(Locale.US, "%.2f", q);
        if (s.contains(".")) {
            s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return s;
    }

    /** "200 g", "2 pcs", or just the unit text for presence-only units ("to taste"). */
    public static String format(double q, String unit) {
        if (UnitConverter.isPresenceOnly(unit)) {
            return unit.trim();
        }
        String u = unit == null ? "" : unit.trim();
        return u.isEmpty() ? number(q) : number(q) + " " + u;
    }
}
