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
import com.example.smartpantry.data.PantryItem;
import com.example.smartpantry.data.Repository;
import com.example.smartpantry.util.ExpiryUtils;
import com.example.smartpantry.util.Prefs;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

/** Pantry list screen: shows every ingredient from the database and lets the user add/edit/delete. */
public class PantryFragment extends Fragment implements PantryAdapter.Listener {

    private Repository repo;
    private PantryAdapter adapter;
    private TextView emptyView;
    private TextView banner;
    private View root;
    private List<PantryItem> current = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pantry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        root = view.findViewById(R.id.pantry_root);
        emptyView = view.findViewById(R.id.pantry_empty);
        banner = view.findViewById(R.id.expiry_banner);
        repo = Repository.get(requireContext());

        RecyclerView list = view.findViewById(R.id.pantry_list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PantryAdapter(this);
        list.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fab_add);
        fab.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddEditIngredientActivity.class)));

        // LiveData: the list refreshes by itself after any add / edit / delete.
        repo.observePantry().observe(getViewLifecycleOwner(), items -> {
            current = items == null ? new ArrayList<PantryItem>() : items;
            adapter.setItems(current);
            refreshChrome();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshChrome();    // settings may have changed while we were away
    }

    private void refreshChrome() {
        if (getView() == null) {
            return;
        }
        boolean alerts = Prefs.isExpiryAlertsEnabled(requireContext());
        int days = Prefs.getAlertDays(requireContext());
        adapter.setAlerts(alerts, days);

        emptyView.setVisibility(current.isEmpty() ? View.VISIBLE : View.GONE);

        int flagged = 0;
        if (alerts) {
            for (PantryItem item : current) {
                ExpiryUtils.State s = ExpiryUtils.state(item.expiryDate, days);
                if (s == ExpiryUtils.State.SOON || s == ExpiryUtils.State.EXPIRED) {
                    flagged++;
                }
            }
        }
        if (flagged > 0) {
            banner.setText(getResources().getQuantityString(R.plurals.expiring_banner, flagged, flagged, days));
            banner.setVisibility(View.VISIBLE);
        } else {
            banner.setVisibility(View.GONE);
        }
    }

    @Override
    public void onEdit(PantryItem item) {
        Intent intent = new Intent(requireContext(), AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.id);
        startActivity(intent);
    }

    @Override
    public void onDelete(PantryItem item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message, item.name))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (d, which) -> {
                    repo.deletePantryItem(item, null);
                    Snackbar.make(root, getString(R.string.deleted_item, item.name), Snackbar.LENGTH_LONG)
                            .setAction(R.string.undo, v -> repo.savePantryItem(
                                    new PantryItem(item.name, item.quantity, item.unit, item.expiryDate), null))
                            .show();
                })
                .show();
    }
}
