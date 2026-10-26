package com.example.smartpantry.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.smartpantry.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Host activity: a toolbar, a bottom navigation bar and a container that swaps between the
 * Pantry, Suggested Recipes and Settings fragments.
 */
public class MainActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        BottomNavigationView nav = findViewById(R.id.bottom_nav);

        nav.setOnItemSelectedListener(item -> {
            show(item.getItemId());
            return true;
        });

        if (savedInstanceState == null) {
            show(R.id.nav_pantry);          // first tab is selected by default
        } else {
            updateTitle(nav.getSelectedItemId());   // fragment itself is restored by the system
        }
    }

    private void show(int navId) {
        Fragment fragment;
        if (navId == R.id.nav_recipes) {
            fragment = new SuggestedFragment();
        } else if (navId == R.id.nav_settings) {
            fragment = new SettingsFragment();
        } else {
            fragment = new PantryFragment();
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        updateTitle(navId);
    }

    private void updateTitle(int navId) {
        if (navId == R.id.nav_recipes) {
            toolbar.setTitle(R.string.title_suggested);
        } else if (navId == R.id.nav_settings) {
            toolbar.setTitle(R.string.title_settings);
        } else {
            toolbar.setTitle(R.string.title_pantry);
        }
    }
}
