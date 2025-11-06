package rttc.dssmv_projectdroid_1231562_1230985.model;

import android.os.Build;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import rttc.dssmv_projectdroid_1231562_1230985.BuildConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
public class PhraseRepository {
    private final OkHttpClient client = new OkHttpClient();
    private static final String SUPABASE_KEY = BuildConfig.SUPABASE_KEY;
    private static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;

    private final MutableLiveData<List<GenericPhrase>> _phrases = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage =  new MutableLiveData<>();

    public void loadPhrasesByLanguage(String language) {
        new Thread(() -> {
            try{
                HttpUrl url = Objects.requireNonNull(HttpUrl.parse(SUPABASE_URL + "/rest/v1/generic_phrases"))
                        .newBuilder()
                        .addQueryParameter("language", "eq." + language)
                        .addQueryParameter("select" , "*")
                        .addQueryParameter("order", "category.asc")
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body() != null ? response.body().string() : "";

                if(response.isSuccessful()){
                    List<GenericPhrase> phrases = new ArrayList<>();
                    JSONArray phrasesArray = new JSONArray(responseBody);

                    for(int i = 0; i < phrasesArray.length(); i++){
                        JSONObject object = phrasesArray.getJSONObject(i);
                        GenericPhrase phrase = new GenericPhrase();
                        phrase.setId(object.getString("id"));
                        phrase.setLanguage(object.getString("language"));
                        phrase.setCategory(object.getString("category"));
                        phrase.setText(object.getString("text"));
                        phrases.add(phrase);
                    }

                    _phrases.postValue(phrases);
                }else{
                    _errorMessage.postValue("Error loading phrases: " + response.code());
                }
            }catch(Exception e){
                _errorMessage.postValue("Exception " + e.getMessage());
            }
        }).start();
    }

    public LiveData<List<GenericPhrase>> getPhrases() {
        return  _phrases;
    }
    public LiveData<String> getErrorMessage() {
        return _errorMessage;
    }
}
