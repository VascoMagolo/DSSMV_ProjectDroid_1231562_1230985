package rttc.dssmv_projectdroid_1231562_1230985.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.view.MainActivity;

public class ConversationFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conversation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Button btnSpeak = view.findViewById(R.id.btnStartListening);
        Spinner spinner = view.findViewById(R.id.spinnerTargetLanguage);

        // Lista de idiomas
        String[] languages = {"English", "Portuguese", "Spanish", "French"};
        String[] languageCodes = {"en", "pt", "es", "fr"};

        // Adapter do Spinner
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
    }

    public void updateRecognizedText(String text) {
        TextView txt = getView().findViewById(R.id.txtRecognized);
        if (txt != null) txt.setText(text);
    }

    public void updateTranslatedText(String translated) {
        TextView txt = getView().findViewById(R.id.txtTranslated);
        if (txt != null) txt.setText(translated);
    }
}
