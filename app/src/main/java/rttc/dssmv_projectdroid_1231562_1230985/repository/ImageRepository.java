package rttc.dssmv_projectdroid_1231562_1230985.repository;

import okhttp3.*;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import static rttc.dssmv_projectdroid_1231562_1230985.BuildConfig.TranslateAPI_KEY;

public class ImageRepository {
    private final OkHttpClient client;
    public ImageRepository() {
        client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public interface OCRCallback {
        void onSuccess(String extractedText);
        void onError(Exception e);
    }
    public void extractTextFromImage(byte[] imageBytes, OCRCallback callback) {
        new Thread(() -> {
            try {
                RequestBody body = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart(
                                "image",
                                "photo.jpg",
                                RequestBody.create(imageBytes, MediaType.parse("image/jpeg"))
                        )
                        .build();

                Request request = new Request.Builder()
                        .url("https://ocr43.p.rapidapi.com/v1/results")
                        .post(body)
                        .addHeader("x-rapidapi-key", TranslateAPI_KEY)
                        .addHeader("x-rapidapi-host", "ocr43.p.rapidapi.com")
                        .build();

                Response response = client.newCall(request).execute();
                String json = response.body().string();

                JSONObject jsonObject = new JSONObject(json);
                String extractedText = jsonObject
                        .getJSONArray("results")
                        .getJSONObject(0)
                        .getJSONArray("entities")
                        .getJSONObject(0)
                        .getJSONArray("objects")
                        .getJSONObject(0)
                        .getJSONArray("entities")
                        .getJSONObject(0)
                        .getString("text");

                callback.onSuccess(extractedText);

            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }
}