package rttc.dssmv_projectdroid_1231562_1230985.repository;

import android.content.Context;
import androidx.lifecycle.MutableLiveData;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import rttc.dssmv_projectdroid_1231562_1230985.BuildConfig;
import rttc.dssmv_projectdroid_1231562_1230985.model.Conversation;
import rttc.dssmv_projectdroid_1231562_1230985.model.User;
import rttc.dssmv_projectdroid_1231562_1230985.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ConversationRepository {

    private final OkHttpClient client = new OkHttpClient();
    private static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    private static final String SUPABASE_KEY = BuildConfig.SUPABASE_KEY;

    private final MutableLiveData<List<Conversation>> _conversations = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _saveResult = new MutableLiveData<>();

    private String formatDateToISO(Date date) {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return isoFormat.format(date);
    }

    public void saveConversation(Conversation conversation, Context context) {
        new Thread(() -> {
            try {
                SessionManager session = new SessionManager(context);
                User user = session.getUser();

                if (user == null || user.getId() == null) {
                    _saveResult.postValue(false);
                    return;
                }

                conversation.setUserId(user.getId());

                Date ts = conversation.getTimestamp();
                if (ts == null) {
                    ts = new Date();
                    conversation.setTimestamp(ts);
                }

                JSONObject conversationBody = new JSONObject();
                conversationBody.put("user_id", conversation.getUserId());
                conversationBody.put("original_text", conversation.getOriginalText() != null ? conversation.getOriginalText() : "");
                conversationBody.put("translated_text", conversation.getTranslatedText() != null ? conversation.getTranslatedText() : "");
                conversationBody.put("source_language", conversation.getSourceLanguage() != null ? conversation.getSourceLanguage() : "auto");
                conversationBody.put("target_language", conversation.getTargetLanguage() != null ? conversation.getTargetLanguage() : "en");
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
                String respBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    _saveResult.postValue(true);
                    loadConversations(context);
                } else {
                    _errorMessage.postValue("Failed to save conversation: " + response.code() + " - " + respBody);
                    _saveResult.postValue(false);
                }
            } catch (Exception e) {
                _errorMessage.postValue(e.getMessage());
                _saveResult.postValue(false);
            }
        }).start();
    }

    public void loadConversations(Context context) {
        new Thread(() -> {
            try {
                SessionManager session = new SessionManager(context);
                User user = session.getUser();

                if (user == null || user.getId() == null) {
                    _conversations.postValue(new ArrayList<>());
                    return;
                }

                HttpUrl url = Objects.requireNonNull(HttpUrl.parse(SUPABASE_URL + "/rest/v1/conversations"))
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
                    List<Conversation> conversations = new ArrayList<>();
                    JSONArray conversationsArray = new JSONArray(responseBody);
                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
                    isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                    for (int i = 0; i < conversationsArray.length(); i++) {
                        JSONObject object = conversationsArray.getJSONObject(i);
                        Conversation conversation = new Conversation();
                        conversation.setId(object.optString("id", null));
                        conversation.setUserId(object.optString("user_id", null));
                        conversation.setOriginalText(object.optString("original_text", ""));
                        conversation.setTranslatedText(object.optString("translated_text", ""));
                        conversation.setSourceLanguage(object.optString("source_language", "auto"));
                        conversation.setTargetLanguage(object.optString("target_language", "en"));

                        Date timestamp = null;
                        String timestampStr = object.optString("timestamp", null);
                        if (timestampStr != null && !timestampStr.isEmpty()) {
                            try {
                                timestamp = isoFormat.parse(timestampStr);
                            } catch (Exception ignore) {
                                timestamp = null;
                            }
                        }
                        if (timestamp == null) timestamp = new Date();
                        conversation.setTimestamp(timestamp);

                        conversations.add(conversation);
                    }
                    _conversations.postValue(conversations);
                } else {
                    _errorMessage.postValue("Failed to load conversation: " + response.code() + " - " + responseBody);
                }
            } catch (Exception e) {
                _errorMessage.postValue(e.getMessage());
            }
        }).start();
    }

    public MutableLiveData<List<Conversation>> getConversations() {
        return _conversations;
    }

    public MutableLiveData<String> getErrorMessage() {
        return _errorMessage;
    }

    public MutableLiveData<Boolean> getSaveResult() {
        return _saveResult;
    }
}

