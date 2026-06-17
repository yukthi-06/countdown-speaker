package com.example.speaktimer.tts;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;
import com.example.speaktimer.R;
import java.util.Locale;

public class TextToSpeechManager implements TextToSpeech.OnInitListener {
    private static final String TAG = "TextToSpeechManager";
    private TextToSpeech tts;
    private boolean isInitialized = false;
    private final Context context;

    public TextToSpeechManager(Context context) {
        this.context = context.getApplicationContext();
        try {
            tts = new TextToSpeech(this.context, this);
        } catch (Exception e) {
            Log.e(TAG, "Error creating TextToSpeech instance", e);
            Toast.makeText(this.context, this.context.getString(R.string.error_tts_unavailable), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            if (tts != null) {
                int result = tts.setLanguage(Locale.getDefault());
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language is not supported or missing data.");
                    Toast.makeText(context, context.getString(R.string.error_tts_unavailable), Toast.LENGTH_LONG).show();
                } else {
                    isInitialized = true;
                }
            }
        } else {
            Log.e(TAG, "TextToSpeech initialization failed.");
            Toast.makeText(context, context.getString(R.string.error_tts_unavailable), Toast.LENGTH_LONG).show();
        }
    }

    public boolean isReady() {
        return isInitialized && tts != null;
    }

    public void speak(String text) {
        if (isReady()) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "SpeakTimerAnnounceID");
        } else {
            Log.w(TAG, "TTS not ready to speak.");
        }
    }

    public void stop() {
        if (tts != null) {
            tts.stop();
        }
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        isInitialized = false;
    }
}
