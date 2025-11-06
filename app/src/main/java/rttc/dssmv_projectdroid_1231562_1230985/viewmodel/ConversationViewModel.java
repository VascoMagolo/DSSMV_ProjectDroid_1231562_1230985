package rttc.dssmv_projectdroid_1231562_1230985.viewmodel;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.ArrayList;
import rttc.dssmv_projectdroid_1231562_1230985.repository.TranslationRepository;

public class ConversationViewModel extends AndroidViewModel {

    private final TranslationRepository repository;
    private SpeechRecognizer recognizer;

    private final MutableLiveData<String> _recognizedText = new MutableLiveData<>();
    public LiveData<String> recognizedText = _recognizedText;

    private final MutableLiveData<String> _translatedText = new MutableLiveData<>();
    public LiveData<String> translatedText = _translatedText;

    private final MutableLiveData<String> _originalLanguage = new MutableLiveData<>();
    public LiveData<String> originalLanguage = _originalLanguage;

    private final MutableLiveData<String> _statusMessage = new MutableLiveData<>();
    public LiveData<String> statusMessage = _statusMessage;

    public ConversationViewModel(@NonNull Application application) {
        super(application);
        repository = new TranslationRepository();
    }

    public void startListening(String targetLanguageCode) {
        if (!SpeechRecognizer.isRecognitionAvailable(getApplication())) {
            _statusMessage.postValue("Voice recognition not available.");
            return;
        }

        if (recognizer != null) {
            recognizer.destroy();
            recognizer = null;
        }

        recognizer = SpeechRecognizer.createSpeechRecognizer(getApplication());
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        recognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {
                _statusMessage.postValue("🎤 Listenning...");
                _recognizedText.postValue("");
                _translatedText.postValue("");
                _originalLanguage.postValue("");
            }
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {
                _statusMessage.postValue("Processing...");
            }
            @Override public void onError(int error) {
                _statusMessage.postValue("Recognition error: " + error);
                destroyRecognizer();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String recognized = matches.get(0);
                    _recognizedText.postValue(recognized);

                    translate(recognized, targetLanguageCode);
                } else {
                    _statusMessage.postValue("No speech recognized.");
                }
                destroyRecognizer();
            }
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });

        recognizer.startListening(intent);
    }

    private void translate(String text, String targetLanguageCode) {
        repository.detectAndTranslate(text, targetLanguageCode, new TranslationRepository.TranslationCallback() {
            @Override
            public void onSuccess(String translatedText, String detectedLang) {
                _translatedText.postValue(translatedText);
                _originalLanguage.postValue(detectedLang);
            }

            @Override
            public void onError(Exception e) {
                _statusMessage.postValue("Translation error: " + e.getMessage());
            }
        });
    }

    private void destroyRecognizer() {
        if (recognizer != null) {
            try {
                recognizer.stopListening();
                recognizer.cancel();
                recognizer.destroy();
            } catch (Exception ignored) {}
            recognizer = null;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        destroyRecognizer();
    }
}