package rttc.dssmv_projectdroid_1231562_1230985.controller;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import androidx.core.content.ContextCompat;

import rttc.dssmv_projectdroid_1231562_1230985.view.fragments.ConversationFragment;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import static rttc.dssmv_projectdroid_1231562_1230985.BuildConfig.TranslateAPI_KEY;

public class ConversationController {

    private final Context context;
    private final ConversationFragment view;
    private SpeechRecognizer recognizer;
    private String detectedLang;

    public ConversationController(Context context, ConversationFragment view) {
        this.context = context;
        this.view = view;
    }

    public void startListening(String targetLanguageCode) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            postUi("Voice recognition not available.");
            return;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            postUi("Microphone permission not allowed.");
            return;
        }

        if (recognizer != null) {
            try {
                recognizer.stopListening();
                recognizer.cancel();
                recognizer.destroy();
            } catch (Exception ignored) {}
            recognizer = null;
        }

        recognizer = SpeechRecognizer.createSpeechRecognizer(context);

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-PT");
        recognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {
                postUi("🎤 Listening...");
            }

            @Override public void onBeginningOfSpeech() {}

            @Override public void onRmsChanged(float rmsdB) {}

            @Override public void onBufferReceived(byte[] buffer) {}

            @Override public void onEndOfSpeech() {
                postUi("Processing...");
            }

            @Override
            public void onError(int error) {
                postUi("Recognition error: " + getErrorText(error));
                destroy();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches =
                        results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String recognizedText = matches.get(0);
                    postUi(recognizedText);
                    detectLang(recognizedText, targetLanguageCode); // detect language
                } else {
                    postUi("No speech recognized.");
                }
                destroy();
            }

            @Override public void onPartialResults(Bundle partialResults) {}

            @Override public void onEvent(int eventType, Bundle params) {}
        });

        recognizer.startListening(intent);
    }


    private void postUi(final String text) {
        if (view == null || view.getActivity() == null) return;
        view.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.updateRecognizedText(text);
            }
        });
    }

    private String getErrorText(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO: return "Audio problem.";
            case SpeechRecognizer.ERROR_CLIENT: return "Client error.";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: return "Not enough permissions.";
            case SpeechRecognizer.ERROR_NETWORK: return "Network error.";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: return "Timeout network error.";
            case SpeechRecognizer.ERROR_NO_MATCH: return "No match.";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: return "Busy recognizer.";
            case SpeechRecognizer.ERROR_SERVER: return "Server error.";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: return "Speech timeout error.";
            default: return "Unknown error (" + error + ").";
        }
    }

    public void destroy() {
        if (recognizer != null) {
            try {
                recognizer.stopListening();
                recognizer.cancel();
                recognizer.destroy();
            } catch (Exception ignored) {}
            recognizer = null;
        }
    }

    public void translateTextRapidAPI(final String text, final String fromLang, final String toLang) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("origin_language", fromLang);
                jsonBody.put("target_language", toLang);
                jsonBody.put("words_not_to_translate", "");
                jsonBody.put("input_text", text);

                RequestBody body = RequestBody.create(
                        jsonBody.toString(),
                        MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                        .url("https://translateai.p.rapidapi.com/google/translate/text")
                        .post(body)
                        .addHeader("x-rapidapi-key", TranslateAPI_KEY)
                        .addHeader("x-rapidapi-host", "translateai.p.rapidapi.com")
                        .addHeader("Content-Type", "application/json")
                        .build();

                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                JSONObject json = new JSONObject(responseData);
                String translated;
                if (json.has("translation")) {
                    translated = json.getString("translation");
                } else {
                    translated = "Translation not available"; // fallback
                }

                view.getActivity().runOnUiThread(() ->
                        view.updateTranslatedText(translated)
                );

            } catch (Exception e) {
                e.printStackTrace();
                postUi("Translation error: " + e.getMessage());
            }
        }).start();
    }

    public void detectLang(final String text, final String targetLanguageCode){
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("input_text", text);

                RequestBody body = RequestBody.create(
                        jsonBody.toString(),
                        MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                        .url("https://translateai.p.rapidapi.com/detect")
                        .post(body)
                        .addHeader("x-rapidapi-key", TranslateAPI_KEY)
                        .addHeader("x-rapidapi-host", "translateai.p.rapidapi.com")
                        .addHeader("Content-Type", "application/json")
                        .build();

                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                JSONObject json = new JSONObject(responseData);
                if (json.has("lang")) {
                    detectedLang = json.getString("lang");
                    translateTextRapidAPI(text, detectedLang, targetLanguageCode);
                } else {
                    detectedLang = "Language not detected"; // fallback
                }

                view.getActivity().runOnUiThread(() ->
                        view.updateOriginalLangText(detectedLang)
                );

            } catch (Exception e) {
                e.printStackTrace();
                postUi("Language detection error: " + e.getMessage());
            }
        }).start();

    }
}