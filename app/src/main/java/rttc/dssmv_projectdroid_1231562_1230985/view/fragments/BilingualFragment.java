package rttc.dssmv_projectdroid_1231562_1230985.view.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Locale;

import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.model.User;
import rttc.dssmv_projectdroid_1231562_1230985.utils.SessionManager;
import rttc.dssmv_projectdroid_1231562_1230985.viewmodel.BilingualViewModel;

public class BilingualFragment extends Fragment {

    private static final int REQUEST_RECORD_AUDIO = 1001;
    private static final int REQ_CODE_SPEECH_INPUT = 1002;

    private BilingualViewModel viewModel;
    private TextToSpeech tts;
    private boolean ttsReady = false;

    private String[] languages = {"Português", "English", "Español", "Français", "日本語", "中文", "Deutsch"};
    private String[] languageCodes = {"pt", "en", "es", "fr", "ja", "zh", "de"};

    private AutoCompleteTextView autoCompleteLangA, autoCompleteLangB;
    private TextView textLangA, textLangB;
    private FloatingActionButton fabMicA, fabMicB;
    private ImageButton btnSwapLangs;

    private SessionManager sessionManager;
    private String langA = "pt";
    private String langB = "en";
    private String currentSourceLang;
    private String currentTargetLang;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(this).get(BilingualViewModel.class);
        sessionManager = new SessionManager(requireContext());

        return inflater.inflate(R.layout.fragment_bilingual, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        autoCompleteLangA = view.findViewById(R.id.autoComplete_lang_a);
        autoCompleteLangB = view.findViewById(R.id.autoComplete_lang_b);
        textLangA = view.findViewById(R.id.text_lang_a);
        textLangB = view.findViewById(R.id.text_lang_b);
        fabMicA = view.findViewById(R.id.fab_mic_a);
        fabMicB = view.findViewById(R.id.fab_mic_b);
        btnSwapLangs = view.findViewById(R.id.btn_swap_langs);

        setupTts();
        setupLanguageMenus();
        setupObservers();

        fabMicA.setOnClickListener(v -> {
            currentSourceLang = langA;
            currentTargetLang = langB;
            startSpeechToText();
        });

        fabMicB.setOnClickListener(v -> {
            currentSourceLang = langB;
            currentTargetLang = langA;
            startSpeechToText();
        });

        btnSwapLangs.setOnClickListener(v -> swapLanguages());
    }

    private void setupTts() {
        tts = new TextToSpeech(requireContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                ttsReady = true;
                tts.setLanguage(new Locale(langA));
            } else {
                Toast.makeText(getContext(), "TTS initialization failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupLanguageMenus() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, languages);

        autoCompleteLangA.setAdapter(adapter);
        User user = sessionManager.getUser();
        langA = (user != null && user.getPreferredLanguage() != null) ? user.getPreferredLanguage() : "pt";
        autoCompleteLangA.setText(getLanguageNameFromCode(langA), false);

        autoCompleteLangA.setOnItemClickListener((parent, view, position, id) -> {
            langA = languageCodes[position];
        });

        autoCompleteLangB.setAdapter(adapter);
        langB = langA.equals("pt") ? "en" : "pt";
        autoCompleteLangB.setText(getLanguageNameFromCode(langB), false);

        autoCompleteLangB.setOnItemClickListener((parent, view, position, id) -> {
            langB = languageCodes[position];
        });
    }

    private void startSpeechToText() {
        if (!checkAudioPermission()) {
            return;
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentSourceLang);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getContext(), "Sorry, but your device does not support voice recognition.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result != null && !result.isEmpty()) {
                    String spokenText = result.get(0);
                    viewModel.translateText(spokenText, currentSourceLang, currentTargetLang);
                }
            }
        }
    }

    private void setupObservers() {
        viewModel.getTextForLangA().observe(getViewLifecycleOwner(), text -> {
            textLangA.setText(text);
        });

        viewModel.getTextForLangB().observe(getViewLifecycleOwner(), text -> {
            textLangB.setText(text);
        });

        viewModel.getTtsRequest().observe(getViewLifecycleOwner(), request -> {
            if (request != null) {
                speak(request.getText(), request.getLocale());
            }
        });

        viewModel.getStatusMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void swapLanguages() {
        String tempLang = langA;
        langA = langB;
        langB = tempLang;

        String textA = autoCompleteLangA.getText().toString();
        String textB = autoCompleteLangB.getText().toString();
        autoCompleteLangA.setText(textB, false);
        autoCompleteLangB.setText(textA, false);

        String contentA = textLangA.getText().toString();
        String contentB = textLangB.getText().toString();
        textLangA.setText(contentB);
        textLangB.setText(contentA);
    }

    private void speak(String text, Locale locale) {
        if (ttsReady && text != null && !text.isEmpty()) {
            tts.setLanguage(locale);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_conversation");
        }
    }

    private String getLanguageNameFromCode(String langCode) {
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].trim().equalsIgnoreCase(langCode.trim())) {
                return languages[i];
            }
        }
        return languages[0];
    }

    private boolean checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Permission granted. Press microphone", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Microphone permission negated.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}