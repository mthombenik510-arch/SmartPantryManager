package com.example.smartpantry.data;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.example.smartpantry.matching.IngredientLike;

/** One row of the user's pantry. */
@Entity(tableName = "pantry_items")
public class PantryItem implements IngredientLike {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public double quantity;
    public String unit;

    /** Optional expiry date as yyyy-MM-dd, or null when not set. */
    @Nullable
    public String expiryDate;

    public PantryItem() {
    }

    @Ignore
    public PantryItem(String name, double quantity, String unit, @Nullable String expiryDate) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    @Override public String getName() { return name; }
    @Override public double getQuantity() { return quantity; }
    @Override public String getUnit() { return unit; }
}
