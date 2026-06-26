package com.example.speaktimer.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.speaktimer.R;
import com.example.speaktimer.databinding.FragmentCountdownBinding;
import com.example.speaktimer.settings.PreferenceManager;
import com.example.speaktimer.tts.TextToSpeechManager;
import com.example.speaktimer.utils.NotificationHelper;
import java.util.Locale;

public class CountdownFragment extends Fragment {

    private FragmentCountdownBinding binding;
    private TextToSpeechManager ttsManager;
    private PreferenceManager prefManager;

    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;
    private boolean isPaused = false;
    private int totalDurationSeconds = 0;
    private int remainingTimeSeconds = 0;

    private static final String KEY_REMAINING_TIME = "remaining_time";
    private static final String KEY_TOTAL_DURATION = "total_duration";
    private static final String KEY_IS_RUNNING = "is_running";
    private static final String KEY_IS_PAUSED = "is_paused";

    // Speaking intervals in seconds mapping
    private final int[] intervalSecondsValues = {1, 5, 10, 15, 30, 60};

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning && !isPaused) {
                if (remainingTimeSeconds > 0) {
                    remainingTimeSeconds--;
                    updateTimerDisplay();

                    if (remainingTimeSeconds > 0 && binding.chkSpeak.isChecked()) {
                        if (remainingTimeSeconds % getSelectedIntervalSeconds() == 0) {
                            speakTime(remainingTimeSeconds);
                        }
                    }

                    if (remainingTimeSeconds <= 0) {
                        onTimerFinished();
                    } else {
                        timerHandler.postDelayed(this, 1000);
                    }
                }
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCountdownBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ttsManager = new TextToSpeechManager(requireContext());
        prefManager = new PreferenceManager(requireContext());

        setupIntervalSpinner();
        loadDefaults();

        // Restore state if available
        if (savedInstanceState != null) {
            remainingTimeSeconds = savedInstanceState.getInt(KEY_REMAINING_TIME);
            totalDurationSeconds = savedInstanceState.getInt(KEY_TOTAL_DURATION);
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
        String hStr = binding.etHours.getText().toString().trim();
        String mStr = binding.etMinutes.getText().toString().trim();
        String sStr = binding.etSeconds.getText().toString().trim();

        int h = hStr.isEmpty() ? 0 : Integer.parseInt(hStr);
        int m = mStr.isEmpty() ? 0 : Integer.parseInt(mStr);
        int s = sStr.isEmpty() ? 0 : Integer.parseInt(sStr);

        if (h < 0 || m < 0 || s < 0) {
            Toast.makeText(requireContext(), getString(R.string.error_invalid_input), Toast.LENGTH_SHORT).show();
            return;
        }

        totalDurationSeconds = h * 3600 + m * 60 + s;
        if (totalDurationSeconds <= 0) {
            Toast.makeText(requireContext(), getString(R.string.error_empty_input), Toast.LENGTH_SHORT).show();
            return;
        }

        remainingTimeSeconds = totalDurationSeconds;
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
        totalDurationSeconds = 0;
        remainingTimeSeconds = 0;

        timerHandler.removeCallbacks(timerRunnable);
        binding.tvTimerDisplay.setText(getString(R.string.default_timer_display));
        binding.etHours.setText("00");
        binding.etMinutes.setText("00");
        binding.etSeconds.setText("00");

        updateUIState();
        loadDefaults();
    }

    private void updateTimerDisplay() {
        int h = remainingTimeSeconds / 3600;
        int m = (remainingTimeSeconds % 3600) / 60;
        int s = remainingTimeSeconds % 60;
        binding.tvTimerDisplay.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s));
    }

    private void updateUIState() {
        if (!isRunning) {
            // Inputs editable
            binding.tilHours.setEnabled(true);
            binding.tilMinutes.setEnabled(true);
            binding.tilSeconds.setEnabled(true);

            binding.btnStart.setVisibility(View.VISIBLE);
            binding.btnPause.setVisibility(View.GONE);
            binding.btnResume.setVisibility(View.GONE);
            binding.btnReset.setEnabled(false);
        } else {
            // Inputs disabled during run
            binding.tilHours.setEnabled(false);
            binding.tilMinutes.setEnabled(false);
            binding.tilSeconds.setEnabled(false);

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

    private void speakTime(int secondsRemaining) {
        int h = secondsRemaining / 3600;
        int m = (secondsRemaining % 3600) / 60;
        int s = secondsRemaining % 60;

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
        //text.append("remaining");
        ttsManager.speak(text.toString());
    }

    private void onTimerFinished() {
        isRunning = false;
        isPaused = false;
        updateUIState();

        if (binding.chkSpeak.isChecked()) {
            ttsManager.speak(getString(R.string.tts_finished_speech));
        }

        NotificationHelper.showFinishedNotification(requireContext());
        showFinishedDialog();
    }

    private void showFinishedDialog() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.timer_finished_title)
                .setMessage(R.string.timer_finished_message)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_ok, (dialog, which) -> resetTimer())
                .show();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_REMAINING_TIME, remainingTimeSeconds);
        outState.putInt(KEY_TOTAL_DURATION, totalDurationSeconds);
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
