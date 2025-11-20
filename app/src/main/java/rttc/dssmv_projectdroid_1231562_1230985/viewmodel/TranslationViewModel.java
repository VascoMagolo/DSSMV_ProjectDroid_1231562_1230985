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

/**
 * ViewModel for translation fragment
 * Manages the state of voice translation feature
 * Uses Android built-in {@link SpeechRecognizer} and works with {@link TranslationRepository}
 */
public class TranslationViewModel extends AndroidViewModel {

    private final TranslationRepository repository;
    private SpeechRecognizer recognizer;

    // LiveData for the text output from SpeechRecognizer
    private final MutableLiveData<String> _recognizedText = new MutableLiveData<>();
    public LiveData<String> recognizedText = _recognizedText;

    // LiveData for the text output from the repository
    private final MutableLiveData<String> _translatedText = new MutableLiveData<>();
    public LiveData<String> translatedText = _translatedText;

    // LiveData for the detected source language
    private final MutableLiveData<String> _originalLanguage = new MutableLiveData<>();
    public LiveData<String> originalLanguage = _originalLanguage;

    // LiveData that provides real-time status update to the UI
    private final MutableLiveData<String> _statusMessage = new MutableLiveData<>();
    public LiveData<String> statusMessage = _statusMessage;

    public TranslationViewModel(@NonNull Application application) {
        super(application);
        repository = new TranslationRepository();
    }

    /**
     * Starts {@link SpeechRecognizer} that listens to user input
     * Called by the view when user presses microphone button
     * @param targetLanguageCode the target language to translate to
     */
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
            /**
             * Called when recognizer is ready for speech
             * Updates status message
             * @param params parameters set by the recognition service. Reserved for future use.
             */
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

            /**
             * Called when STT has a result
             * The recognized text is posted on {@code _recognizedText}
             * and translation is initiated by calling {@link TranslationViewModel#translate(String, String)}
             * @param results the recognition results.
             */
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

    /**
     * Calls {@link TranslationRepository} to detect language and translate given text
     * Results are posted on correct LiveData object
     * @param text the text from speech recognizer
     * @param targetLanguageCode target language from in the UI
     */
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


    /**
     * Stop, cancel and destroy {@link SpeechRecognizer} instance
     */
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

    /**
     * Makes sure that {@link SpeechRecognizer} instance is destroyed when ViewModel is cleared
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        destroyRecognizer();
    }
}