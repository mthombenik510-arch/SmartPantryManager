package com.example.smartpantry.data;

import com.example.smartpantry.matching.IngredientLike;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The 20 recipes that are pre-loaded on first run.
 *
 * This class is plain Java (no Android / Room imports) so it can be unit-tested on the JVM.
 * {@link RecipeRepository} converts these into Room entities.
 *
 * Plain water is assumed to always be available and is not listed as an ingredient.
 */
public final class SeedData {

    public static final class Ing implements IngredientLike {
        public final String name;
        public final double quantity;
        public final String unit;

        Ing(String name, double quantity, String unit) {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
        }

        @Override public String getName() { return name; }
        @Override public double getQuantity() { return quantity; }
        @Override public String getUnit() { return unit; }
    }

    public static final class SeedRecipe {
        public final String name;
        public final String description;
        public final int minutes;
        public final int servings;
        public final String steps;
        public final List<Ing> ingredients;

        SeedRecipe(String name, String description, int minutes, int servings, String steps, Ing... ingredients) {
            this.name = name;
            this.description = description;
            this.minutes = minutes;
            this.servings = servings;
            this.steps = steps;
            this.ingredients = Arrays.asList(ingredients);
        }
    }

    private SeedData() {
    }

    private static Ing ing(String name, double qty, String unit) {
        return new Ing(name, qty, unit);
    }

    /** Salt, pepper and similar: only need to be in the pantry, in any amount. */
    private static Ing taste(String name) {
        return new Ing(name, 0, "to taste");
    }

