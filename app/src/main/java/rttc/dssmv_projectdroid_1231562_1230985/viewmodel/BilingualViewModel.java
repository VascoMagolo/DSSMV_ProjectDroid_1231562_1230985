package rttc.dssmv_projectdroid_1231562_1230985.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import rttc.dssmv_projectdroid_1231562_1230985.model.Translation;
import rttc.dssmv_projectdroid_1231562_1230985.model.TtsRequest;
import rttc.dssmv_projectdroid_1231562_1230985.repository.TranslationRepository;

/**
 * View model for {@link rttc.dssmv_projectdroid_1231562_1230985.view.fragments.BilingualFragment}
 * Manages two-way translation mode
 * Handles direct translation and saves result to history
 */
public class BilingualViewModel extends AndroidViewModel {

    private final TranslationRepository translationRepository;
    private final TranslationHistoryViewModel historyViewModel;

    // LiveData for text displayed in top half (A)
    private final MutableLiveData<String> _textForLangA = new MutableLiveData<>();
    public LiveData<String> getTextForLangA() { return _textForLangA; }

    // LiveData for text displayed in top half (B)
    private final MutableLiveData<String> _textForLangB = new MutableLiveData<>();
    public LiveData<String> getTextForLangB() { return _textForLangB; }


    // LiveData to request the View to speak using TTS
    private final MutableLiveData<TtsRequest> _ttsRequest = new MutableLiveData<>();
    public LiveData<TtsRequest> getTtsRequest() { return _ttsRequest; }

    private final MutableLiveData<String> _statusMessage = new MutableLiveData<>();
    public LiveData<String> getStatusMessage() { return _statusMessage; }

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() { return _errorMessage; }

    private final String langA = "pt";
    private final String langB = "en";


    public BilingualViewModel(@NonNull Application application) {
        super(application);
        translationRepository = new TranslationRepository();
        historyViewModel = new TranslationHistoryViewModel(application);
    }

    /**
     * Performs translation between 2 active language
     * It updates the opposite TextView with results and posts {@link TtsRequest}
     * to speak the translation
     * @param spokenText text captured from TTS
     * @param sourceLang language code of spoken text
     * @param targetLang language code to translate to
     */
    public void translateText(String spokenText, String sourceLang, String targetLang) {

        _statusMessage.postValue("Translating...");
        // updates the source text view
        if (sourceLang.equals(langA)) {
            _textForLangA.postValue(spokenText);
            _textForLangB.postValue("...");
        } else {
            _textForLangB.postValue(spokenText);
            _textForLangA.postValue("...");
        }
        // calls repository for translation
        translationRepository.translate(spokenText, sourceLang, targetLang, new TranslationRepository.TranslationCallback() {

            @Override
            public void onSuccess(String translatedText, String detectedSourceLang) {
                if (targetLang.equals(langB)) {
                    _textForLangB.postValue(translatedText);
                } else {
                    _textForLangA.postValue(translatedText);
                }

                // requests TTS of translation
                _ttsRequest.postValue(new TtsRequest(translatedText, targetLang));
                // saves the pair to history
                saveToHistory(spokenText, translatedText, sourceLang, targetLang);

                _statusMessage.postValue(null);
            }

            @Override
            public void onError(Exception e) {
                _errorMessage.postValue("Translation Error: " + e.getMessage());
                _statusMessage.postValue(null);
            }
        });
    }

    /**
     * Saves translation pair on supabase via {@link TranslationViewModel}
     * @param original source text
     * @param translated translated text
     * @param sourceLang source language code
     * @param targetLang target language code
     */
    private void saveToHistory(String original, String translated, String sourceLang, String targetLang) {
        Translation translation = new Translation(
                null,
                original,
                translated,
                sourceLang,
                targetLang
        );
        historyViewModel.saveTranslation(translation, getApplication().getApplicationContext());
    }
}