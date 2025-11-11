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
    private final MutableLiveData<List<GenericPhrase>> _genericPhrases = new MutableLiveData<>();
    private final MutableLiveData<List<GenericPhrase>> _userPhrases = new MutableLiveData<>();
    private final MediatorLiveData<List<GenericPhrase>> _allPhrases = new MediatorLiveData<>();
    public LiveData<List<GenericPhrase>> getAllPhrases() { return _allPhrases; }
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() { return _errorMessage; }
    public MutableLiveData<String> translatedText = new MutableLiveData<>();
    private final MutableLiveData<String> _toastMessage = new MutableLiveData<>();
    public LiveData<String> getToastMessage() { return _toastMessage; }


    public PhraseViewModel() {
        phraseRepository = new PhraseRepository();
        userPhraseRepository = new UserPhraseRepository();
        translationRepository = new TranslationRepository();
        _allPhrases.addSource(_genericPhrases, genericList ->
                combineLists(_userPhrases.getValue(), genericList));
        _allPhrases.addSource(_userPhrases, userList ->
                combineLists(userList, _genericPhrases.getValue()));
    }

    private void combineLists(List<GenericPhrase> userList, List<GenericPhrase> genericList) {
        List<GenericPhrase> combined = new ArrayList<>();
        if (userList != null) {
            combined.addAll(userList);
        }
        if (genericList != null) {
            combined.addAll(genericList);
        }
        _allPhrases.postValue(combined);
    }
    public void loadAllPhrases(Context context, String genericLang) {
        loadGenericPhrases(genericLang);
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
            public void onSuccess(List<GenericPhrase> phrases) {
                _userPhrases.postValue(phrases);
            }
            @Override
            public void onError(Exception e) {
                _errorMessage.postValue(e.getMessage());
            }
        });
    }

    public void saveUserPhrase(GenericPhrase phrase, Context context) {
        translationRepository.detectAndTranslate(phrase.getText(), "en", new TranslationRepository.TranslationCallback() {
            @Override
            public void onSuccess(String translated, String detectedLang) {
                phrase.setLanguage(detectedLang);
                userPhraseRepository.saveUserPhrase(phrase, context, new UserPhraseRepository.SaveCallback() {
                    @Override
                    public void onSuccess() {
                        _toastMessage.postValue("Phrase saved.");
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
                _errorMessage.postValue("Error detecting language.Phrase not saved.");
            }
        });
    }

    public void deleteUserPhrase(GenericPhrase phrase, Context context) {
        if (!phrase.isUserPhrase()) return;

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
                _errorMessage.postValue("Translation Error: " + e.getMessage());
            }
        });
    }
}