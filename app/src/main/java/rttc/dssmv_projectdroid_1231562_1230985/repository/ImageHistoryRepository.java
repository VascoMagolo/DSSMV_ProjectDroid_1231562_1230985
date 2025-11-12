package rttc.dssmv_projectdroid_1231562_1230985.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import rttc.dssmv_projectdroid_1231562_1230985.BuildConfig;
import rttc.dssmv_projectdroid_1231562_1230985.model.ImageHistory;
import rttc.dssmv_projectdroid_1231562_1230985.model.User;
import rttc.dssmv_projectdroid_1231562_1230985.utils.SessionManager;

public class ImageHistoryRepository {
    private static final String TAG = "ImageHistoryRepository";
    private final OkHttpClient client;
    private static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    private static final String SUPABASE_KEY = BuildConfig.SUPABASE_KEY;

    private final MutableLiveData<List<ImageHistory>> _imageHistory = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();

    public ImageHistoryRepository() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        _imageHistory.setValue(new ArrayList<>());
    }

    public void loadImageHistory(Context context) {
        new Thread(() -> {
            try {
                SessionManager session = new SessionManager(context);
                User user = session.getUser();

                if (user == null || user.getId() == null) {
                    _imageHistory.postValue(new ArrayList<>());
                    return;
                }

                HttpUrl url = HttpUrl.parse(SUPABASE_URL + "/rest/v1/image_history")
                        .newBuilder()
                        .addQueryParameter("user_id", "eq." + user.getId())
                        .addQueryParameter("select", "*")
                        .addQueryParameter("order", "timestamp.desc")
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                if (response.isSuccessful()) {
                    List<ImageHistory> historyList = parseImageHistoryResponse(responseBody);
                    _imageHistory.postValue(historyList);
                } else {
                    throw new IOException("HTTP " + response.code() + ": " + responseBody);
                }

            } catch (Exception e) {
                _errorMessage.postValue("Error loading image history: " + e.getMessage());
            }
        }).start();
    }

    public void saveImageHistory(ImageHistory imageHistory, Context context) {
        new Thread(() -> {
            try {
                SessionManager session = new SessionManager(context);
                User user = session.getUser();

                if (user == null || user.getId() == null) {
                    _errorMessage.postValue("User not logged in");
                    return;
                }

                imageHistory.setUserId(user.getId());

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("user_id", imageHistory.getUserId());
                jsonBody.put("image_url", imageHistory.getImageUrl() != null ? imageHistory.getImageUrl() : "");
                jsonBody.put("extracted_text", imageHistory.getExtractedText() != null ? imageHistory.getExtractedText() : "");
                jsonBody.put("translated_text", imageHistory.getTranslatedText() != null ? imageHistory.getTranslatedText() : "");
                jsonBody.put("target_language", imageHistory.getTargetLanguage() != null ? imageHistory.getTargetLanguage() : "en");

                if (imageHistory.getTimestamp() != null) {
                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                    isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    jsonBody.put("timestamp", isoFormat.format(imageHistory.getTimestamp()));
                } else {
                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                    isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    jsonBody.put("timestamp", isoFormat.format(new java.util.Date()));
                }

                RequestBody body = RequestBody.create(
                        jsonBody.toString(),
                        MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/rest/v1/image_history")
                        .post(body)
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "return=minimal")
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body() != null ? response.body().string() : "";

                if (response.isSuccessful()) {
                    loadImageHistory(context);
                } else {
                    throw new IOException("HTTP " + response.code() + ": " + responseBody);
                }

            } catch (Exception e) {
                _errorMessage.postValue("Error saving image: " + e.getMessage());
            }
        }).start();
    }

    public void deleteImageHistory(ImageHistory imageHistory, Context context) {
        new Thread(() -> {
            try {
                SessionManager session = new SessionManager(context);
                User user = session.getUser();

                if (user == null || user.getId() == null || imageHistory.getId() == null) {
                    _errorMessage.postValue("Cannot delete: User not logged in or image ID null");
                    return;
                }

                HttpUrl url = HttpUrl.parse(SUPABASE_URL + "/rest/v1/image_history")
                        .newBuilder()
                        .addQueryParameter("id", "eq." + imageHistory.getId())
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .delete()
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body() != null ? response.body().string() : "";

                if (response.isSuccessful()) {
                    loadImageHistory(context);
                } else {
                    throw new IOException("HTTP " + response.code() + ": " + responseBody);
                }

            } catch (Exception e) {
                _errorMessage.postValue("Error deleting image: " + e.getMessage());
            }
        }).start();
    }

    private List<ImageHistory> parseImageHistoryResponse(String jsonResponse) throws JSONException {
        List<ImageHistory> historyList = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(jsonResponse);

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            ImageHistory imageHistory = new ImageHistory();

            imageHistory.setId(jsonObject.optString("id"));
            imageHistory.setUserId(jsonObject.optString("user_id"));
            imageHistory.setImageUrl(jsonObject.optString("image_url"));
            imageHistory.setExtractedText(jsonObject.optString("extracted_text"));
            imageHistory.setTranslatedText(jsonObject.optString("translated_text"));
            imageHistory.setTargetLanguage(jsonObject.optString("target_language"));

            String timestampStr = jsonObject.optString("timestamp");
            if (timestampStr != null && !timestampStr.isEmpty()) {
                try {
                    java.util.Date timestamp = isoFormat.parse(timestampStr);
                    imageHistory.setTimestamp(timestamp);
                } catch (Exception e) {
                    imageHistory.setTimestamp(new java.util.Date());
                }
            } else {
                imageHistory.setTimestamp(new java.util.Date());
            }

            historyList.add(imageHistory);
        }

        return historyList;
    }
    public interface ImageUploadCallback {
        void onSuccess(String imageUrl);
        void onError(Exception e);
    }

    public void uploadImage(byte[] imageBytes, String fileName, ImageUploadCallback callback) {
        new Thread(() -> {
            try {
                String bucketName = "images";

                RequestBody body = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", fileName,
                                RequestBody.create(imageBytes, MediaType.parse("image/jpeg")))
                        .build();

                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/storage/v1/object/images/" + fileName)
                        .post(body)
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                if (response.isSuccessful()) {
                    String imageUrl = SUPABASE_URL + "/storage/v1/object/public/images/" + fileName;
                    callback.onSuccess(imageUrl);
                } else {
                    throw new IOException("HTTP " + response.code() + ": " + responseBody);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error uploading image to Supabase", e);
                callback.onError(e);
            }
        }).start();
    }

    public MutableLiveData<List<ImageHistory>> getImageHistory() {
        return _imageHistory;
    }

    public MutableLiveData<String> getErrorMessage() {
        return _errorMessage;
    }
}