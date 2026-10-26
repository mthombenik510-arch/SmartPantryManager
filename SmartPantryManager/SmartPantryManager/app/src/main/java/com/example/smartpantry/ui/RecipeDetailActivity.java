package com.example.smartpantry.ui;

import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.smartpantry.R;
import com.example.smartpantry.data.Recipe;
import com.example.smartpantry.data.RecipeDetail;
import com.example.smartpantry.data.RecipeIngredient;
import com.example.smartpantry.data.Repository;
import com.example.smartpantry.matching.MatchResult;
import com.example.smartpantry.matching.QuantityFormatter;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

/** Recipe Detail screen: full ingredient list (with have / missing marks) and the method. */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "com.example.smartpantry.RECIPE_ID";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_recipe);
        }

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        Repository.get(this).loadRecipeDetail(recipeId, detail -> {
            if (detail == null) {
                Toast.makeText(this, R.string.recipe_not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            show(detail);
        });
    }

    private void show(RecipeDetail detail) {
        Recipe r = detail.recipe;
        ((TextView) findViewById(R.id.detail_name)).setText(r.name);
        ((TextView) findViewById(R.id.detail_meta)).setText(getString(R.string.recipe_meta, r.minutes, r.servings));
        ((TextView) findViewById(R.id.detail_description)).setText(r.description);

        // Status banner: can the user cook this right now?
        TextView status = findViewById(R.id.detail_status);
        int missing = detail.match.getMissingCount();
        if (detail.match.canCook()) {
            status.setText(R.string.status_can_cook);
            status.setBackgroundColor(ContextCompat.getColor(this, R.color.green_50));
            status.setTextColor(ContextCompat.getColor(this, R.color.green_900));
        } else {
            status.setText(getResources().getQuantityString(R.plurals.status_missing, missing, missing));
            status.setBackgroundColor(ContextCompat.getColor(this, R.color.orange_50));
            status.setTextColor(ContextCompat.getColor(this, R.color.orange_700));
        }

        // Ingredient list.
        LinearLayout ingredients = findViewById(R.id.detail_ingredients);
        ingredients.removeAllViews();
        List<RecipeIngredient> needs = detail.ingredients;
        List<MatchResult.Line> lines = detail.match.getLines();
        for (int i = 0; i < needs.size(); i++) {
            RecipeIngredient need = needs.get(i);
            MatchResult.Line line = lines.get(i);
            String amount = QuantityFormatter.format(need.quantity, need.unit);

            StringBuilder text = new StringBuilder();
            text.append(line.isSatisfied() ? "✓  " : "✗  ");
            text.append(capitalise(need.name)).append(" – ").append(amount);
            if (line.status == MatchResult.Status.MISSING) {
                text.append("  (not in pantry)");
            } else if (line.status == MatchResult.Status.INSUFFICIENT) {
                text.append("  (you have ").append(QuantityFormatter.format(line.haveQuantity, need.unit)).append(")");
            }
            TextView row = makeText(text.toString(), 16);
            row.setTextColor(ContextCompat.getColor(this,
                    line.isSatisfied() ? R.color.green_700 : R.color.red_700));
            ingredients.addView(row);
        }

        // Method, one numbered step per line.
        LinearLayout steps = findViewById(R.id.detail_steps);
        steps.removeAllViews();
        String[] parts = r.steps.split("\n");
        int n = 1;
        for (String step : parts) {
            if (step.trim().isEmpty()) {
                continue;
            }
            TextView row = makeText(n + ".  " + step.trim(), 16);
            row.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            steps.addView(row);
            n++;
        }
    }

    private TextView makeText(String text, int sizeSp) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(sizeSp);
        tv.setGravity(Gravity.START);
        tv.setLineSpacing(0, 1.15f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = (int) (6 * getResources().getDisplayMetrics().density);
        tv.setLayoutParams(lp);
        return tv;
    }

    private static String capitalise(String s) {
        return s == null || s.isEmpty() ? "" : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
