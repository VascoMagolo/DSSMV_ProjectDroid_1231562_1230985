package rttc.dssmv_projectdroid_1231562_1230985.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import rttc.dssmv_projectdroid_1231562_1230985.repository.ImageRepository;
import rttc.dssmv_projectdroid_1231562_1230985.repository.TranslationRepository;

public class ImageViewModel extends AndroidViewModel {
    private final ImageRepository imageRepository;
    private final TranslationRepository translationRepository;

    private final MutableLiveData<String> extractedText = new MutableLiveData<>();
    private final MutableLiveData<String> translatedText = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public LiveData<String> getExtractedText() { return extractedText; }
    public LiveData<String> getTranslatedText() { return translatedText; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public ImageViewModel(@NonNull Application application) {
        super(application);
        imageRepository = new ImageRepository();
        translationRepository = new TranslationRepository();
    }

    public void processImage(byte[] imageBytes, String targetLang) {
        isLoading.postValue(true);
        imageRepository.extractTextFromImage(imageBytes, new ImageRepository.OCRCallback() {
            @Override
            public void onSuccess(String text) {
                extractedText.postValue(text);
                translateText(text, targetLang);
            }

            @Override
            public void onError(Exception e) {
                extractedText.postValue("OCR Error: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    private void translateText(String text, String targetLang) {
        translationRepository.detectAndTranslate(text, targetLang, new TranslationRepository.TranslationCallback() {
            @Override
            public void onSuccess(String translated, String detectedLang) {
                translatedText.postValue(translated);
                isLoading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                translatedText.postValue("Translation error: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }
}
