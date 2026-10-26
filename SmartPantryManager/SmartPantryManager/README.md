# Smart Pantry Manager (Mobile App Development 700)

An Android app, written entirely in **Java**, that tracks the ingredients you have at home and
suggests recipes you can cook using **only** what is already in your pantry.

## Open and run

1. Install **Android Studio** (Hedgehog 2023.1.1 or newer) - it bundles JDK 17.
2. `File > Open` and choose this folder (`SmartPantryManager`). Let Gradle sync finish
   (needs internet the first time to download dependencies).
3. Pick an emulator or a phone (Android 7.0 / API 24 or newer) and press **Run**.
4. Unit tests: right-click `app/src/test` > *Run Tests*, or `./gradlew testDebugUnitTest`.

## Features (mapped to the brief)

| Brief requirement | Where it is implemented |
|---|---|
| Add / edit / delete pantry items (name, quantity, unit, optional expiry) | `AddEditIngredientActivity`, delete button + undo in `PantryFragment` |
| Pantry list with RecyclerView bound to the database | `PantryFragment`, `PantryAdapter`, Room `LiveData` |
| 15-20 seeded recipes (name, ingredients, steps) | `data/SeedData.java` (20 recipes), loaded on first run by `Repository.seedIfEmpty()` |
| Suggested Recipes screen using strict matching | `SuggestedFragment` (tab "Cook now") + `matching/RecipeMatcher` |
| Recipe detail (ingredients + method) | `RecipeDetailActivity` (shows a tick or cross against each ingredient) |
| Settings screen | `SettingsFragment` (expiring-soon alerts, alert window, default unit) |
| Feedback when nothing matches | Empty-state message on the Suggested screen |
| Optional "Almost There" list (bonus) | Second tab, clearly labelled "NOT ready to cook" |
| Intents to navigate and pass data | `EXTRA_ITEM_ID`, `EXTRA_RECIPE_ID` extras |
| Navigation element | Bottom navigation bar (`MainActivity`) |
| Input validation | `AddEditIngredientActivity.validate()` |
| No maps / GPS / location | No such dependency and no permissions in `AndroidManifest.xml` |

## The strict-matching rule (`matching/RecipeMatcher.java`)

A recipe is suggested only if **every** ingredient is in the pantry in at least the required
quantity. 5 ingredients needed and 4 owned means the recipe is **not** in the suggestions.

Robustness against messy input:

* **Names** are normalised: case, extra spaces, punctuation, plurals (`Tomatoes` = `tomato`,
  `cherries` = `cherry`, `bay leaves` = `bay leaf`), filler words (`fresh`, `chopped`...) and a
  small synonym table (`capsicum` = `bell pepper`, `mielie meal` = `maize meal`, `tinned` = `canned`).
* **Units** are converted inside a dimension: `kg/g/oz/lb`, `l/ml/cup/tbsp/tsp`, and pieces.
  1 kg of flour satisfies a recipe that needs 500 g.
* **Duplicate rows** for one ingredient are added together (two bags of rice).
* **"To taste" ingredients** (salt, pepper) only need to be present.
* **Different dimensions are not guessed**: 2 onions do not satisfy "200 g onion", because the
  app cannot know what an onion weighs. The strict rule says no.
* Different ingredients are never merged: `peanut butter` does not satisfy `butter`.

Behaviour you should mention in the report: expired items still count as "in the pantry" (they are
highlighted instead), and plain water is assumed to always be available.

## Database choice: SQLite via Room

Local, offline, no server to host, and it survives closing and reopening the app. Room gives
compile-time-checked SQL, `LiveData` that refreshes the list automatically after any change,
and keeps all disk access off the main thread (`Repository` runs it on a background executor).
Tables: `pantry_items`, `recipes`, `recipe_ingredients` (foreign key to `recipes`).

## Project layout

```
app/src/main/java/com/example/smartpantry/
  PantryApp.java            app start: opens DB and seeds recipes
  data/                     Room entities, DAOs, AppDatabase, Repository, SeedData
  matching/                 pure-Java matching engine (no Android imports, unit tested)
  ui/                       MainActivity, fragments, activities, adapters
  util/                     Prefs (settings), ExpiryUtils
app/src/test/               JUnit tests for the matcher and the seed data
```

## Manual test script (good for the demo video)

1. Fresh install: Recipes tab shows *"No recipes match your pantry yet - add more ingredients."*
2. Add `Eggs` 6 pcs, `Bread` 4 pcs, `Butter` 250 g, `Salt` 1 g. "Cook now" shows **Scrambled Eggs on
   Toast** only. "Almost there" shows Cheese Omelette and Grilled Cheese (each missing just cheese) -
   they are NOT in the main list.
3. Add `Tomatoes` 3 pcs (plural), `Onions` 2 pcs, `Maize meal` 1 kg, `Oil` 500 ml: **Pap and Tomato
   Relish** appears (1 kg satisfies 250 g, and the plural names match).
4. Edit `Eggs` down to 2 pcs: Scrambled Eggs leaves "Cook now" and moves to "Almost there"
   ("egg: need 3 pcs, have 2 pcs").
5. Delete an item, then tap Undo in the snackbar.
6. Set an expiry date within 3 days: orange highlight and banner. Switch alerts off in Settings: both go away.
7. Close the app completely and reopen it: the pantry is still there.
