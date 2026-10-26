package com.example.smartpantry.matching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Outcome of checking one recipe against the pantry. */
public final class MatchResult {

    public enum Status { OK, INSUFFICIENT, MISSING }

    /** The result for one required ingredient. */
    public static final class Line {
        public final String name;
        public final double requiredQuantity;
        public final String unit;
        public final Status status;
        /** What the pantry holds, in the recipe's unit (0 if none in a compatible unit). */
        public final double haveQuantity;

        Line(String name, double requiredQuantity, String unit, Status status, double haveQuantity) {
            this.name = name;
            this.requiredQuantity = requiredQuantity;
            this.unit = unit;
            this.status = status;
            this.haveQuantity = haveQuantity;
        }

        public boolean isSatisfied() {
            return status == Status.OK;
        }

        /** Human-readable description of what is missing, e.g. "Rice: need 200 g, have 150 g". */
        public String describeShortfall() {
            if (status == Status.OK) {
                return "";
            }
            if (status == Status.MISSING) {
                return name + ": not in pantry";
            }
            return name + ": need " + QuantityFormatter.format(requiredQuantity, unit)
                    + ", have " + QuantityFormatter.format(haveQuantity, unit);
        }
    }

    private final List<Line> lines;

    MatchResult(List<Line> lines) {
        this.lines = Collections.unmodifiableList(new ArrayList<>(lines));
    }

    public List<Line> getLines() {
        return lines;
    }

    public int getMissingCount() {
        int n = 0;
        for (Line l : lines) {
            if (!l.isSatisfied()) {
                n++;
            }
        }
        return n;
    }

    public List<Line> getMissingLines() {
        List<Line> out = new ArrayList<>();
        for (Line l : lines) {
            if (!l.isSatisfied()) {
                out.add(l);
            }
        }
        return out;
    }

    /**
     * The strict rule: true only if the recipe has ingredients and EVERY one is satisfied.
     * A recipe with zero listed ingredients is never "cookable" (avoids a vacuous truth).
     */
    public boolean canCook() {
        return !lines.isEmpty() && getMissingCount() == 0;
    }

    /** Optional stretch feature: exactly one ingredient short. Never part of the strict list. */
    public boolean isAlmostThere() {
        return !lines.isEmpty() && getMissingCount() == 1;
    }
}
