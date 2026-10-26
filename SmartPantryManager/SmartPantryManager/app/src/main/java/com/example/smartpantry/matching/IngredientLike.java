package com.example.smartpantry.matching;

/**
 * Anything that has a name, a quantity and a unit (a pantry item or a recipe ingredient).
 * The matching engine only depends on this interface so it has no Android dependencies
 * and can be unit-tested on the plain JVM.
 */
public interface IngredientLike {
    String getName();

    double getQuantity();

    String getUnit();
}
