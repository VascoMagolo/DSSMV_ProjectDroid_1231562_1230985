package rttc.dssmv_projectdroid_1231562_1230985.view.fragments;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Locale;

import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.model.GenericPhrase;
import rttc.dssmv_projectdroid_1231562_1230985.model.User;
import rttc.dssmv_projectdroid_1231562_1230985.utils.SessionManager;
import rttc.dssmv_projectdroid_1231562_1230985.view.adapters.PhraseAdapter;
import rttc.dssmv_projectdroid_1231562_1230985.viewmodel.PhraseViewModel;

public class PhrasesFragment extends Fragment {

    private PhraseViewModel viewModel;
    private RecyclerView recyclerView;
    private PhraseAdapter adapter;
    private TextToSpeech tts;

    private AutoCompleteTextView autoCompleteSourceLanguage;
    private AutoCompleteTextView autoCompleteTargetLanguage;
    private FloatingActionButton fabAddPhrase;

    private CardView cardTranslation;
    private TextView textOriginalPhrase;
    private TextView textTranslatedPhrase;
    private Button btnSpeakTranslation;

    private String targetLang = "en";
    private String currentTranslatedPhrase = "";
    private SessionManager sessionManager;
    private String[] languages = {"Português", "English", "Español", "Français", "日本語", "中文", "Deutsch"};
    private String[] languageCodes = {"pt", "en", "es", "fr", "ja", "zh", "de"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phrases, container, false);

        viewModel = new ViewModelProvider(this).get(PhraseViewModel.class);
        sessionManager = new SessionManager(requireContext());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerViewPhrases);
        autoCompleteSourceLanguage = view.findViewById(R.id.autoCompleteSourceLanguage);
        autoCompleteTargetLanguage = view.findViewById(R.id.autoCompleteTargetLanguage);
        fabAddPhrase = view.findViewById(R.id.fab_add_phrase);

        cardTranslation = view.findViewById(R.id.cardTranslation);
        textOriginalPhrase = view.findViewById(R.id.textOriginalPhrase);
        textTranslatedPhrase = view.findViewById(R.id.textTranslatedPhrase);
        btnSpeakTranslation = view.findViewById(R.id.btnSpeakTranslation);
        setupTTS();
        setupRecyclerView();
        setupSourceLanguageMenu();
        setupTargetLanguageMenu();
        setupTranslationCard();
        setupObservers();

        fabAddPhrase.setOnClickListener(v -> showAddPhraseDialog());
        String initialLang = "pt";
        User user = sessionManager.getUser();
        if (user != null && user.getPreferredLanguage() != null && !user.getPreferredLanguage().isEmpty()) {
            initialLang = user.getPreferredLanguage();
        }

        autoCompleteSourceLanguage.setText(getLanguageNameFromCode(initialLang), false);
        viewModel.loadAllPhrases(requireContext(), initialLang);
    }

    private void setupTTS() {
        tts = new TextToSpeech(getContext(), status -> {
            if (status != TextToSpeech.ERROR) {
                Locale targetLocale = new Locale(targetLang);
                int result = tts.setLanguage(targetLocale);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
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
        adapter.setOnDeleteClickListener(phrase -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete phrase")
                    .setMessage("Are you sure that you want to delete the phrase: \"" + phrase.getText() + "\"?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete", (dialog, which) -> {
                        viewModel.deleteUserPhrase(phrase, requireContext());
                    })
                    .show();
        });
    }

    private void setupSourceLanguageMenu() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                languages
        );
        autoCompleteSourceLanguage.setAdapter(spinnerAdapter);

        autoCompleteSourceLanguage.setOnItemClickListener((parent, view, position, id) -> {
            String selectedLanguage = languageCodes[position];
            viewModel.loadGenericPhrases(selectedLanguage);
        });
    }

    private void setupTargetLanguageMenu() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                languages
        );
        autoCompleteTargetLanguage.setAdapter(spinnerAdapter);

        User user = sessionManager.getUser();
        String preferredLangCode = "en";
        if (user != null && user.getPreferredLanguage() != null && !user.getPreferredLanguage().isEmpty()) {
            preferredLangCode = user.getPreferredLanguage();
        }

        String preferredLangName = getLanguageNameFromCode(preferredLangCode);
        autoCompleteTargetLanguage.setText(preferredLangName, false);
        targetLang = preferredLangCode;

        autoCompleteTargetLanguage.setOnItemClickListener((parent, view, position, id) -> {
            targetLang = languageCodes[position];

            if (tts != null) {
                Locale targetLocale = new Locale(targetLang);
                int result = tts.setLanguage(targetLocale);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.setLanguage(Locale.US);
                }
            }
            if (cardTranslation.getVisibility() == View.VISIBLE) {
                String original = textOriginalPhrase.getText().toString();
                if (!original.isEmpty()) {
                    viewModel.translatePhrase(original, targetLang);
                }
            }
        });
    }

    private void setupTranslationCard() {
        btnSpeakTranslation.setOnClickListener(v -> speakTranslation(currentTranslatedPhrase));
    }

    private String getLanguageNameFromCode(String langCode) {
        if (langCode == null) return languages[0];
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equalsIgnoreCase(langCode.trim())) {
                return languages[i];
            }
        }
        return languages[0];
    }

    private void speakTranslation(String translatedText) {
        if (tts != null && translatedText != null && !translatedText.isEmpty()) {
            tts.speak(translatedText, TextToSpeech.QUEUE_FLUSH, null, "phrase_tts");
        }
    }

    private void setupObservers() {
        viewModel.getAllPhrases().observe(getViewLifecycleOwner(), phrases -> {
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
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.translatedText.observe(getViewLifecycleOwner(), translated -> {
            textTranslatedPhrase.setText(translated);
            currentTranslatedPhrase = translated;
        });
    }
    private void showAddPhraseDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_phrases, null, false);

        TextInputEditText edtText = dialogView.findViewById(R.id.edt_new_phrase_text);
        TextInputEditText edtCategory = dialogView.findViewById(R.id.edt_new_phrase_category);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add personal phrase")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("save", (dialog, which) -> {
                    String text = edtText.getText().toString().trim();
                    String category = edtCategory.getText().toString().trim();

                    if (text.isEmpty()) {
                        Toast.makeText(getContext(), "Cannot be empty.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (category.isEmpty()) {
                        category = "Personal";
                    }

                    GenericPhrase newPhrase = new GenericPhrase();
                    newPhrase.setText(text);
                    newPhrase.setCategory(category);

                    viewModel.saveUserPhrase(newPhrase, requireContext());
                })
                .show();
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