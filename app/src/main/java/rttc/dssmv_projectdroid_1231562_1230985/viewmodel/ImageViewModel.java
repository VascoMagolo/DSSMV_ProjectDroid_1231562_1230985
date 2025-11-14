package rttc.dssmv_projectdroid_1231562_1230985.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import rttc.dssmv_projectdroid_1231562_1230985.repository.ImageRepository;
import rttc.dssmv_projectdroid_1231562_1230985.repository.TranslationRepository;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.ApiException;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.NetworkException;
import rttc.dssmv_projectdroid_1231562_1230985.model.ImageHistory;
import rttc.dssmv_projectdroid_1231562_1230985.repository.ImageHistoryRepository;

public class ImageViewModel extends AndroidViewModel {
    private final ImageRepository imageRepository;
    private final TranslationRepository translationRepository;
    private final ImageHistoryRepository imageHistoryRepository;

    private final MutableLiveData<String> extractedText = new MutableLiveData<>();
    private final MutableLiveData<String> translatedText = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<String> getExtractedText() { return extractedText; }
    public LiveData<String> getTranslatedText() { return translatedText; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public ImageViewModel(@NonNull Application application) {
        super(application);
        imageRepository = new ImageRepository();
        translationRepository = new TranslationRepository();
        imageHistoryRepository = new ImageHistoryRepository();
    }

    public void processImage(byte[] imageBytes, String targetLang) {
        isLoading.postValue(true);
        String fileName = "image_" + System.currentTimeMillis() + ".jpg";
        imageHistoryRepository.uploadImage(imageBytes, fileName, new ImageHistoryRepository.ImageUploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                imageRepository.extractTextFromImage(imageBytes, new ImageRepository.OCRCallback() {
                    @Override
                    public void onSuccess(String text) {
                        extractedText.postValue(text);
                        processImageInternal(imageBytes, targetLang, imageUrl);
                    }

                    @Override
                    public void onError(Exception e) {
                        if (e instanceof NetworkException) {
                            extractedText.postValue("OCR Error: " + e.getMessage());
                        } else if (e instanceof ApiException) {
                            extractedText.postValue("OCR Error: Could not read text from image.");
                        } else {
                            extractedText.postValue("OCR Error: An unknown error occurred.");
                        }
                        isLoading.postValue(false);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                String errorMsg = "Error uploading image to storage: " + e.getMessage();
                extractedText.postValue(errorMsg);
                errorMessage.postValue(errorMsg);
                isLoading.postValue(false);
            }
        });
    }

    public void processImageInternal(byte[] imageBytes, String targetLang, String imageUrl) {
        String currentText = extractedText.getValue();
        if (currentText != null && !currentText.isEmpty()) {
            translateText(currentText, targetLang, imageUrl);
        } else {
            imageRepository.extractTextFromImage(imageBytes, new ImageRepository.OCRCallback() {
                @Override
                public void onSuccess(String text) {
                    extractedText.postValue(text);
                    translateText(text, targetLang, imageUrl);
                }

                @Override
                public void onError(Exception e) {
                    if (e instanceof NetworkException) {
                        extractedText.postValue("OCR Error: " + e.getMessage());
                    } else if (e instanceof ApiException) {
                        extractedText.postValue("OCR Error: Could not read text from image.");
                    } else {
                        extractedText.postValue("OCR Error: An unknown error occurred.");
                    }
                    isLoading.postValue(false);
                }
            });
        }
    }

    private void translateText(String text, String targetLang, String imageUrl) {
        translationRepository.detectAndTranslate(text, targetLang, new TranslationRepository.TranslationCallback() {
            @Override
            public void onSuccess(String translated, String detectedLang) {
                translatedText.postValue(translated);
                isLoading.postValue(false);
                saveToImageHistory(text, translated, targetLang, imageUrl);
            }

            @Override
            public void onError(Exception e) {
                translatedText.postValue("Translation error: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    private void saveToImageHistory(String extractedText, String translatedText, String targetLang, String imageUrl) {
        ImageHistory imageHistory = new ImageHistory(
                imageUrl,
                extractedText,
                translatedText,
                targetLang
        );

        imageHistoryRepository.saveImageHistory(imageHistory, getApplication());
    }
}