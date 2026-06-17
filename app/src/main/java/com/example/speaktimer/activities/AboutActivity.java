package com.example.speaktimer.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.speaktimer.BuildConfig;
import com.example.speaktimer.R;
import com.example.speaktimer.databinding.ActivityAboutBinding;

public class AboutActivity extends AppCompatActivity {
    private ActivityAboutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        String buildInfo = 
                "Timestamp: " + BuildConfig.BUILD_TIMESTAMP + "\n" +
                "Commit: " + BuildConfig.GIT_SHA + "\n" +
                "Full SHA: " + BuildConfig.GIT_SHA_FULL + "\n\n" +
                getString(R.string.about_desc);

        binding.tvBuildInfo.setText(buildInfo);
    }
}
