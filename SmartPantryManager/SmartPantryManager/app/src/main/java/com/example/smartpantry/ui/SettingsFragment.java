package com.example.smartpantry.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartpantry.R;
import com.example.smartpantry.matching.UnitConverter;
import com.example.smartpantry.util.Prefs;
import com.google.android.material.switchmaterial.SwitchMaterial;

/** Settings screen: expiring-soon alerts, the alert window, and the default unit for new items. */
public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SwitchMaterial alerts = view.findViewById(R.id.switch_expiry);
        Spinner daysSpinner = view.findViewById(R.id.spinner_days);
        Spinner unitSpinner = view.findViewById(R.id.spinner_default_unit);

        // Expiring-soon alerts on/off.
        alerts.setChecked(Prefs.isExpiryAlertsEnabled(requireContext()));
        alerts.setOnCheckedChangeListener((button, checked) -> {
            Prefs.setExpiryAlertsEnabled(requireContext(), checked);
            daysSpinner.setEnabled(checked);
        });
        daysSpinner.setEnabled(alerts.isChecked());

        // Alert window (1 / 3 / 7 / 14 days).
        String[] dayLabels = new String[Prefs.ALERT_DAY_OPTIONS.length];
        int selectedDays = 0;
        int currentDays = Prefs.getAlertDays(requireContext());
        for (int i = 0; i < dayLabels.length; i++) {
            int d = Prefs.ALERT_DAY_OPTIONS[i];
            dayLabels[i] = getResources().getQuantityString(R.plurals.days_option, d, d);
            if (d == currentDays) {
                selectedDays = i;
            }
        }
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, dayLabels);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daysSpinner.setAdapter(dayAdapter);
        daysSpinner.setSelection(selectedDays);
        daysSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                Prefs.setAlertDays(requireContext(), Prefs.ALERT_DAY_OPTIONS[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Default unit for new ingredients.
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, UnitConverter.DISPLAY_UNITS);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(unitAdapter);
        String currentUnit = Prefs.getDefaultUnit(requireContext());
        for (int i = 0; i < UnitConverter.DISPLAY_UNITS.length; i++) {
            if (UnitConverter.DISPLAY_UNITS[i].equals(currentUnit)) {
                unitSpinner.setSelection(i);
            }
        }
        unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                Prefs.setDefaultUnit(requireContext(), UnitConverter.DISPLAY_UNITS[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}
