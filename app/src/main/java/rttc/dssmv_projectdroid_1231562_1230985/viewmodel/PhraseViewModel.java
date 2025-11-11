package rttc.dssmv_projectdroid_1231562_1230985.viewmodel;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import rttc.dssmv_projectdroid_1231562_1230985.model.GenericPhrase;
import rttc.dssmv_projectdroid_1231562_1230985.repository.PhraseRepository;
import rttc.dssmv_projectdroid_1231562_1230985.repository.TranslationRepository;
import rttc.dssmv_projectdroid_1231562_1230985.repository.UserPhraseRepository;

public class PhraseViewModel extends ViewModel {
    private final PhraseRepository phraseRepository;
    private final UserPhraseRepository userPhraseRepository;
    private final TranslationRepository translationRepository;
    private final MutableLiveData<List<GenericPhrase>> _genericPhrases = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<GenericPhrase>> _userPhrases = new MutableLiveData<>(new ArrayList<>());
    private final MediatorLiveData<List<GenericPhrase>> _allPhrases = new MediatorLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> _toastMessage = new MutableLiveData<>();
    public final MutableLiveData<String> translatedText = new MutableLiveData<>();
    public LiveData<List<GenericPhrase>> getAllPhrases() { return _allPhrases; }
    public LiveData<String> getErrorMessage() { return _errorMessage; }
    public LiveData<String> getToastMessage() { return _toastMessage; }


    public PhraseViewModel() {
        phraseRepository = new PhraseRepository();
        userPhraseRepository = new UserPhraseRepository();
        translationRepository = new TranslationRepository();
        _allPhrases.addSource(_genericPhrases, generic -> combineLists());
        _allPhrases.addSource(_userPhrases, user -> combineLists());
    }
    private void combineLists() {
        List<GenericPhrase> combined = new ArrayList<>();
        List<GenericPhrase> userPhrases = _userPhrases.getValue();
        List<GenericPhrase> genericPhrases = _genericPhrases.getValue();

        if (userPhrases != null) {
            combined.addAll(userPhrases);
        }
        if (genericPhrases != null) {
            combined.addAll(genericPhrases);
        }
        _allPhrases.postValue(combined);
    }
    public void loadAllPhrases(Context context, String initialLanguage) {
        loadGenericPhrases(initialLanguage);
        loadUserPhrases(context);
    }
    public void loadGenericPhrases(String language) {
        phraseRepository.loadGenericPhrases(language, new PhraseRepository.LoadPhrasesCallback() {
            @Override
            public void onSuccess(List<GenericPhrase> phrases) {
                _genericPhrases.postValue(phrases);
            }
            @Override
            public void onError(Exception e) {
                _errorMessage.postValue(e.getMessage());
            }
        });
    }
    public void loadUserPhrases(Context context) {
        userPhraseRepository.loadUserPhrases(context, new UserPhraseRepository.LoadUserPhrasesCallback() {
            @Override
            public void onSuccess(List<GenericPhrase> userPhrases) {
                _userPhrases.postValue(userPhrases);
            }
            @Override
            public void onError(Exception e) {
                _errorMessage.postValue(e.getMessage());
            }
        });
    }
    public void saveUserPhrase(GenericPhrase phrase, Context context) {

        _toastMessage.postValue("Detecting language...");
        translationRepository.detectAndTranslate(phrase.getText(), "en", new TranslationRepository.TranslationCallback() {

            @Override
            public void onSuccess(String translated, String detectedLang) {
                phrase.setLanguage(detectedLang);
                _toastMessage.postValue("Language detected: " + detectedLang.toUpperCase() + ". Saving...");

                userPhraseRepository.saveUserPhrase(phrase, context, new UserPhraseRepository.SaveCallback() {
                    @Override
                    public void onSuccess() {
                        _toastMessage.postValue("Phrase saved!");
                        loadUserPhrases(context);
                    }
                    @Override
                    public void onError(Exception e) {
                        _errorMessage.postValue(e.getMessage());
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                _errorMessage.postValue("Error detecting language, phrase not saved.");
            }
        });
    }

    public void deleteUserPhrase(GenericPhrase phrase, Context context) {
        userPhraseRepository.deleteUserPhrase(phrase, context, new UserPhraseRepository.DeleteCallback() {
            @Override
            public void onSuccess() {
                _toastMessage.postValue("Phrase deleted.");
                loadUserPhrases(context);
            }
            @Override
            public void onError(Exception e) {
                _errorMessage.postValue(e.getMessage());
            }
        });
    }
    public void translatePhrase(String text, String targetLang) {
        translationRepository.detectAndTranslate(text, targetLang, new TranslationRepository.TranslationCallback() {
            @Override
            public void onSuccess(String translated, String detectedLang) {
                translatedText.postValue(translated);
            }

            @Override
            public void onError(Exception e) {
                _errorMessage.postValue(e.getMessage());
            }
        });
    }
}