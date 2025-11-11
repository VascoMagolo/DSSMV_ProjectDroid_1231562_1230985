package rttc.dssmv_projectdroid_1231562_1230985.repository;

import android.content.Context;
import android.util.Log;

import java.net.SocketTimeoutException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.AuthException;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.NetworkException;
import rttc.dssmv_projectdroid_1231562_1230985.model.User;
import rttc.dssmv_projectdroid_1231562_1230985.utils.SessionManager;

import static rttc.dssmv_projectdroid_1231562_1230985.BuildConfig.SUPABASE_KEY;
import static rttc.dssmv_projectdroid_1231562_1230985.BuildConfig.SUPABASE_URL;

public class AccountRepository {

    private final OkHttpClient client;

    public interface DeleteCallback {
        void onSuccess();
        void onError(Exception e);
    } // Callback interface for delete operation

    public AccountRepository() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    } // Initialize OkHttpClient with timeouts
    public void deleteAccount(Context context, String userId, DeleteCallback callback) {
        new Thread(() -> {
            try {
                HttpUrl url = Objects.requireNonNull(HttpUrl.parse(SUPABASE_URL + "/rest/v1/users"))
                        .newBuilder()
                        .addQueryParameter("id", "eq." + userId) // Filter to delete specific user by ID
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
                    SessionManager session = new SessionManager(context);
                    session.clearSession();

                    Log.d("DELETE_ACCOUNT", "User deleted successfully from Supabase.");
                    callback.onSuccess();

                } else {
                    Log.e("DELETE_ACCOUNT", "Error deleting user: " + response.code() + " -> " + responseBody);

                    if (response.code() == 401 || response.code() == 403) {
                        callback.onError(new AuthException("Authentication error: " + responseBody));
                    } else {
                        callback.onError(new Exception("Delete error: " + response.code() + " " + responseBody));
                    }
                }

            } catch (SocketTimeoutException e) {
                Log.e("DELETE_ACCOUNT", "Network timeout: " + e.getMessage());
                callback.onError(new NetworkException("Connection timed out. Please try again."));

            } catch (Exception e) {
                Log.e("DELETE_ACCOUNT", "Exception during account deletion: " + e.getMessage());
                callback.onError(e);
            }
        }).start();
    } // Method to delete user account

    public interface UpdateCallback {
        void onSuccess();
        void onError(Exception e);
    } // Callback interface for update operation

    public void updateAccount(Context context, User user, UpdateCallback callback) {
        String userId = user.getId();
        new Thread(() -> {
            try {
                HttpUrl url = Objects.requireNonNull(HttpUrl.parse(SUPABASE_URL + "/rest/v1/users"))
                        .newBuilder()
                        .addQueryParameter("id", "eq." + userId) // Filter to update specific user by ID
                        .build();


                JSONObject bodyJson = new JSONObject();
                bodyJson.put("name", user.getName());
                bodyJson.put("preferred_language", user.getPreferredLanguage());
                // Add other fields as necessary
                RequestBody body = RequestBody.create(
                        bodyJson.toString(),
                        MediaType.parse("application/json; charset=utf-8")
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
                    SessionManager session = new SessionManager(context);
                    session.saveUser(user);

                    Log.d("UPDATE_ACCOUNT", "User updated successfully on Supabase.");
                    callback.onSuccess();

                } else {
                    Log.e("UPDATE_ACCOUNT", "Error updating user: " + response.code() + " -> " + responseBody);
                    if (response.code() == 401 || response.code() == 403) {
                        callback.onError(new AuthException("Authentication error: " + responseBody));
                    } else {
                        callback.onError(new Exception("Update error: " + response.code() + " " + responseBody));
                    }
                }
            } catch (SocketTimeoutException e) {
                Log.e("UPDATE_ACCOUNT", "Network timeout: " + e.getMessage());
                callback.onError(new NetworkException("Connection timed out. Please try again."));

            } catch (Exception e) {
                Log.e("UPDATE_ACCOUNT", "Exception during account update: " + e.getMessage());
                callback.onError(e);
            }
        }).start();
    } // Method to update user account for later use
}