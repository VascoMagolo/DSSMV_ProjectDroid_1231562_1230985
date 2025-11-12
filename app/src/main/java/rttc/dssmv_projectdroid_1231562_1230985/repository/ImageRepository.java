package rttc.dssmv_projectdroid_1231562_1230985.repository;

import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.ApiException;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.NetworkException;

import static rttc.dssmv_projectdroid_1231562_1230985.BuildConfig.TranslateAPI_KEY;

public class ImageRepository {
    private final OkHttpClient client;
    public ImageRepository() {
        client = new OkHttpClient.Builder() // increasing the timeout time of the client
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public interface OCRCallback { // Callback for OCR method
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
                        .build(); // building the request body

                Request request = new Request.Builder()
                        .url("https://ocr43.p.rapidapi.com/v1/results")
                        .post(body)
                        .addHeader("x-rapidapi-key", TranslateAPI_KEY)
                        .addHeader("x-rapidapi-host", "ocr43.p.rapidapi.com")
                        .build(); // building the request for the API

                Response response = client.newCall(request).execute();
                String json = response.body().string();

                JSONObject jsonObject = new JSONObject(json);
                String extractedText = jsonObject // getting the text extracted from the api
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
            } catch (SocketTimeoutException e) {
                callback.onError(new NetworkException("OCR request timed out."));
            } catch (JSONException e) {
                callback.onError(new ApiException("Failed to parse OCR response."));
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    } // method that call the OCR API and get the text from an image
}