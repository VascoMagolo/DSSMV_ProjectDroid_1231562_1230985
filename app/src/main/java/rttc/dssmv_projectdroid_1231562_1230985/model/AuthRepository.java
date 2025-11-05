package rttc.dssmv_projectdroid_1231562_1230985.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import rttc.dssmv_projectdroid_1231562_1230985.BuildConfig;
import rttc.dssmv_projectdroid_1231562_1230985.utils.SessionManager;

import java.util.Objects;

public class AuthRepository {
    private final MutableLiveData<Boolean> registrationResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loginResult = new MutableLiveData<>();
    private final OkHttpClient client = new OkHttpClient();

    private static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    private static final String SUPABASE_KEY = BuildConfig.SUPABASE_KEY;

    public void RegisterUser(String name, String email, String password) {
        new Thread(() -> {
            try {
                JSONObject userJson = new JSONObject();
                userJson.put("name", name);
                userJson.put("email", email);
                userJson.put("password", password);

                RequestBody body = RequestBody.create(
                        userJson.toString(),
                        MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/rest/v1/users")
                        .post(body)
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "return=minimal")
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                if (response.isSuccessful()) {
                    registrationResult.postValue(true);
                } else {
                    errorMessage.postValue("Registration error: " + response.code() + " -> " + responseBody);
                    registrationResult.postValue(false);
                }

            } catch (Exception e) {
                errorMessage.postValue("Exception: " + e.getMessage());
                registrationResult.postValue(false);
            }
        }).start();
    }

    public void login(Context context, String email, String password) {
        new Thread(() -> {
            try {
                HttpUrl url = Objects.requireNonNull(HttpUrl.parse(SUPABASE_URL + "/rest/v1/users"))
                        .newBuilder()
                        .addQueryParameter("email", "eq." + email)
                        .addQueryParameter("password", "eq." + password)
                        .addQueryParameter("select", "*")
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("Range", "0-9")
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                if (response.isSuccessful()) {
                    JSONArray usersArray = new JSONArray(responseBody);
                    if (usersArray.length() > 0) {
                        JSONObject userObj = usersArray.getJSONObject(0);
                        User user = new User(
                                userObj.optString("name"),
                                userObj.optString("email"),
                                null,
                                userObj.optString("id")
                        );



                        SessionManager session = new SessionManager(context);
                        session.saveUser(user);
                        Log.d("LOGIN_DEBUG", "NAME=" + userObj.optString("name") + " | ID=" + userObj.optString("id"));
                        loginResult.postValue(true);
                    } else {
                        errorMessage.postValue("Invalid email or password ");
                        loginResult.postValue(false);
                    }
                } else {
                    errorMessage.postValue("Login error: " + response.code() + " → " + responseBody);
                    loginResult.postValue(false);
                }

            } catch (Exception e) {
                errorMessage.postValue("Exception during login: " + e.getMessage());
                loginResult.postValue(false);
            }
        }).start();
    }

    private void saveUser(Context context, User user) {
        try {
            JSONObject json = new JSONObject();
            json.put("id", user.getId());
            json.put("name", user.getName());
            json.put("email", user.getEmail());
            json.put("password", user.getPassword());

            SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
            prefs.edit().putString("user_json", json.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LiveData<Boolean> getRegistrationResult() {return registrationResult;}
    public LiveData<String> getErrorMessage() {return errorMessage;}
    public LiveData<Boolean> getLoginResult() {return loginResult;}
}

