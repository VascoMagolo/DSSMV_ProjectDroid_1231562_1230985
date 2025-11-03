package rttc.dssmv_projectdroid_1231562_1230985.model;
import okhttp3.*;
import org.json.JSONObject;
import rttc.dssmv_projectdroid_1231562_1230985.BuildConfig;

public class TranslationRepository {

    private final OkHttpClient client = new OkHttpClient();

    public interface TranslationCallback {
        void onSuccess(String translatedText, String detectedLang);
        void onError(Exception e);
    }

    public void detectAndTranslate(String text, String targetLanguageCode, TranslationCallback callback) {
        detectLang(text, new LanguageDetectionCallback() {
            @Override
            public void onSuccess(String detectedLang) {
                translateText(text, detectedLang, targetLanguageCode, new TranslationApiCallback() {
                    @Override
                    public void onSuccess(String translatedText) {
                        callback.onSuccess(translatedText, detectedLang);
                    }

                    @Override
                    public void onError(Exception e) {
                        callback.onError(e);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    private interface LanguageDetectionCallback {
        void onSuccess(String detectedLang);
        void onError(Exception e);
    }

    private void detectLang(String text, LanguageDetectionCallback callback) {
        new Thread(() -> {
            try {
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("input_text", text);
                RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));

                Request request = new Request.Builder()
                        .url("https://translateai.p.rapidapi.com/detect")
                        .post(body)
                        .addHeader("x-rapidapi-key", BuildConfig.TranslateAPI_KEY)
                        .addHeader("x-rapidapi-host", "translateai.p.rapidapi.com")
                        .addHeader("Content-Type", "application/json")
                        .build();

                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                JSONObject json = new JSONObject(responseData);

                if (json.has("lang")) {
                    callback.onSuccess(json.getString("lang"));
                } else {
                    callback.onSuccess("en"); // Fallback
                }
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    private interface TranslationApiCallback {
        void onSuccess(String translatedText);
        void onError(Exception e);
    }

    private void translateText(String text, String fromLang, String toLang, TranslationApiCallback callback) {
        new Thread(() -> {
            try {
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("origin_language", fromLang);
                jsonBody.put("target_language", toLang);
                jsonBody.put("input_text", text);
                RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));

                Request request = new Request.Builder()
                        .url("https://translateai.p.rapidapi.com/google/translate/text")
                        .post(body)
                        .addHeader("x-rapidapi-key", BuildConfig.TranslateAPI_KEY)
                        .addHeader("x-rapidapi-host", "translateai.p.rapidapi.com")
                        .addHeader("Content-Type", "application/json")
                        .build();

                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                JSONObject json = new JSONObject(responseData);

                if (json.has("translation")) {
                    callback.onSuccess(json.getString("translation"));
                } else {
                    callback.onSuccess("Translation not available");
                }
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }
}