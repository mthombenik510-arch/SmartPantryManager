package com.example.smartpantry.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.data.Recipe;
import com.example.smartpantry.data.Repository;
import com.example.smartpantry.data.Suggestions;
import com.google.android.material.tabs.TabLayout;

/**
 * Suggested Recipes screen. Tab 1 is the STRICT list (the assignment's core rule): only recipes
 * whose every ingredient is in the pantry in a sufficient amount. Tab 2 is the optional
 * "Almost there" list, kept visibly separate because those recipes cannot be cooked yet.
 */
public class SuggestedFragment extends Fragment implements RecipeAdapter.Listener {

    private static final int TAB_COOK_NOW = 0;
    private static final int TAB_ALMOST = 1;

    private TabLayout tabs;
    private TextView note;
    private TextView emptyView;
    private RecipeAdapter adapter;
    private Suggestions latest;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_suggested, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tabs = view.findViewById(R.id.tabs);
        note = view.findViewById(R.id.list_note);
        emptyView = view.findViewById(R.id.recipe_empty);

        RecyclerView list = view.findViewById(R.id.recipe_list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RecipeAdapter(this);
        list.setAdapter(adapter);

        tabs.addTab(tabs.newTab().setText(getString(R.string.tab_cook_now, 0)));
        tabs.addTab(tabs.newTab().setText(getString(R.string.tab_almost, 0)));
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                render();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        render();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Re-run the matching every time the screen is shown, so it always reflects the pantry.
        Repository.get(requireContext()).loadSuggestions(result -> {
            if (getView() == null) {
                return;                 // fragment went away while the query was running
            }
            latest = result;
            TabLayout.Tab first = tabs.getTabAt(TAB_COOK_NOW);
            TabLayout.Tab second = tabs.getTabAt(TAB_ALMOST);
            if (first != null) {
                first.setText(getString(R.string.tab_cook_now, result.cookable.size()));
            }
            if (second != null) {
                second.setText(getString(R.string.tab_almost, result.almostThere.size()));
            }
            render();
        });
    }

    private void render() {
        boolean almostTab = tabs.getSelectedTabPosition() == TAB_ALMOST;
        note.setText(almostTab ? R.string.note_almost : R.string.note_cook_now);

        if (latest == null) {
            adapter.setItems(null, almostTab);
            emptyView.setVisibility(View.GONE);
            return;
        }

        if (almostTab) {
            adapter.setItems(latest.almostThere, true);
            emptyView.setText(R.string.empty_no_almost);
            emptyView.setVisibility(latest.almostThere.isEmpty() ? View.VISIBLE : View.GONE);
        } else {
            adapter.setItems(latest.cookable, false);
            // Friendly feedback instead of a blank screen when nothing matches.
            emptyView.setText(latest.pantrySize == 0 ? R.string.empty_no_pantry : R.string.empty_no_match);
            emptyView.setVisibility(latest.cookable.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onRecipeClicked(Recipe recipe) {
        Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.id);
        startActivity(intent);
    }
}
