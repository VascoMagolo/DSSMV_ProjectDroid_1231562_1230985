package rttc.dssmv_projectdroid_1231562_1230985.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import rttc.dssmv_projectdroid_1231562_1230985.model.GenericPhrase;
import rttc.dssmv_projectdroid_1231562_1230985.repository.PhraseRepository;
import rttc.dssmv_projectdroid_1231562_1230985.repository.TranslationRepository;

import java.util.List;

public class PhraseViewModel extends AndroidViewModel {
    private final PhraseRepository phraseRepository;
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;
    private final MutableLiveData<String> _translatedText = new MutableLiveData<>();
    public LiveData<String> translatedText = _translatedText;

    private final TranslationRepository translationRepository = new TranslationRepository();

    public void translatePhrase(String phrase, String targetLang) {
        translationRepository.detectAndTranslate(phrase, targetLang, new TranslationRepository.TranslationCallback() {
            @Override
            public void onSuccess(String translatedText, String detectedLang) {
                _translatedText.postValue(translatedText);
            }

            @Override
            public void onError(Exception e) {
                _translatedText.postValue("Translation failed: " + e.getMessage());
            }
        });
    }

    public PhraseViewModel(@NonNull Application application) {
        super(application);
        phraseRepository =  new PhraseRepository();
    }

    public void loadPhrases(String language) {
        _isLoading.setValue(true);
        phraseRepository.loadPhrasesByLanguage(language);
    }

    public LiveData<List<GenericPhrase>> getPhrases() {
        return phraseRepository.getPhrases();
    }

    public LiveData<String> getErrorMessage(){
        return phraseRepository.getErrorMessage();
    }
}

