package rttc.dssmv_projectdroid_1231562_1230985.viewmodel;

import android.content.Context;
import android.widget.Toast;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import rttc.dssmv_projectdroid_1231562_1230985.model.GenericPhrase;
import rttc.dssmv_projectdroid_1231562_1230985.repository.PhraseRepository;
import rttc.dssmv_projectdroid_1231562_1230985.repository.TranslationRepository;
import rttc.dssmv_projectdroid_1231562_1230985.repository.UserPhraseRepository;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.ApiException;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.NetworkException;

import java.util.ArrayList;
import java.util.List;

public class PhraseViewModel extends ViewModel {

    private final PhraseRepository phraseRepository;
    private final UserPhraseRepository userPhraseRepository;
    private final TranslationRepository translationRepository;
    private final MutableLiveData<List<GenericPhrase>> _genericPhrases = new MutableLiveData<>();
    private final MutableLiveData<List<GenericPhrase>> _userPhrases = new MutableLiveData<>();
    private final MediatorLiveData<List<GenericPhrase>> _allPhrases = new MediatorLiveData<>();
    public LiveData<List<GenericPhrase>> getAllPhrases() {
        return _allPhrases;
    }

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() {
        return _errorMessage;
    }

    private final MutableLiveData<String> _translatedText = new MutableLiveData<>();
    public LiveData<String> translatedText = _translatedText;

    private final MutableLiveData<Boolean> _saveSuccess = new MutableLiveData<>();
    public LiveData<Boolean> getSaveSuccess() { return _saveSuccess; }

    private final MutableLiveData<Boolean> _deleteSuccess = new MutableLiveData<>();
    public LiveData<Boolean> getDeleteSuccess() { return _deleteSuccess; }


    public PhraseViewModel() {
        phraseRepository = new PhraseRepository();
        userPhraseRepository = new UserPhraseRepository();
        translationRepository = new TranslationRepository();
        _allPhrases.addSource(_genericPhrases, genericList ->
                combineLists(genericList, _userPhrases.getValue()));
        _allPhrases.addSource(_userPhrases, userList ->
                combineLists(_genericPhrases.getValue(), userList));
    }

    private void combineLists(List<GenericPhrase> genericList, List<GenericPhrase> userList) {
        List<GenericPhrase> combined = new ArrayList<>();
        if (userList != null) {
            combined.addAll(userList);
        }
        if (genericList != null) {
            combined.addAll(genericList);
        }
        _allPhrases.postValue(combined);
    }

    public void loadAllPhrases(Context context, String language) {
        loadGenericPhrases(language);
        loadUserPhrases(context, language);
    }
    public void loadPhrasesForLanguage(Context context, String language) {
        loadGenericPhrases(language);
        loadUserPhrases(context, language);

    }
    private void loadGenericPhrases(String language) {
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
    private void loadUserPhrases(Context context, String language) {
        userPhraseRepository.loadUserPhrases(context, language, new UserPhraseRepository.LoadUserPhrasesCallback() {
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
    public void translatePhrase(String text, String targetLang) {
        translationRepository.detectAndTranslate(text, targetLang, new TranslationRepository.TranslationCallback() {
            @Override
            public void onSuccess(String translated, String detectedLang) {
                _translatedText.postValue(translated);
            }
            @Override
            public void onError(Exception e) {
                _errorMessage.postValue("Translation Error: " + e.getMessage());
            }
        });
    }
    public void saveUserPhrase(Context context, GenericPhrase phrase) {
        translationRepository.detectAndTranslate(phrase.getText(), "en", new TranslationRepository.TranslationCallback() {
            @Override
            public void onSuccess(String translated, String detectedLang) {
                phrase.setLanguage(detectedLang);
                userPhraseRepository.saveUserPhrase(phrase, context, new UserPhraseRepository.SaveCallback() {
                    @Override
                    public void onSuccess() {
                        _saveSuccess.postValue(true);
                        loadUserPhrases(context, detectedLang);
                    }
                    @Override
                    public void onError(Exception e) {
                        if (e instanceof ApiException) {
                            _errorMessage.postValue(e.getMessage());
                        } else if (e instanceof NetworkException) {
                            _errorMessage.postValue(e.getMessage());
                        } else {
                            _errorMessage.postValue("Error saving phrase.");
                        }
                    }
                });
            }
            @Override
            public void onError(Exception e) {
                _errorMessage.postValue("Error detecting language for saving: " + e.getMessage());
            }
        });
    }

    public void deleteUserPhrase(Context context, GenericPhrase phrase) {
        userPhraseRepository.deleteUserPhrase(phrase, context, new UserPhraseRepository.DeleteCallback() {
            @Override
            public void onSuccess() {
                _deleteSuccess.postValue(true);
                loadUserPhrases(context, phrase.getLanguage());
            }
            @Override
            public void onError(Exception e) {
                _errorMessage.postValue("Error deleting phrase: " + e.getMessage());
            }
        });
    }

    public void clearSaveSuccess() {
        _saveSuccess.setValue(false);
    }
    public void clearDeleteSuccess() {
        _deleteSuccess.setValue(false);
    }
    public void clearErrorMessage() { _errorMessage.setValue(null); }
}