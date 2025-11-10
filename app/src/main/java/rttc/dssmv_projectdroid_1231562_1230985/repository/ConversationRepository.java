package rttc.dssmv_projectdroid_1231562_1230985.repository;

import android.content.Context;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

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
import rttc.dssmv_projectdroid_1231562_1230985.model.Conversation;
import rttc.dssmv_projectdroid_1231562_1230985.model.User;
import rttc.dssmv_projectdroid_1231562_1230985.utils.SessionManager;

public class ConversationRepository {

    private final OkHttpClient client;
    private static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    private static final String SUPABASE_KEY = BuildConfig.SUPABASE_KEY;

    public interface SaveCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface LoadCallback {
        void onSuccess(List<Conversation> conversations);
        void onError(Exception e);
    }

    public interface DeleteCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface FavoriteCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public ConversationRepository() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public void saveConversation(Conversation conversation, Context context, SaveCallback callback) {
        new Thread(() -> {
            try {
                SessionManager session = new SessionManager(context);
                User user = session.getUser();

                if (user == null || user.getId() == null) {
                    callback.onSuccess();
                    return;
                }

                conversation.setUserId(user.getId());

                Date ts = conversation.getTimestamp();
                if (ts == null) {
                    ts = new Date();
                }

                JSONObject conversationBody = new JSONObject();
                conversationBody.put("user_id", conversation.getUserId());
                conversationBody.put("original_text", conversation.getOriginalText());
                conversationBody.put("translated_text", conversation.getTranslatedText());
                conversationBody.put("source_language", conversation.getSourceLanguage());
                conversationBody.put("target_language", conversation.getTargetLanguage());
                conversationBody.put("is_favorite", conversation.getFavorite());

                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
                isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                conversationBody.put("timestamp", isoFormat.format(ts));

                RequestBody body = RequestBody.create(conversationBody.toString(), MediaType.parse("application/json"));

                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/rest/v1/conversations")
                        .post(body)
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
                        callback.onError(new ApiException("Failed to save conversation: " + response.code()));
                    }
                }
            } catch (SocketTimeoutException e) {
                callback.onError(new NetworkException("Network Error: Connection timed out."));
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    public void loadConversations(Context context, LoadCallback callback) {
        new Thread(() -> {
            try {
                SessionManager session = new SessionManager(context);
                User user = session.getUser();

                if (user == null || user.getId() == null) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }

                HttpUrl url = Objects.requireNonNull(HttpUrl.parse(SUPABASE_URL + "/rest/v1/conversations"))
                        .newBuilder()
                        .addQueryParameter("user_id", "eq." + user.getId())
                        .addQueryParameter("select", "*")
                        .addQueryParameter("order", "is_favorite.desc,timestamp.desc")
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
                    List<Conversation> conversations = new ArrayList<>();
                    JSONArray conversationsArray = new JSONArray(responseBody);
                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
                    isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                    for (int i = 0; i < conversationsArray.length(); i++) {
                        JSONObject object = conversationsArray.getJSONObject(i);

                        Conversation conversation = new Conversation();
                        conversation.setId(object.optString("id", null)); // <-- O ID crucial
                        conversation.setUserId(object.optString("user_id", null));
                        conversation.setOriginalText(object.optString("original_text", ""));
                        conversation.setTranslatedText(object.optString("translated_text", ""));
                        conversation.setSourceLanguage(object.optString("source_language", "auto"));
                        conversation.setTargetLanguage(object.optString("target_language", "en"));
                        conversation.setFavorite(object.optBoolean("is_favorite", false));

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
                        conversation.setTimestamp(timestamp);

                        conversations.add(conversation);
                    }
                    callback.onSuccess(conversations);
                } else {
                    if (response.code() == 401 || response.code() == 403) {
                        callback.onError(new AuthException("Authentication Error: " + responseBody));
                    } else {
                        callback.onError(new ApiException("Failed to load conversations: " + response.code()));
                    }
                }
            } catch (SocketTimeoutException e) {
                callback.onError(new NetworkException("Network Error: Connection timed out."));
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    public void deleteConversation(Conversation conversation, Context context, DeleteCallback callback) {
        new Thread(() -> {
            try {
                SessionManager session = new SessionManager(context);
                User user = session.getUser();

                if (user == null || user.getId() == null || conversation.getId() == null) {
                    callback.onError(new ApiException("Cannot delete: User not logged in or conversation ID is null"));
                    return;
                }

                HttpUrl url = Objects.requireNonNull(HttpUrl.parse(SUPABASE_URL + "/rest/v1/conversations"))
                        .newBuilder()
                        .addQueryParameter("id", "eq." + conversation.getId())
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .delete()
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    if (response.code() == 401 || response.code() == 403) {
                        callback.onError(new AuthException("Authentication Error: " + responseBody));
                    } else {
                        callback.onError(new ApiException("Failed to delete conversation: " + response.code()));
                    }
                }
            } catch (SocketTimeoutException e) {
                callback.onError(new NetworkException("Network Error: Connection timed out."));
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    public void updateFavoriteStatus(String conversationId, boolean isFavorite, FavoriteCallback callback) {
        new Thread(() -> {
            try {
                HttpUrl url = Objects.requireNonNull(HttpUrl.parse(SUPABASE_URL + "/rest/v1/conversations"))
                        .newBuilder()
                        .addQueryParameter("id", "eq." + conversationId)
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
    }
}