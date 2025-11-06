package rttc.dssmv_projectdroid_1231562_1230985.repository;

import android.content.Context;
import android.util.Log;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rttc.dssmv_projectdroid_1231562_1230985.utils.SessionManager;

import static rttc.dssmv_projectdroid_1231562_1230985.BuildConfig.SUPABASE_KEY;
import static rttc.dssmv_projectdroid_1231562_1230985.BuildConfig.SUPABASE_URL;

public class AccountRepository {

    private final OkHttpClient client;
    public interface DeleteCallback {
        void onSuccess();
        void onError(String message);
    }

    public AccountRepository() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    public void deleteAccount(Context context, String userId, DeleteCallback callback) {
        new Thread(() -> {
            try {
                HttpUrl url = Objects.requireNonNull(HttpUrl.parse(SUPABASE_URL + "/rest/v1/users"))
                        .newBuilder()
                        .addQueryParameter("id", "eq." + userId)
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .delete()
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    SessionManager session = new SessionManager(context);
                    session.clearSession();

                    Log.d("DELETE_ACCOUNT", "User deleted successfully from Supabase.");
                    callback.onSuccess();

                } else {
                    String responseBody = response.body() != null ? response.body().string() : "No error body";
                    Log.e("DELETE_ACCOUNT", "Error deleting user: " + response.code() + " -> " + responseBody);
                    callback.onError("Delete error: " + response.code() + " " + responseBody);
                }

            } catch (Exception e) {
                Log.e("DELETE_ACCOUNT", "Exception during account deletion: " + e.getMessage());
                callback.onError("Exception during delete: " + e.getMessage());
            }
        }).start();
    }
}