package rttc.dssmv_projectdroid_1231562_1230985.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import okhttp3.*;
import org.json.JSONObject;
import rttc.dssmv_projectdroid_1231562_1230985.BuildConfig;
import java.io.IOException;
public class AuthRepository {
    private MutableLiveData<Boolean> registrationResult = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private OkHttpClient client = new OkHttpClient();

    private static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    private static final String SUPABASE_KEY = BuildConfig.SUPABASE_KEY;

    public void RegisterUser(String email, String password, String name) {
        new Thread(() -> {
            try {
             User user = new User(name, email, password);

             boolean result = registerInSupabase(user);
             registrationResult.postValue(result);
            } catch (Exception e) {
                errorMessage.postValue(e.getMessage());
                registrationResult.postValue(false);
            }
        }).start();
    }

    private boolean registerInSupabase(User user) {
        try{
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("email", user.getEmail());
            jsonBody.put("password", user.getPassword());

            RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(SUPABASE_URL + "auth/v1/signup")
                    .post(body)
                    .addHeader("apikey", SUPABASE_KEY)
                    .addHeader("Authorization","Bearer " + SUPABASE_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();

            if (response.isSuccessful()) {
                return true;
            }else{
                errorMessage.postValue(responseBody);
                return false;
            }
        }catch(Exception e){
            errorMessage.postValue(e.getMessage());
            return false;
        }
    }

    public LiveData<Boolean> getRegistrationResult() {
        return registrationResult ;
    }
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}