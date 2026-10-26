package com.example.smartpantry.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.data.Recipe;
import com.example.smartpantry.data.RecipeMatch;
import com.example.smartpantry.matching.MatchResult;

import java.util.ArrayList;
import java.util.List;

/** Adapter for the recipe cards on the Suggested Recipes screen. */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.Holder> {

    public interface Listener {
        void onRecipeClicked(Recipe recipe);
    }

    private final List<RecipeMatch> items = new ArrayList<>();
    private final Listener listener;
    /** True on the "Almost there" tab: also shows which ingredient is missing. */
    private boolean showMissing = false;

    public RecipeAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<RecipeMatch> newItems, boolean showMissing) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        this.showMissing = showMissing;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        RecipeMatch match = items.get(position);
        Recipe r = match.recipe;
        h.name.setText(r.name);
        h.meta.setText(h.itemView.getContext().getString(R.string.recipe_meta, r.minutes, r.servings));
        h.description.setText(r.description);

        if (showMissing) {
            StringBuilder sb = new StringBuilder();
            for (MatchResult.Line line : match.result.getMissingLines()) {
                if (sb.length() > 0) {
                    sb.append("; ");
                }
                sb.append(line.describeShortfall());
            }
            h.missing.setText(h.itemView.getContext().getString(R.string.missing_prefix, sb.toString()));
            h.missing.setVisibility(View.VISIBLE);
        } else {
            h.missing.setVisibility(View.GONE);
        }
        h.itemView.setOnClickListener(v -> listener.onRecipeClicked(r));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView meta;
        final TextView description;
        final TextView missing;

        Holder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.recipe_name);
            meta = itemView.findViewById(R.id.recipe_meta);
            description = itemView.findViewById(R.id.recipe_description);
            missing = itemView.findViewById(R.id.recipe_missing);
        }
    }
}
