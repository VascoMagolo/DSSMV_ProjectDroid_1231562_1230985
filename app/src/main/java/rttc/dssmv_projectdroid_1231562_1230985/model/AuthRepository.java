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

                    Response response = this.client.newCall(request).execute();
                    String responseBody = response.body().string();

                    if (response.isSuccessful()) {
                        JSONObject object = new JSONObject(responseBody);
                        if (object.has("id")) {
                            String id = object.getString("id");
                            user.setId(id);
                            createUserProfile(user);
                            registrationResult.postValue(true);
                        }
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

        private void createUserProfile(User user) {
            try{
                JSONObject profileData = new JSONObject();
                profileData.put("id", user.getId());
                profileData.put("name", user.getName());
                profileData.put("email", user.getEmail());

                RequestBody profileBody = RequestBody.create(profileData.toString(), MediaType.parse("application/json"));

                Request profileRequest = new Request.Builder()
                        .url(SUPABASE_URL + "/rest/v1/users")
                        .post(profileBody)
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "request=minimal")
                        .build();

                Response response = this.client.newCall(profileRequest).execute();

            }catch (Exception e){
                e.printStackTrace();
            }


        }

        public LiveData<Boolean> getRegistrationResult() {
            return registrationResult;
        }

        public LiveData<String> getErrorMessage() {
            return errorMessage;
        }
    }