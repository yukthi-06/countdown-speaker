package com.example.speaktimer.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.speaktimer.R;
import com.example.speaktimer.databinding.FragmentCountupBinding;
import com.example.speaktimer.settings.PreferenceManager;
import com.example.speaktimer.tts.TextToSpeechManager;
import java.util.Locale;

public class CountupFragment extends Fragment {

    private FragmentCountupBinding binding;
    private TextToSpeechManager ttsManager;
    private PreferenceManager prefManager;

    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;
    private boolean isPaused = false;
    private int elapsedTimeSeconds = 0;

    private static final String KEY_ELAPSED_TIME = "elapsed_time";
    private static final String KEY_IS_RUNNING = "is_running";
    private static final String KEY_IS_PAUSED = "is_paused";

    private final int[] intervalSecondsValues = {1, 5, 10, 15, 30, 60};

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning && !isPaused) {
                elapsedTimeSeconds++;
                updateTimerDisplay();

                if (binding.chkSpeak.isChecked()) {
                    if (elapsedTimeSeconds % getSelectedIntervalSeconds() == 0) {
                        speakTime(elapsedTimeSeconds);
                    }
                }

                timerHandler.postDelayed(this, 1000);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCountupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ttsManager = new TextToSpeechManager(requireContext());
        prefManager = new PreferenceManager(requireContext());

        setupIntervalSpinner();
        loadDefaults();

        if (savedInstanceState != null) {
            elapsedTimeSeconds = savedInstanceState.getInt(KEY_ELAPSED_TIME);
            isRunning = savedInstanceState.getBoolean(KEY_IS_RUNNING);
            isPaused = savedInstanceState.getBoolean(KEY_IS_PAUSED);

            updateTimerDisplay();
            updateUIState();

            if (isRunning && !isPaused) {
                timerHandler.postDelayed(timerRunnable, 1000);
            }
        }

        binding.btnStart.setOnClickListener(v -> startTimer());
        binding.btnPause.setOnClickListener(v -> pauseTimer());
        binding.btnResume.setOnClickListener(v -> resumeTimer());
        binding.btnReset.setOnClickListener(v -> resetTimer());
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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, intervalLabels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerInterval.setAdapter(adapter);
    }

    private void loadDefaults() {
        if (!isRunning) {
            binding.chkSpeak.setChecked(prefManager.isDefaultSpeakEnabled());
            int defaultInterval = prefManager.getDefaultSpeakingInterval();
            int selectedIndex = 1; // Default to 5s if match fails
            for (int i = 0; i < intervalSecondsValues.length; i++) {
                if (intervalSecondsValues[i] == defaultInterval) {
                    selectedIndex = i;
                    break;
                }
            }
            binding.spinnerInterval.setSelection(selectedIndex);
        }
    }

    private int getSelectedIntervalSeconds() {
        int index = binding.spinnerInterval.getSelectedItemPosition();
        if (index >= 0 && index < intervalSecondsValues.length) {
            return intervalSecondsValues[index];
        }
        return 5;
    }

    private void startTimer() {
        elapsedTimeSeconds = 0;
        isRunning = true;
        isPaused = false;

        updateTimerDisplay();
        updateUIState();

        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void pauseTimer() {
        isPaused = true;
        updateUIState();
    }

    private void resumeTimer() {
        isPaused = false;
        updateUIState();
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void resetTimer() {
        isRunning = false;
        isPaused = false;
        elapsedTimeSeconds = 0;

        timerHandler.removeCallbacks(timerRunnable);
        binding.tvTimerDisplay.setText(getString(R.string.default_timer_display));

        updateUIState();
        loadDefaults();
    }

    private void updateTimerDisplay() {
        int h = elapsedTimeSeconds / 3600;
        int m = (elapsedTimeSeconds % 3600) / 60;
        int s = elapsedTimeSeconds % 60;
        binding.tvTimerDisplay.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s));
    }

    private void updateUIState() {
        if (!isRunning) {
            binding.btnStart.setVisibility(View.VISIBLE);
            binding.btnPause.setVisibility(View.GONE);
            binding.btnResume.setVisibility(View.GONE);
            binding.btnReset.setEnabled(false);
        } else {
            binding.btnStart.setVisibility(View.GONE);
            if (isPaused) {
                binding.btnPause.setVisibility(View.GONE);
                binding.btnResume.setVisibility(View.VISIBLE);
            } else {
                binding.btnPause.setVisibility(View.VISIBLE);
                binding.btnResume.setVisibility(View.GONE);
            }
            binding.btnReset.setEnabled(true);
        }
    }

    private void speakTime(int secondsElapsed) {
        int h = secondsElapsed / 3600;
        int m = (secondsElapsed % 3600) / 60;
        int s = secondsElapsed % 60;

        StringBuilder text = new StringBuilder();
        if (h > 0) {
            text.append(h).append(h == 1 ? " hour " : " hours ");
        }
        if (m > 0) {
            text.append(m).append(m == 1 ? " minute " : " minutes ");
        }
        if (s > 0 || text.length() == 0) {
            text.append(s).append(s == 1 ? " second " : " seconds ");
        }
        //text.append("elapsed");
        ttsManager.speak(text.toString());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_ELAPSED_TIME, elapsedTimeSeconds);
        outState.putBoolean(KEY_IS_RUNNING, isRunning);
        outState.putBoolean(KEY_IS_PAUSED, isPaused);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDefaults();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timerHandler.removeCallbacks(timerRunnable);
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
        binding = null;
    }
}
