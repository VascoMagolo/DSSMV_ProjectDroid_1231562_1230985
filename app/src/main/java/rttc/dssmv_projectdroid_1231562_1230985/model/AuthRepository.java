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
            User user = new User(name, email, password);
            registerInSupabase(user);
        }

        private void registerInSupabase(User user) {
            new Thread(() -> {
                try {
                    OkHttpClient client = new OkHttpClient();

                    JSONObject data = new JSONObject();
                    data.put("email", user.getEmail());
                    data.put("password", user.getPassword());

                    RequestBody body = RequestBody.create(
                            data.toString(),
                            MediaType.parse("application/json")
                    );

                    Request request = new Request.Builder()
                            .url(BuildConfig.SUPABASE_URL + "/auth/v1/signup")
                            .post(body)
                            .addHeader("apikey", BuildConfig.SUPABASE_KEY)
                            .addHeader("Authorization", "Bearer " + BuildConfig.SUPABASE_KEY)
                            .addHeader("Content-Type", "application/json")
                            .build();

                    Response response = client.newCall(request).execute();
                    String responseBody = response.body().string();

                    if (response.isSuccessful()) {
                        registrationResult.postValue(true);
                    } else {
                        errorMessage.postValue("Erro no registo: " + response.code());
                        registrationResult.postValue(false);
                    }
                } catch (Exception e) {
                    errorMessage.postValue("Erro: " + e.getMessage());
                    registrationResult.postValue(false);
                }
            }).start();
        }

        public LiveData<Boolean> getRegistrationResult() {
            return registrationResult;
        }

        public LiveData<String> getErrorMessage() {
            return errorMessage;
        }
    }