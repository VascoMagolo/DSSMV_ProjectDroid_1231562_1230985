package rttc.dssmv_projectdroid_1231562_1230985.repository;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import okhttp3.*;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import rttc.dssmv_projectdroid_1231562_1230985.BuildConfig;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.ApiException;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.AuthException;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.NetworkException;
import rttc.dssmv_projectdroid_1231562_1230985.model.Translation;
import rttc.dssmv_projectdroid_1231562_1230985.model.User;
import rttc.dssmv_projectdroid_1231562_1230985.utils.SessionManager;

public class TranslationsRepository {

    private final OkHttpClient client;
    private static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    private static final String SUPABASE_KEY = BuildConfig.SUPABASE_KEY;

    public interface SaveCallback { // Callback interface for Saving the translation
        void onSuccess();
        void onError(Exception e);
    }

    public interface LoadCallback { // Callback interface for Loading the translations
        void onSuccess(List<Translation> translations);
        void onError(Exception e);
    }

    public interface DeleteCallback { // Callback interface for the deletion of the translations
        void onSuccess();
        void onError(Exception e);
    }

    public interface FavoriteCallback { // Callback interface for the method that sets the previous translations as favorite
        void onSuccess();
        void onError(Exception e);
    }

    public TranslationsRepository() { // by default OkHttp waits has a timeout of 10 seconds which sometimes is not enough, this initializes it with a 30 second timeout
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public void saveTranslation(Translation translation, Context context, SaveCallback callback) {
        new Thread(() -> {
            try {
                SessionManager session = new SessionManager(context); // creates new object of SessionManager to get the user from sharedprefs
                User user = session.getUser();

                if (user == null || user.getId() == null) { // this is for is the user is a guest
                    callback.onSuccess(); // this is to fake to continue the application
                    return;
                }

                translation.setUserId(user.getId()); // linking the translation to the user with userId

                Date ts = translation.getTimestamp(); // getting current timestamp
                if (ts == null) {
                    ts = new Date();
                }

                JSONObject translationObject = new JSONObject(); // building the body
                translationObject.put("user_id", translation.getUserId());
                translationObject.put("original_text", translation.getOriginalText());
                translationObject.put("translated_text", translation.getTranslatedText());
                translationObject.put("source_language", translation.getSourceLanguage());
                translationObject.put("target_language", translation.getTargetLanguage());
                translationObject.put("is_favorite", translation.getFavorite());

                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
                isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                translationObject.put("timestamp", isoFormat.format(ts));

                RequestBody body = RequestBody.create(translationObject.toString(), MediaType.parse("application/json"));

                Request request = new Request.Builder() // building the request ( insert/ POST )
                        .url(SUPABASE_URL + "/rest/v1/translations")
                        .post(body)
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "return=minimal")
                        .build();

                Response response = client.newCall(request).execute(); // calling the request
                String responseBody = response.body().string();

                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    if (response.code() == 401 || response.code() == 403) {
                        callback.onError(new AuthException("Authentication Error: " + responseBody));
                    } else {
                        callback.onError(new ApiException("Failed to save translation: " + response.code()));
                    }
                }
            } catch (SocketTimeoutException e) {
                callback.onError(new NetworkException("Network Error: Connection timed out."));
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    public void loadTranslations(Context context, LoadCallback callback) {
        new Thread(() -> {
            try {
                SessionManager session = new SessionManager(context);  // creates new object of SessionManager to get the user from sharedprefs
                User user = session.getUser();

                if (user == null || user.getId() == null) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }

                HttpUrl url = Objects.requireNonNull(HttpUrl.parse(SUPABASE_URL + "/rest/v1/translations"))
                        .newBuilder()
                        .addQueryParameter("user_id", "eq." + user.getId())
                        .addQueryParameter("select", "*")
                        .addQueryParameter("order", "is_favorite.desc,timestamp.desc")
                        .build(); // builds the query, selects all the previous translations of the user with favorite
                                  // being the first and in time order

                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .build(); // builds the request

                Response response = client.newCall(request).execute(); // calls the request
                String responseBody = response.body().string();

                if (response.isSuccessful()) {
                    List<Translation> translations = new ArrayList<>();
                    JSONArray translationsArray = new JSONArray(responseBody);
                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
                    isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                    for (int i = 0; i < translationsArray.length(); i++) {
                        // loop to create new translation instances N times,
                        // N being the number of translations saved on supabase form the logged user
                        JSONObject object = translationsArray.getJSONObject(i);

                        Translation translation = new Translation(); // creating new translation object
                        translation.setId(object.optString("id", null));
                        translation.setUserId(object.optString("user_id", null));
                        translation.setOriginalText(object.optString("original_text", ""));
                        translation.setTranslatedText(object.optString("translated_text", ""));
                        translation.setSourceLanguage(object.optString("source_language", "auto"));
                        translation.setTargetLanguage(object.optString("target_language", "en"));
                        translation.setFavorite(object.optBoolean("is_favorite", false));

                        Date timestamp = null;
                        String timestampStr = object.optString("timestamp", null);
                        if (!timestampStr.isEmpty()) {
                            try {
                                if (timestampStr.contains(".")) {
                                    timestamp = isoFormat.parse(timestampStr);
                                } else {
                                    SimpleDateFormat noMillisFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault());
                                    noMillisFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                                    timestamp = noMillisFormat.parse(timestampStr);
                                }
                            } catch (Exception ignore) {
                            }
                        }
                        if (timestamp == null) timestamp = new Date();
                        translation.setTimestamp(timestamp);

                        translations.add(translation); // adds translation to the array
                    }
                    callback.onSuccess(translations);
                } else {
                    if (response.code() == 401 || response.code() == 403) {
                        callback.onError(new AuthException("Authentication Error: " + responseBody));
                    } else {
                        callback.onError(new ApiException("Failed to load translations: " + response.code()));
                    }
                }
            } catch (SocketTimeoutException e) {
                callback.onError(new NetworkException("Network Error: Connection timed out."));
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    public void deleteTranslation(Translation translation, Context context, DeleteCallback callback) {
        new Thread(() -> {
            try {
                SessionManager session = new SessionManager(context); // creates new object session
                User user = session.getUser(); // passes user in shared prefs to a new object of user

                if (user == null || user.getId() == null || translation.getId() == null) {
                    callback.onError(new ApiException("Cannot delete: User not logged in or translation ID is null"));
                    return;
                }

                HttpUrl url = Objects.requireNonNull(HttpUrl.parse(SUPABASE_URL + "/rest/v1/translations"))
                        .newBuilder()
                        .addQueryParameter("id", "eq." + translation.getId())
                        .build(); // building query body by getting the value with the id

                Request request = new Request.Builder() // building the request
                        .url(url)
                        .delete()
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .build();

                Response response = client.newCall(request).execute(); // calling the request
                String responseBody = response.body().string();

                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    if (response.code() == 401 || response.code() == 403) {
                        callback.onError(new AuthException("Authentication Error: " + responseBody));
                    } else {
                        callback.onError(new ApiException("Failed to delete translation: " + response.code()));
                    }
                }
            } catch (SocketTimeoutException e) {
                callback.onError(new NetworkException("Network Error: Connection timed out."));
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    } // method to delete previous user translation

    public void updateFavoriteStatus(String translationId, boolean isFavorite, FavoriteCallback callback) {
        new Thread(() -> {
            try {
                HttpUrl url = Objects.requireNonNull(HttpUrl.parse(SUPABASE_URL + "/rest/v1/translations"))
                        .newBuilder()
                        .addQueryParameter("id", "eq." + translationId)
                        .build();

                JSONObject bodyJson = new JSONObject();
                bodyJson.put("is_favorite", isFavorite);
                RequestBody body = RequestBody.create(
                        bodyJson.toString(),
                        MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                        .url(url)
                        .patch(body)
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "return=minimal")
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    if (response.code() == 401 || response.code() == 403) {
                        callback.onError(new AuthException("Authentication Error: " + responseBody));
                    } else {
                        callback.onError(new ApiException("Failed to update favorite: " + response.code()));
                    }
                }
            } catch (SocketTimeoutException e) {
                callback.onError(new NetworkException("Network Error: Connection timed out."));
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    } // Method to update previous translation
}