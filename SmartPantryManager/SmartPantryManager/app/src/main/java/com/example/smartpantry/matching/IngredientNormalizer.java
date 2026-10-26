package com.example.smartpantry.matching;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Turns free-text ingredient names into a canonical key so that trivial differences
 * ("Tomato" vs "tomatoes", " Fresh  Onions ", "Capsicum" vs "bell pepper") do not
 * break the strict-matching rule.
 *
 * Both the pantry side and the recipe side are normalised with the same function, so the
 * singular/plural rules only have to be *consistent*, not linguistically perfect.
 */
public final class IngredientNormalizer {

    /** Words that describe preparation/size/state but do not change which ingredient it is. */
    private static final Set<String> DESCRIPTORS = new HashSet<>(Arrays.asList(
            "fresh", "large", "small", "medium", "big", "ripe", "chopped", "diced", "sliced",
            "grated", "frozen", "raw", "organic", "peeled", "finely",
            "roughly", "extra", "a", "an", "the", "of"));

    /** Plural forms that the generic suffix rules would get wrong. */
    private static final Map<String, String> IRREGULAR = new HashMap<>();

    /** Different names for the same thing (checked after singularising). */
    private static final Map<String, String> SYNONYMS = new HashMap<>();

    /** Single-word spelling variants, applied word by word before singularising. */
    private static final Map<String, String> WORD_ALIASES = new HashMap<>();

    static {
        IRREGULAR.put("leaves", "leaf");
        IRREGULAR.put("loaves", "loaf");
        IRREGULAR.put("halves", "half");
        IRREGULAR.put("cookies", "cookie");
        IRREGULAR.put("pies", "pie");
        IRREGULAR.put("molasses", "molasses");
        IRREGULAR.put("couscous", "couscous");
        IRREGULAR.put("hummus", "hummus");
        IRREGULAR.put("asparagus", "asparagus");

        WORD_ALIASES.put("tinned", "canned");
        WORD_ALIASES.put("chilli", "chili");
        WORD_ALIASES.put("chillies", "chili");
        WORD_ALIASES.put("chilies", "chili");
        WORD_ALIASES.put("yoghurt", "yogurt");
        WORD_ALIASES.put("mince", "mince");

        SYNONYMS.put("minced garlic", "garlic");
        SYNONYMS.put("minced onion", "onion");
        SYNONYMS.put("minced beef", "beef mince");
        SYNONYMS.put("capsicum", "bell pepper");
        SYNONYMS.put("green pepper", "bell pepper");
        SYNONYMS.put("red pepper", "bell pepper");
        SYNONYMS.put("sweet pepper", "bell pepper");
        SYNONYMS.put("aubergine", "eggplant");
        SYNONYMS.put("courgette", "zucchini");
        SYNONYMS.put("baby marrow", "zucchini");
        SYNONYMS.put("scallion", "spring onion");
        SYNONYMS.put("green onion", "spring onion");
        SYNONYMS.put("coriander", "cilantro");
        SYNONYMS.put("mielie meal", "maize meal");
        SYNONYMS.put("mealie meal", "maize meal");
        SYNONYMS.put("cornmeal", "maize meal");
        SYNONYMS.put("plain flour", "flour");
        SYNONYMS.put("all purpose flour", "flour");
        SYNONYMS.put("cake flour", "flour");
        SYNONYMS.put("white flour", "flour");
        SYNONYMS.put("bicarbonate of soda", "baking soda");
        SYNONYMS.put("bicarb", "baking soda");
        SYNONYMS.put("caster sugar", "sugar");
        SYNONYMS.put("white sugar", "sugar");
        SYNONYMS.put("granulated sugar", "sugar");
        SYNONYMS.put("garlic clove", "garlic");
        SYNONYMS.put("clove garlic", "garlic");
        SYNONYMS.put("cooking oil", "oil");
        SYNONYMS.put("vegetable oil", "oil");
        SYNONYMS.put("sunflower oil", "oil");
        SYNONYMS.put("chilli pepper", "chili");
        SYNONYMS.put("chili pepper", "chili");
        SYNONYMS.put("ground beef", "beef mince");
        SYNONYMS.put("mince", "beef mince");
        SYNONYMS.put("chicken breast fillet", "chicken breast");
        SYNONYMS.put("tinned tomato", "canned tomato");
        SYNONYMS.put("tin tomato", "canned tomato");
        SYNONYMS.put("tinned tuna", "canned tuna");
        SYNONYMS.put("tin tuna", "canned tuna");
        SYNONYMS.put("tuna", "canned tuna");
        SYNONYMS.put("irish potato", "potato");
        SYNONYMS.put("full cream milk", "milk");
        SYNONYMS.put("whole milk", "milk");
    }

    private IngredientNormalizer() {
    }

    /** Returns the canonical key for an ingredient name. Never returns null. */
    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.toLowerCase(Locale.ROOT);
        s = s.replaceAll("\\(.*?\\)", " ");          // drop "(optional)" style notes
        s = s.replaceAll("[^a-z\\s]", " ");           // drop digits/punctuation
        s = s.replaceAll("\\s+", " ").trim();
        if (s.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (String word : s.split(" ")) {
            if (DESCRIPTORS.contains(word)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(word);
        }
        s = sb.toString().trim();
        if (s.isEmpty()) {
            return "";
        }

        // Singularise every word so "cherry tomatoes" and "cherry tomato" agree.
        String[] words = s.split(" ");
        for (int i = 0; i < words.length; i++) {
            String alias = WORD_ALIASES.get(words[i]);
            words[i] = alias != null ? alias : singular(words[i]);
        }
        s = String.join(" ", words);

        String synonym = SYNONYMS.get(s);
        return synonym != null ? synonym : s;
    }

    /** Simple English singulariser; consistency matters more than perfection. */
    static String singular(String w) {
        if (w.length() <= 2) {
            return w;
        }
        String irregular = IRREGULAR.get(w);
        if (irregular != null) {
            return irregular;
        }
        if (w.endsWith("ies") && w.length() > 4) {
            return w.substring(0, w.length() - 3) + "y";          // berries -> berry
        }
        if (w.endsWith("oes")) {
            return w.substring(0, w.length() - 2);                 // tomatoes -> tomato
        }
        if (w.endsWith("ches") || w.endsWith("shes") || w.endsWith("xes") || w.endsWith("zes")) {
            return w.substring(0, w.length() - 2);                 // peaches -> peach
        }
        if (w.endsWith("sses")) {
            return w.substring(0, w.length() - 2);                 // glasses -> glass
        }
        if (w.endsWith("ss") || w.endsWith("us")) {
            return w;                                              // cress, hummus
        }
        if (w.endsWith("s")) {
            return w.substring(0, w.length() - 1);                 // onions -> onion
        }
        return w;
    }
}
