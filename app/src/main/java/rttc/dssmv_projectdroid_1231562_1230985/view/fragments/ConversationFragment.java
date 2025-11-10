package rttc.dssmv_projectdroid_1231562_1230985.view.fragments;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import java.util.Locale;
import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.model.Conversation;
import rttc.dssmv_projectdroid_1231562_1230985.model.User;
import rttc.dssmv_projectdroid_1231562_1230985.utils.SessionManager;
import rttc.dssmv_projectdroid_1231562_1230985.viewmodel.ConversationHistoryViewModel;
import rttc.dssmv_projectdroid_1231562_1230985.viewmodel.ConversationViewModel;

public class ConversationFragment extends Fragment {

    private static final int REQUEST_RECORD_AUDIO = 1001;
    private ConversationViewModel viewModel;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private String translatedText = "";
    private ConversationHistoryViewModel historyViewModel;
    private String pendingOriginalText = null;
    private String pendingTranslatedText = null;
    private String pendingDetectedLanguage = null;
    private TextView txtRecognized, txtTranslated, txtOriginalLang;
    private AutoCompleteTextView autoCompleteTargetLanguage;
    private SessionManager sessionManager;
    private String targetLang = "en";
    private final String[] languages = {"Português", "English", "Español", "Français", "日本語", "中文", "Deutsch"};
    private final String[] languageCodes = {"pt", "en", "es", "fr", "ja", "zh", "de"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(this).get(ConversationViewModel.class);
        historyViewModel = new ViewModelProvider(this).get(ConversationHistoryViewModel.class);
        sessionManager = new SessionManager(requireContext());

        return inflater.inflate(R.layout.fragment_conversation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Button btnSpeak = view.findViewById(R.id.btnStartListening);
        Button btnPlay = view.findViewById(R.id.btnPlayTranslation);
        autoCompleteTargetLanguage = view.findViewById(R.id.autoCompleteTargetLanguage);

        txtRecognized = view.findViewById(R.id.txtRecognized);
        txtTranslated = view.findViewById(R.id.txtTranslated);
        txtOriginalLang = view.findViewById(R.id.txtOriginalLang);

        setupTts();
        setupTargetLanguageMenu();
        setupObservers();

        btnSpeak.setOnClickListener(v -> {
            if (checkAudioPermission()) {
                viewModel.startListening(targetLang);
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
    private void setupTargetLanguageMenu() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                languages
        );
        autoCompleteTargetLanguage.setAdapter(adapter);
        User user = sessionManager.getUser();
        String preferredLangCode = "en";
        if (user != null && user.getPreferredLanguage() != null) {
            preferredLangCode = user.getPreferredLanguage();
        }
        String preferredLangName = getLanguageNameFromCode(preferredLangCode);
        autoCompleteTargetLanguage.setText(preferredLangName, false);
        targetLang = preferredLangCode;
        autoCompleteTargetLanguage.setOnItemClickListener((parent, view, position, id) -> {
            targetLang = languageCodes[position];
        });
    }
    private String getLanguageNameFromCode(String langCode) {
        if (langCode == null) {
            return languages[1];
        }
        String trimmedLangCode = langCode.trim();
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equalsIgnoreCase(trimmedLangCode)) {
                return languages[i];
            }
        }
        return languages[1];
    }

    private void setupObservers() {
        viewModel.recognizedText.observe(getViewLifecycleOwner(), text -> {
            txtRecognized.setText(text);
            pendingOriginalText = text;
            tryToSaveConversation();
        });

        viewModel.translatedText.observe(getViewLifecycleOwner(), translated -> {
            txtTranslated.setText(translated);
            translatedText = translated != null ? translated : "";
            pendingTranslatedText = translated;
            tryToSaveConversation();
        });

        viewModel.originalLanguage.observe(getViewLifecycleOwner(), lang -> {
            if (lang != null && !lang.isEmpty()) {
                txtOriginalLang.setText("Detetado: " + lang.toUpperCase());
                pendingDetectedLanguage = lang;
                tryToSaveConversation();
                if (ttsReady) {
                    Locale locale = new Locale(lang);
                    tts.setLanguage(locale);
                }
            }
        });

        viewModel.statusMessage.observe(getViewLifecycleOwner(), message -> {
            txtRecognized.setText(message);
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), "Permission granted. Press Microphone.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Microphone permission negated.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void tryToSaveConversation() {
        if (pendingOriginalText != null &&
                pendingTranslatedText != null &&
                pendingDetectedLanguage != null) {

            String sourceLang = pendingDetectedLanguage;
            if (sourceLang.isEmpty()) {
                sourceLang = "auto";
            }

            try {
                Conversation conversation = new Conversation(
                        null,
                        pendingOriginalText,
                        pendingTranslatedText,
                        sourceLang,
                        targetLang
                );
                historyViewModel.saveConversation(conversation, requireContext());

            } catch (Exception e) {
                Toast.makeText(getContext(), "Error saving conversation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            pendingOriginalText = null;
            pendingTranslatedText = null;
            pendingDetectedLanguage = null;
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