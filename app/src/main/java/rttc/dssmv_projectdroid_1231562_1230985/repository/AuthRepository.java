package rttc.dssmv_projectdroid_1231562_1230985.repository;

import android.content.Context;
import android.util.Log;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import rttc.dssmv_projectdroid_1231562_1230985.BuildConfig;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.ApiException;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.AuthException;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.NetworkException;
import rttc.dssmv_projectdroid_1231562_1230985.model.User;
import rttc.dssmv_projectdroid_1231562_1230985.utils.SessionManager;

public class AuthRepository {

    private final OkHttpClient client;
    private static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    private static final String SUPABASE_KEY = BuildConfig.SUPABASE_KEY;

    public interface RegisterCallback { // Callback interface for registration operation
        void onSuccess();
        void onError(Exception e);
    }

    public interface LoginCallback { // Callback interface for login operation
        void onSuccess(User user);
        void onError(Exception e);
    }

    public AuthRepository() { // by default OkHttp waits has a timeout of 10 seconds which sometimes is not enough, this initializes it with a 30 second timeout
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public void RegisterUser(String name, String email, String password, String preferredLanguage, RegisterCallback callback) {
        new Thread(() -> {
            try {
                JSONObject userJson = new JSONObject(); // building a json object with new user infos
                userJson.put("name", name);
                userJson.put("email", email);
                userJson.put("password", password);
                userJson.put("preferred_language", preferredLanguage);

                RequestBody body = RequestBody.create( // building the query for creating/registing a new user
                        userJson.toString(),
                        MediaType.parse("application/json")
                );

                Request request = new Request.Builder() // building the request
                        .url(SUPABASE_URL + "/rest/v1/users")
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
                        callback.onError(new AuthException("Registration failed: " + responseBody));
                    } else if (response.code() == 409) {
                        callback.onError(new AuthException("User with this email already exists."));
                    }else {
                        callback.onError(new ApiException("Registration error: " + response.code() + " -> " + responseBody));
                    }
                }

            } catch (SocketTimeoutException e) {
                callback.onError(new NetworkException("Registration timed out."));
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    } // Register method

    public void login(Context context, String email, String password, LoginCallback callback) {
        new Thread(() -> {
            try {
                HttpUrl url = Objects.requireNonNull(HttpUrl.parse(SUPABASE_URL + "/rest/v1/users")) // building the query 'select * from users where ....'
                        .newBuilder()
                        .addQueryParameter("email", "eq." + email)
                        .addQueryParameter("password", "eq." + password)
                        .addQueryParameter("select", "*")
                        .build();

                Request request = new Request.Builder() // building the request
                        .url(url)
                        .get()
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("Range", "0-9")
                        .build();

                Response response = client.newCall(request).execute(); // calling the request
                String responseBody = response.body().string();

                if (response.isSuccessful()) { // if login is successful creates new user object and saves him in shared prefs
                    JSONArray usersArray = new JSONArray(responseBody);
                    if (usersArray.length() > 0) {
                        JSONObject userObj = usersArray.getJSONObject(0);
                        User user = new User(
                                userObj.optString("name"),
                                userObj.optString("email"),
                                null,
                                userObj.optString("id")
                                ,userObj.optString("preferred_language")
                        );

                        SessionManager session = new SessionManager(context);
                        session.saveUser(user); // saving in shared prefs
                        Log.d("LOGIN_DEBUG", "NAME=" + userObj.optString("name") + " | ID=" + userObj.optString("id"));
                        callback.onSuccess(user);
                    } else {
                        callback.onError(new AuthException("Invalid email or password."));
                    }
                } else {
                    if (response.code() == 401 || response.code() == 403) {
                        callback.onError(new AuthException("Login failed: " + responseBody));
                    } else {
                        callback.onError(new ApiException("Login error: " + response.code() + " → " + responseBody));
                    }
                }
            } catch (SocketTimeoutException e) {
                callback.onError(new NetworkException("Login timed out."));
            } catch (JSONException e) {
                callback.onError(new ApiException("Failed to read user data."));
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    } // Login method

}