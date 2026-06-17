package com.example.speaktimer.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import com.example.speaktimer.R;
import com.example.speaktimer.databinding.ActivitySettingsBinding;
import com.example.speaktimer.settings.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    private PreferenceManager prefManager;
    private final int[] intervalSecondsValues = {1, 5, 10, 15, 30, 60};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefManager = new PreferenceManager(this);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupIntervalSpinner();
        loadPreferences();

        binding.chkNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefManager.setNotificationsEnabled(isChecked);
        });

        binding.chkDefaultSpeak.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefManager.setDefaultSpeakEnabled(isChecked);
        });

        binding.spinnerDefaultInterval.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position >= 0 && position < intervalSecondsValues.length) {
                    prefManager.setDefaultSpeakingInterval(intervalSecondsValues[position]);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void setupIntervalSpinner() {
        String[] intervalLabels = {
                getString(R.string.interval_1s),
                getString(R.string.interval_5s),
                getString(R.string.interval_10s),
                getString(R.string.interval_15s),
                getString(R.string.interval_30s),
                getString(R.string.interval_60s)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, intervalLabels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerDefaultInterval.setAdapter(adapter);
    }

    private void loadPreferences() {
        binding.chkNotifications.setChecked(prefManager.isNotificationsEnabled());
        binding.chkDefaultSpeak.setChecked(prefManager.isDefaultSpeakEnabled());

        int currentInterval = prefManager.getDefaultSpeakingInterval();
        int selectedIndex = 1; // Default to 5s
        for (int i = 0; i < intervalSecondsValues.length; i++) {
            if (intervalSecondsValues[i] == currentInterval) {
                selectedIndex = i;
                break;
            }
        }
        binding.spinnerDefaultInterval.setSelection(selectedIndex);
    }
}
