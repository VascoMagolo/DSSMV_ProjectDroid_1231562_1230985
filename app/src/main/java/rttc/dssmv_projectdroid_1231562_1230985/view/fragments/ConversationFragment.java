package rttc.dssmv_projectdroid_1231562_1230985.view.fragments;

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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.Locale;

import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.view.MainActivity;

public class ConversationFragment extends Fragment {

    private TextToSpeech tts;
    private String translatedText = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conversation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Button btnSpeak = view.findViewById(R.id.btnStartListening);
        Button btnPlay = view.findViewById(R.id.btnPlayTranslation);
        Spinner spinner = view.findViewById(R.id.spinnerTargetLanguage);

        tts = new TextToSpeech(getContext(), status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.US); // default, será mudado dinamicamente se quiser
            }
        });

        String[] languages = {"English", "Portuguese", "Spanish", "French"};
        String[] languageCodes = {"en", "pt", "es", "fr"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        final String[] selectedLanguageCode = {languageCodes[0]}; // default

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLanguageCode[0] = languageCodes[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSpeak.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).startSpeechListening(selectedLanguageCode[0]);
            }
        });

        btnPlay.setOnClickListener(v -> {
            if (!translatedText.isEmpty()) {
                tts.speak(translatedText, TextToSpeech.QUEUE_FLUSH, null, "tts1");
            }
        });
    }

    public void updateRecognizedText(String text) {
        TextView txt = getView().findViewById(R.id.txtRecognized);
        if (txt != null) txt.setText(text);
    }

    public void updateTranslatedText(String translated) {
        TextView txt = getView().findViewById(R.id.txtTranslated);
        if (txt != null) txt.setText(translated);
        translatedText = translated; // salva para TTS
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
