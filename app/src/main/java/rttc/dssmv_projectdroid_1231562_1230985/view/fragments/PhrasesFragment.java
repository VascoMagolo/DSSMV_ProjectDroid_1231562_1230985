package rttc.dssmv_projectdroid_1231562_1230985.view.fragments;
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
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.Locale;
import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.model.User;
import rttc.dssmv_projectdroid_1231562_1230985.repository.TranslationRepository;
import rttc.dssmv_projectdroid_1231562_1230985.utils.SessionManager;
import rttc.dssmv_projectdroid_1231562_1230985.view.adapters.PhraseAdapter;
import rttc.dssmv_projectdroid_1231562_1230985.viewmodel.PhraseViewModel;

public class PhrasesFragment extends Fragment {

    private PhraseViewModel viewModel;
    private RecyclerView recyclerView;
    private PhraseAdapter adapter;
    private TextToSpeech tts;

    private AutoCompleteTextView autoCompleteLanguage;
    private AutoCompleteTextView autoCompleteTargetLanguage;
    private SessionManager sessionManager;
    private String targetLang = "en";
    private String[] languages = {"Português", "English", "Español", "Français", "日本語", "中文", "Deutsch"};
    private String[] languageCodes = {"pt", "en", "es", "fr", "ja", "zh", "de"};
    private CardView cardTranslation;
    private TextView textOriginalPhrase;
    private TextView textTranslatedPhrase;
    private MaterialButton btnSpeakTranslation;
    private String currentTranslatedPhrase = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phrases, container, false);

        viewModel = new ViewModelProvider(this).get(PhraseViewModel.class);
        sessionManager = new SessionManager(requireContext());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewPhrases);
        autoCompleteLanguage = view.findViewById(R.id.autoCompleteLanguage);
        autoCompleteTargetLanguage = view.findViewById(R.id.autoCompleteTargetLanguage);

        cardTranslation = view.findViewById(R.id.cardTranslation);
        textOriginalPhrase = view.findViewById(R.id.textOriginalPhrase);
        textTranslatedPhrase = view.findViewById(R.id.textTranslatedPhrase);
        btnSpeakTranslation = view.findViewById(R.id.btnSpeakTranslation);

        setupTTS();
        setupRecyclerView();
        setupLanguageMenus();
        setupTranslationCard();
        setupObservers();
        viewModel.loadPhrases(languageCodes[0]);
    }

    private void setupTTS() {
        tts = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.US);
                }
            }
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PhraseAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        adapter.setOnPhraseClickListener(phrase -> {
            textOriginalPhrase.setText(phrase.getText());
            textTranslatedPhrase.setText("Translating...");
            cardTranslation.setVisibility(View.VISIBLE);
            viewModel.translatePhrase(phrase.getText(), targetLang);
        });

    }
    private void setupLanguageMenus() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                languages
        );
        autoCompleteLanguage.setAdapter(adapter);
        autoCompleteTargetLanguage.setAdapter(adapter);
        autoCompleteLanguage.setText(languages[0], false);
        autoCompleteLanguage.setOnItemClickListener((parent, view, position, id) -> {
            String selectedLanguage = languageCodes[position];
            viewModel.loadPhrases(selectedLanguage);
        });
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

    private void setupTranslationCard() {
        btnSpeakTranslation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakTranslation(currentTranslatedPhrase);
            }
        });
    }

    private void speakTranslation(String translatedText) {
        if (tts != null && translatedText != null && !translatedText.isEmpty()) {
            tts.speak(translatedText, TextToSpeech.QUEUE_FLUSH, null, "phrase_tts");
        }
    }

    private void setupObservers() {
        viewModel.getPhrases().observe(getViewLifecycleOwner(), phrases -> {
            if (phrases != null && !phrases.isEmpty()) {
                adapter.updatePhrases(phrases);
            } else {
                adapter.updatePhrases(new ArrayList<>());
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.translatedText.observe(getViewLifecycleOwner(), translated -> {
            textTranslatedPhrase.setText(translated);
            currentTranslatedPhrase = translated;
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (tts != null) {
            tts.stop();
        }
    }
}