package rttc.dssmv_projectdroid_1231562_1230985.repository;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import okhttp3.*;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import rttc.dssmv_projectdroid_1231562_1230985.BuildConfig;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.ApiException;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.AuthException;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.NetworkException;
import rttc.dssmv_projectdroid_1231562_1230985.model.GenericPhrase;
import rttc.dssmv_projectdroid_1231562_1230985.utils.SessionManager;
import rttc.dssmv_projectdroid_1231562_1230985.model.User;

/**
 * Repository class for managing user-specific phrases in Supabase.
 * Handles CRUD operations for user phrases.
 * Private to the user
 */
public class UserPhraseRepository {
    private final OkHttpClient client;
    private static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    private static final String SUPABASE_KEY = BuildConfig.SUPABASE_KEY;
    private SessionManager sessionManager;

    /** Callback for loading a list of phrases */
    public interface LoadUserPhrasesCallback {
        void onSuccess(List<GenericPhrase> userPhrases);
        void onError(Exception e);
    }
    /** Callback for saving phrase */
    public interface SaveCallback {
        void onSuccess();
        void onError(Exception e);
    }
    /** Callback for deleting phrase */
    public interface DeleteCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public UserPhraseRepository() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    
    public UserPhraseRepository(Context context) {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.sessionManager = new SessionManager(context);
    }

    /**
     * Fetches/Loads user personal phrases from Supabase, filtered by language
     * @param context Context to get current user id
     * @param initialLanguage Language to filter phrases
     * @param callback Returns list of user phrases or error
     */
    public void loadUserPhrases(Context context, String initialLanguage, LoadUserPhrasesCallback callback) {
        new Thread(() -> {
            Response response = null;
            try {
                SessionManager session = sessionManager != null ? sessionManager : new SessionManager(context);
                User user = session.getUser();

                if (user == null || user.getId() == null) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }

                HttpUrl url = Objects.requireNonNull(HttpUrl.parse(SUPABASE_URL + "/rest/v1/user_phrases"))
                        .newBuilder()
                        .addQueryParameter("user_id", "eq." + user.getId())
                        .addQueryParameter("language", "eq." + initialLanguage)
                        .addQueryParameter("select" , "*")
                        .addQueryParameter("order", "created_at.desc")
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .build();

                response = client.newCall(request).execute();
                String responseBody = response.body().string();

                if (response.isSuccessful()) {
                    List<GenericPhrase> userPhrases = new ArrayList<>();
                    JSONArray phrasesArray = new JSONArray(responseBody);

                    for (int i = 0; i < phrasesArray.length(); i++) {
                        JSONObject object = phrasesArray.getJSONObject(i);
                        GenericPhrase phrase = new GenericPhrase();
                        phrase.setId(object.getString("id"));
                        phrase.setUserId(object.getString("user_id"));
                        phrase.setCategory(object.optString("category", "Personal"));
                        phrase.setText(object.getString("text"));
                        phrase.setLanguage(object.optString("language", ""));
                        phrase.setUserPhrase(true);
                        userPhrases.add(phrase);
                    }
                    callback.onSuccess(userPhrases);
                } else {
                    callback.onError(new ApiException("Error loading user phrases: " + response.code()));
                }
            } catch (SocketTimeoutException e) {
                callback.onError(new NetworkException("Network Error: Connection timed out."));
            } catch (JSONException e) {
                callback.onError(new ApiException("Data Error: Failed to read user phrases."));
            } catch (Exception e) {
                callback.onError(e);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        }).start();
    }

    /**
     * Saves a user personal phrase to Supabase on 'user_phrases' table
     * @param phrase Phrase to save
     * @param context Context to get current user id
     * @param callback Callback that reports success or error
     */
    public void saveUserPhrase(GenericPhrase phrase, Context context, SaveCallback callback) {
        new Thread(() -> {
            Response response = null;
            try {
                SessionManager session = sessionManager != null ? sessionManager : new SessionManager(context);
                User user = session.getUser();
                if (user == null || user.getId() == null) {
                    callback.onError(new AuthException("User not logged in."));
                    return;
                }

                JSONObject bodyJson = new JSONObject();
                bodyJson.put("user_id", user.getId());
                bodyJson.put("text", phrase.getText());
                bodyJson.put("category", phrase.getCategory());
                bodyJson.put("language", phrase.getLanguage());

                RequestBody body = RequestBody.create(
                        bodyJson.toString(),
                        MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/rest/v1/user_phrases")
                        .post(body)
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "return=minimal")
                        .build();

                response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError(new ApiException("Failed to save phrase: " + response.code()));
                }
            } catch (Exception e) {
                callback.onError(e);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        }).start();
    }

    /**
     * Checks that the phrase belongs to the user and deletes it from Supabase on 'user_phrases' table
     * @param phrase Phrase to delete
     * @param context Context to get current user id
     * @param callback Callback that reports success or error
     */
    public void deleteUserPhrase(GenericPhrase phrase, Context context, DeleteCallback callback) {
        new Thread(() -> {
            Response response = null;
            try {
                SessionManager session = sessionManager != null ? sessionManager : new SessionManager(context);
                User user = session.getUser();
                if (user == null || user.getId() == null) {
                    callback.onError(new AuthException("User not logged in."));
                    return;
                }

                if (phrase.getId() == null) {
                    callback.onError(new ApiException("Phrase ID is null. Cannot delete."));
                    return;
                }

                HttpUrl url = Objects.requireNonNull(HttpUrl.parse(SUPABASE_URL + "/rest/v1/user_phrases"))
                        .newBuilder()
                        .addQueryParameter("id", "eq." + phrase.getId())
                        .addQueryParameter("user_id", "eq." + user.getId())
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .delete()
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .build();

                response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError(new ApiException("Failed to delete phrase: " + response.code()));
                }
            } catch (Exception e) {
                callback.onError(e);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        }).start();
    }
}