package com.example.smartpantry.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantry.R;
import com.example.smartpantry.data.PantryItem;
import com.example.smartpantry.data.Repository;
import com.example.smartpantry.matching.QuantityFormatter;
import com.example.smartpantry.matching.UnitConverter;
import com.example.smartpantry.util.ExpiryUtils;
import com.example.smartpantry.util.Prefs;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Date;

/**
 * Add / Edit Ingredient screen (Create + Update). Started with an Intent; when the Intent carries
 * {@link #EXTRA_ITEM_ID} the matching pantry row is loaded for editing, otherwise a new one is created.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "com.example.smartpantry.ITEM_ID";

    private Repository repo;
    private long itemId = 0;            // 0 = creating a new item
    private PantryItem existing;        // loaded item when editing

    private TextInputLayout layoutName;
    private TextInputLayout layoutQuantity;
    private TextInputEditText inputName;
    private TextInputEditText inputQuantity;
    private TextInputEditText inputExpiry;
    private Spinner spinnerUnit;
    private Button saveButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);
        repo = Repository.get(this);

        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, 0);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(itemId == 0 ? R.string.title_add : R.string.title_edit);
        }

        layoutName = findViewById(R.id.layout_name);
        layoutQuantity = findViewById(R.id.layout_quantity);
        inputName = findViewById(R.id.input_name);
        inputQuantity = findViewById(R.id.input_quantity);
        inputExpiry = findViewById(R.id.input_expiry);
        spinnerUnit = findViewById(R.id.spinner_unit);
        saveButton = findViewById(R.id.btn_save);

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, UnitConverter.DISPLAY_UNITS);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);

        inputExpiry.setOnClickListener(v -> pickDate());
        findViewById(R.id.btn_clear_date).setOnClickListener(v -> inputExpiry.setText(""));
        saveButton.setOnClickListener(v -> save());

        if (savedInstanceState == null) {
            if (itemId == 0) {
                selectUnit(Prefs.getDefaultUnit(this));
            } else {
                repo.loadPantryItem(itemId, this::bind);
            }
        }
        // On rotation the views restore their own text/selection, and `existing` is reloaded lazily.
        if (savedInstanceState != null && itemId != 0) {
            repo.loadPantryItem(itemId, item -> existing = item);
        }
    }

    private void bind(PantryItem item) {
        if (item == null) {
            Toast.makeText(this, R.string.item_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        existing = item;
        inputName.setText(item.name);
        inputQuantity.setText(QuantityFormatter.number(item.quantity));
        selectUnit(item.unit);
        inputExpiry.setText(item.expiryDate == null ? "" : item.expiryDate);
        invalidateOptionsMenu();
    }

    private void selectUnit(String unit) {
        String wanted = UnitConverter.canonicalUnit(unit);
        for (int i = 0; i < UnitConverter.DISPLAY_UNITS.length; i++) {
            if (UnitConverter.DISPLAY_UNITS[i].equals(wanted)) {
                spinnerUnit.setSelection(i);
                return;
            }
        }
    }

    private void pickDate() {
        Calendar cal = Calendar.getInstance();
        Date existingDate = ExpiryUtils.parse(text(inputExpiry));
        if (existingDate != null) {
            cal.setTime(existingDate);
        }
        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar picked = Calendar.getInstance();
            picked.clear();
            picked.set(year, month, day);
            inputExpiry.setText(ExpiryUtils.format(picked));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private static String text(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    /** Input validation for the form. Returns true only when every field is acceptable. */
    private boolean validate() {
        boolean ok = true;
        layoutName.setError(null);
        layoutQuantity.setError(null);

        String name = text(inputName);
        if (name.isEmpty()) {
            layoutName.setError(getString(R.string.err_name_required));
            ok = false;
        } else if (!name.matches(".*\\p{L}.*")) {
            layoutName.setError(getString(R.string.err_name_letters));
            ok = false;
        } else if (name.length() > 40) {
            layoutName.setError(getString(R.string.err_name_long));
            ok = false;
        }

        String q = text(inputQuantity).replace(',', '.');
        if (q.isEmpty()) {
            layoutQuantity.setError(getString(R.string.err_quantity_required));
            ok = false;
        } else {
            try {
                double value = Double.parseDouble(q);
                if (Double.isNaN(value) || value <= 0) {
                    layoutQuantity.setError(getString(R.string.err_quantity_invalid));
                    ok = false;
                } else if (value > 1_000_000) {
                    layoutQuantity.setError(getString(R.string.err_quantity_big));
                    ok = false;
                }
            } catch (NumberFormatException e) {
                layoutQuantity.setError(getString(R.string.err_quantity_invalid));
                ok = false;
            }
        }
        return ok;
    }

    private void save() {
        if (itemId != 0 && existing == null) {
            return;                         // still loading the row we are editing
        }
        if (!validate()) {
            return;
        }
        PantryItem item = existing != null ? existing : new PantryItem();
        item.name = text(inputName);
        item.quantity = Double.parseDouble(text(inputQuantity).replace(',', '.'));
        item.unit = (String) spinnerUnit.getSelectedItem();
        String expiry = text(inputExpiry);
        item.expiryDate = expiry.isEmpty() ? null : expiry;

        saveButton.setEnabled(false);       // avoid double taps
        repo.savePantryItem(item, () -> {
            Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (itemId != 0) {
            getMenuInflater().inflate(R.menu.menu_add_edit, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_delete && existing != null) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.delete_title)
                    .setMessage(getString(R.string.delete_message, existing.name))
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.delete,
                            (d, which) -> repo.deletePantryItem(existing, this::finish))
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
