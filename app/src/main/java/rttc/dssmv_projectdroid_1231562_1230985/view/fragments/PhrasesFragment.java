package rttc.dssmv_projectdroid_1231562_1230985.view.fragments;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.repository.TranslationRepository;
import rttc.dssmv_projectdroid_1231562_1230985.view.adapters.PhraseAdapter;
import rttc.dssmv_projectdroid_1231562_1230985.viewmodel.PhraseViewModel;

public class PhrasesFragment extends Fragment {

    private PhraseViewModel viewModel;
    private RecyclerView recyclerView;
    private PhraseAdapter adapter;
    private Spinner spinnerLanguage;
    private Spinner spinnerTargetLanguage;
    private TextToSpeech tts;
    private TranslationRepository translationRepository;


    private CardView cardTranslation;
    private TextView textOriginalPhrase;
    private TextView textTranslatedPhrase;
    private Button btnSpeakTranslation;

    private String currentTranslatedPhrase = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phrases, container, false);

        viewModel = new ViewModelProvider(this).get(PhraseViewModel.class);
        translationRepository = new TranslationRepository();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewPhrases);
        spinnerLanguage = view.findViewById(R.id.spinnerLanguage);
        spinnerTargetLanguage = view.findViewById(R.id.spinnerTargetLanguage);

        cardTranslation = view.findViewById(R.id.cardTranslation);
        textOriginalPhrase = view.findViewById(R.id.textOriginalPhrase);
        textTranslatedPhrase = view.findViewById(R.id.textTranslatedPhrase);
        btnSpeakTranslation = view.findViewById(R.id.btnSpeakTranslation);

        setupTTS();
        setupRecyclerView();
        setupSourceSpinner();
        setupTargetSpinner();
        setupTranslationCard();
        setupObservers();

        viewModel.loadPhrases("pt");
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
            viewModel.translatePhrase(phrase.getText(), getSelectedTargetLanguage());
        });

    }

    private void setupSourceSpinner() {
        String[] languages = {"Português", "English", "Español", "Français", "日本語", "中文", "Deutsch"};
        String[] languageCodes = {"pt", "en", "es", "fr", "ja", "zh", "de"};

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                languages
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(spinnerAdapter);

        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLanguage = languageCodes[position];
                viewModel.loadPhrases(selectedLanguage);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupTargetSpinner() {
        String[] languages = {"English", "Português", "Español", "Français", "日本語", "中文", "Deutsch"};
        String[] languageCodes = {"en", "pt", "es", "fr", "ja", "zh", "de"};

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                languages
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTargetLanguage.setAdapter(spinnerAdapter);

        spinnerTargetLanguage.setSelection(0);
    }

    private void setupTranslationCard() {
        btnSpeakTranslation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakTranslation(currentTranslatedPhrase);
            }
        });
    }

    private String getSelectedTargetLanguage() {
        String[] languageCodes = {"en", "pt", "es", "fr", "ja", "zh", "de"};
        int position = spinnerTargetLanguage.getSelectedItemPosition();
        return languageCodes[position];
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