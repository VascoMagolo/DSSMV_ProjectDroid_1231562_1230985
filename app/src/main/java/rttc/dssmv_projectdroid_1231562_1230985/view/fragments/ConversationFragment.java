package rttc.dssmv_projectdroid_1231562_1230985.view.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import java.util.Locale;
import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.model.Conversation;
import rttc.dssmv_projectdroid_1231562_1230985.viewmodel.ConversationHistoryViewModel;
import rttc.dssmv_projectdroid_1231562_1230985.viewmodel.ConversationViewModel;

public class ConversationFragment extends Fragment {

    private static final int REQUEST_RECORD_AUDIO = 1001;
    private ConversationViewModel viewModel;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private String translatedText = "";
    private ConversationHistoryViewModel historyViewModel;
    private String currentDetectedLanguage = "auto";

    private TextView txtRecognized, txtTranslated, txtOriginalLang;
    private Spinner spinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(this).get(ConversationViewModel.class);
        historyViewModel = new ViewModelProvider(this).get(ConversationHistoryViewModel.class);
        return inflater.inflate(R.layout.fragment_conversation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Button btnSpeak = view.findViewById(R.id.btnStartListening);
        Button btnPlay = view.findViewById(R.id.btnPlayTranslation);
        spinner = view.findViewById(R.id.spinnerTargetLanguage);


        txtRecognized = view.findViewById(R.id.txtRecognized);
        txtTranslated = view.findViewById(R.id.txtTranslated);
        txtOriginalLang = view.findViewById(R.id.txtOriginalLang);

        setupTts();
        setupSpinner(spinner);
        setupObservers();

        btnSpeak.setOnClickListener(v -> {
            if (checkAudioPermission()) {
                String currentTargetLanguage = (String) spinner.getTag();
                viewModel.startListening(currentTargetLanguage);
            }
        });

        btnPlay.setOnClickListener(v -> {
            if (ttsReady && !translatedText.isEmpty()) {
                tts.speak(translatedText, TextToSpeech.QUEUE_FLUSH, null, "tts1");
            }
        });
    }

    private void setupTts() {
        tts = new TextToSpeech(requireContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                ttsReady = true;
            }
        });
    }

    private void setupSpinner(Spinner spinner) {
        String[] languages = {"English", "Portuguese", "Spanish", "French", "Japanese", "Chinese", "German"};
        String[] languageCodes = {"en", "pt", "es", "fr", "ja", "zh", "de"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setTag(languageCodes[0]);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinner.setTag(languageCodes[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupObservers() {
        viewModel.recognizedText.observe(getViewLifecycleOwner(), text -> {
            txtRecognized.setText(text);
        });

        viewModel.translatedText.observe(getViewLifecycleOwner(), translated -> {
            txtTranslated.setText(translated);
            translatedText = translated != null ? translated : "";

            String recognizedNow = txtRecognized.getText() != null ? txtRecognized.getText().toString() : "";
            if (!translatedText.isEmpty() && !recognizedNow.isEmpty()) {
                String originalText = recognizedNow;
                String sourceLang = currentDetectedLanguage;
                if (sourceLang == null || sourceLang.isEmpty()) {
                    sourceLang = "auto";
                }

                Object tag = spinner != null ? spinner.getTag() : null;
                String targetLang = tag instanceof String ? (String) tag : "en";

                try {
                    Conversation conversation = new Conversation(
                            null,
                            originalText,
                            translatedText,
                            sourceLang,
                            targetLang
                    );
                    historyViewModel.saveConversation(conversation, requireContext());
                } catch (Exception e) {

                }
            }
        });
        viewModel.originalLanguage.observe(getViewLifecycleOwner(), lang -> {
            if (lang != null && !lang.isEmpty()) {
                txtOriginalLang.setText("Detected: " + lang.toUpperCase());
                currentDetectedLanguage = lang;
            }
        });

        viewModel.statusMessage.observe(getViewLifecycleOwner(), message -> {
            txtRecognized.setText(message);
        });
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
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Permissão concedida. Pressione o microfone.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Permissões de microfone negadas.", Toast.LENGTH_SHORT).show();
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