    public static List<SeedRecipe> recipes() {
        List<SeedRecipe> r = new ArrayList<>();

        r.add(new SeedRecipe("Scrambled Eggs on Toast", "A five-minute breakfast classic.", 10, 1,
                "Toast the bread.\nMelt the butter in a pan over low heat.\nBeat the eggs with a pinch of salt and pour into the pan.\nStir gently until just set, then spoon onto the toast.",
                ing("egg", 3, "pcs"), ing("bread", 2, "pcs"), ing("butter", 10, "g"), taste("salt")));

        r.add(new SeedRecipe("Pap and Tomato Relish", "Soft maize porridge with a tangy tomato and onion sauce.", 35, 4,
                "Bring 750 ml of water to the boil and stir in the maize meal until smooth. Cover and cook on low for 20 minutes, stirring now and then.\nFry the chopped onion in the oil until soft.\nAdd the chopped tomatoes and cook for 10 minutes until saucy. Season with salt.\nServe the relish over the pap.",
                ing("maize meal", 250, "g"), ing("tomato", 3, "pcs"), ing("onion", 1, "pcs"), ing("oil", 2, "tbsp"), taste("salt")));

        r.add(new SeedRecipe("Egg Fried Rice", "Quick fried rice from leftover cooked rice.", 20, 2,
                "Cook the rice and let it cool slightly.\nHeat the oil in a wok or large pan and scramble the eggs, then push to one side.\nAdd the rice and stir-fry for 3 minutes.\nStir in the soy sauce and chopped spring onion and serve hot.",
                ing("rice", 200, "g"), ing("egg", 2, "pcs"), ing("spring onion", 2, "pcs"), ing("oil", 1, "tbsp"), ing("soy sauce", 2, "tbsp")));

        r.add(new SeedRecipe("Tuna Pasta", "Pantry-staple pasta with tuna, garlic and onion.", 20, 2,
                "Boil the pasta in salted water until al dente, then drain.\nFry the chopped onion and garlic in the oil for 3 minutes.\nAdd the drained tuna and cook for 2 minutes.\nToss the pasta through the tuna mixture and season to taste.",
                ing("pasta", 200, "g"), ing("canned tuna", 1, "pcs"), ing("onion", 1, "pcs"), ing("garlic", 2, "pcs"), ing("oil", 1, "tbsp"), taste("salt")));

        r.add(new SeedRecipe("Classic Pancakes", "Thin, golden pancakes for sweet or savoury fillings.", 25, 4,
                "Whisk the flour, sugar, egg and milk into a smooth batter. Rest for 10 minutes.\nBrush a hot pan lightly with oil.\nPour in a thin layer of batter and cook until golden underneath, then flip.\nStack and serve with your favourite filling.",
                ing("flour", 200, "g"), ing("milk", 300, "ml"), ing("egg", 1, "pcs"), ing("sugar", 1, "tbsp"), ing("oil", 1, "tbsp")));

        r.add(new SeedRecipe("Cheese Omelette", "Fluffy omelette with melted cheese.", 10, 1,
                "Beat the eggs with a pinch of salt.\nMelt the butter in a non-stick pan.\nPour in the eggs and cook gently until nearly set.\nSprinkle on the grated cheese, fold the omelette in half and serve.",
                ing("egg", 3, "pcs"), ing("cheese", 50, "g"), ing("butter", 10, "g"), taste("salt")));

        r.add(new SeedRecipe("Creamy Tomato Soup", "Comforting homemade soup.", 35, 3,
                "Melt the butter and soften the chopped onion and garlic.\nAdd the roughly chopped tomatoes and 500 ml water. Simmer for 20 minutes.\nBlend until smooth.\nSeason with salt and serve with bread if you have it.",
                ing("tomato", 6, "pcs"), ing("onion", 1, "pcs"), ing("garlic", 2, "pcs"), ing("butter", 20, "g"), taste("salt")));

        r.add(new SeedRecipe("Spaghetti Bolognese", "Rich mince and tomato sauce over pasta.", 45, 3,
                "Fry the onion and garlic in the oil until soft.\nAdd the mince and brown it well.\nStir in the canned tomatoes and simmer for 25 minutes.\nCook the pasta, drain, and serve topped with the sauce.",
                ing("pasta", 250, "g"), ing("beef mince", 300, "g"), ing("canned tomato", 1, "pcs"), ing("onion", 1, "pcs"), ing("garlic", 2, "pcs"), ing("oil", 1, "tbsp")));

        r.add(new SeedRecipe("Chicken Rice Bowl", "Pan-fried chicken over fluffy rice.", 35, 2,
                "Cook the rice according to the packet.\nSlice the chicken breast and season with pepper.\nFry the onion and garlic in the oil, add the chicken and cook through, about 8 minutes.\nServe the chicken over the rice.",
                ing("chicken breast", 300, "g"), ing("rice", 200, "g"), ing("onion", 1, "pcs"), ing("garlic", 2, "pcs"), ing("oil", 1, "tbsp"), taste("black pepper")));

        r.add(new SeedRecipe("Vegetable Stir-fry", "Colourful veg tossed in soy sauce.", 20, 2,
                "Slice the carrots, bell pepper and onion thinly.\nHeat the oil in a wok and add the garlic.\nStir-fry the carrots first for 3 minutes, then add the pepper and onion for 3 more.\nSplash in the soy sauce and serve immediately.",
                ing("carrot", 2, "pcs"), ing("bell pepper", 1, "pcs"), ing("onion", 1, "pcs"), ing("garlic", 2, "pcs"), ing("soy sauce", 2, "tbsp"), ing("oil", 1, "tbsp")));

        r.add(new SeedRecipe("Bean Chili", "Hearty, spicy and budget friendly.", 40, 3,
                "Fry the onion, garlic and chopped chili in the oil.\nStir in the cumin for 30 seconds.\nAdd the canned tomatoes and drained beans.\nSimmer for 25 minutes, stirring occasionally, and serve hot.",
                ing("kidney bean", 1, "pcs"), ing("canned tomato", 1, "pcs"), ing("onion", 1, "pcs"), ing("chili", 1, "pcs"), ing("garlic", 2, "pcs"), ing("cumin", 1, "tsp"), ing("oil", 1, "tbsp")));

        r.add(new SeedRecipe("Creamy Mashed Potatoes", "Smooth, buttery mash.", 30, 4,
                "Peel and cut the potatoes, then boil in salted water for 15-20 minutes until soft.\nDrain well.\nMash with the butter and warm milk until smooth.\nSeason with salt.",
                ing("potato", 4, "pcs"), ing("butter", 30, "g"), ing("milk", 100, "ml"), taste("salt")));

        r.add(new SeedRecipe("Crispy Roast Potatoes", "Golden on the outside, fluffy inside.", 55, 4,
                "Heat the oven to 220 C.\nCut the potatoes into chunks and parboil for 8 minutes. Drain and shake to roughen the edges.\nToss in the oil, salt and pepper and spread on a hot tray.\nRoast for 35-40 minutes, turning once.",
                ing("potato", 6, "pcs"), ing("oil", 3, "tbsp"), taste("salt"), taste("black pepper")));

        r.add(new SeedRecipe("Creamy Oats Porridge", "A warm bowl to start the day.", 10, 1,
                "Combine the oats and milk in a small pot.\nCook over medium heat, stirring, for 5 minutes until thick.\nStir in the sugar and serve.",
                ing("oat", 80, "g"), ing("milk", 300, "ml"), ing("sugar", 1, "tbsp")));

        r.add(new SeedRecipe("French Toast", "Eggy bread, pan-fried until golden.", 15, 2,
                "Whisk the eggs, milk and sugar in a shallow dish.\nSoak each bread slice for 20 seconds per side.\nMelt the butter in a pan and fry the slices until golden on both sides.\nServe warm.",
                ing("bread", 4, "pcs"), ing("egg", 2, "pcs"), ing("milk", 100, "ml"), ing("sugar", 1, "tbsp"), ing("butter", 10, "g")));

        r.add(new SeedRecipe("Lentil Dal", "Spiced lentil stew.", 40, 3,
                "Rinse the lentils and simmer in 600 ml water for 20 minutes until soft.\nFry the onion and garlic in the oil, add the curry powder and chopped tomatoes.\nCook for 8 minutes, then stir into the lentils.\nSimmer 5 more minutes and serve.",
                ing("lentil", 200, "g"), ing("onion", 1, "pcs"), ing("tomato", 2, "pcs"), ing("garlic", 2, "pcs"), ing("curry powder", 2, "tsp"), ing("oil", 1, "tbsp")));

        r.add(new SeedRecipe("Easy Chicken Curry", "Weeknight curry with simple pantry spices.", 50, 3,
                "Fry the sliced onions in the oil until golden. Add the garlic and curry powder.\nAdd the diced chicken and brown it.\nStir in the chopped tomatoes and 200 ml water.\nSimmer for 25 minutes until the chicken is tender.",
                ing("chicken breast", 400, "g"), ing("onion", 2, "pcs"), ing("tomato", 2, "pcs"), ing("garlic", 3, "pcs"), ing("curry powder", 2, "tbsp"), ing("oil", 2, "tbsp")));

        r.add(new SeedRecipe("Grilled Cheese Sandwich", "Crispy outside, gooey inside.", 10, 1,
                "Butter one side of each bread slice.\nPlace the cheese between the slices, buttered sides out.\nFry in a pan over medium heat for 2-3 minutes per side until golden.",
                ing("bread", 2, "pcs"), ing("cheese", 40, "g"), ing("butter", 10, "g")));

        r.add(new SeedRecipe("Chakalaka-style Beans", "A mild, spiced vegetable relish with baked beans.", 30, 4,
                "Fry the chopped onion, chili and grated carrot in the oil.\nAdd the diced bell pepper and curry powder and cook for 5 minutes.\nStir in the baked beans and simmer for 10 minutes.\nServe with pap or bread.",
                ing("baked bean", 1, "pcs"), ing("carrot", 1, "pcs"), ing("bell pepper", 1, "pcs"), ing("onion", 1, "pcs"), ing("chili", 1, "pcs"), ing("curry powder", 1, "tsp"), ing("oil", 1, "tbsp")));

        r.add(new SeedRecipe("Vetkoek", "Fried bread rolls, delicious with mince or jam.", 120, 6,
                "Mix the flour, yeast, sugar and a pinch of salt. Add 300 ml warm water and knead into a soft dough.\nCover and let it rise for 1 hour.\nShape into balls and deep-fry in hot oil until golden brown, turning once.\nDrain on paper towel and serve warm.",
                ing("flour", 500, "g"), ing("yeast", 10, "g"), ing("sugar", 1, "tbsp"), ing("oil", 250, "ml"), taste("salt")));

        return r;
    }
}
