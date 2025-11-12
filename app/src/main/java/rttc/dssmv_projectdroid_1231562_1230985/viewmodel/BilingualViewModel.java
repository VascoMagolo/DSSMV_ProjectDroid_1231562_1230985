package rttc.dssmv_projectdroid_1231562_1230985.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import rttc.dssmv_projectdroid_1231562_1230985.model.Conversation;
import rttc.dssmv_projectdroid_1231562_1230985.model.TtsRequest;
import rttc.dssmv_projectdroid_1231562_1230985.repository.TranslationRepository;

public class BilingualViewModel extends AndroidViewModel {

    private final TranslationRepository translationRepository;
    private final ConversationHistoryViewModel historyViewModel;

    private final MutableLiveData<String> _textForLangA = new MutableLiveData<>();
    public LiveData<String> getTextForLangA() { return _textForLangA; }

    private final MutableLiveData<String> _textForLangB = new MutableLiveData<>();
    public LiveData<String> getTextForLangB() { return _textForLangB; }

    private final MutableLiveData<TtsRequest> _ttsRequest = new MutableLiveData<>();
    public LiveData<TtsRequest> getTtsRequest() { return _ttsRequest; }

    private final MutableLiveData<String> _statusMessage = new MutableLiveData<>();
    public LiveData<String> getStatusMessage() { return _statusMessage; }

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() { return _errorMessage; }

    private String langA = "pt";
    private String langB = "en";


    public BilingualViewModel(@NonNull Application application) {
        super(application);
        translationRepository = new TranslationRepository();
        historyViewModel = new ConversationHistoryViewModel(application);
    }

    public void translateText(String spokenText, String sourceLang, String targetLang) {

        _statusMessage.postValue("Translating...");

        if (sourceLang.equals(langA)) {
            _textForLangA.postValue(spokenText);
            _textForLangB.postValue("...");
        } else {
            _textForLangB.postValue(spokenText);
            _textForLangA.postValue("...");
        }
        translationRepository.translate(spokenText, sourceLang, targetLang, new TranslationRepository.TranslationCallback() {

            @Override
            public void onSuccess(String translatedText, String detectedSourceLang) {
                if (targetLang.equals(langB)) {
                    _textForLangB.postValue(translatedText);
                } else {
                    _textForLangA.postValue(translatedText);
                }

                _ttsRequest.postValue(new TtsRequest(translatedText, targetLang));
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

    private void saveToHistory(String original, String translated, String sourceLang, String targetLang) {
        Conversation conversation = new Conversation(
                null,
                original,
                translated,
                sourceLang,
                targetLang
        );
        historyViewModel.saveConversation(conversation, getApplication().getApplicationContext());
    }
}