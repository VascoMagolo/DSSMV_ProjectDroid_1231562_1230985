package rttc.dssmv_projectdroid_1231562_1230985.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import rttc.dssmv_projectdroid_1231562_1230985.model.User;

public class SessionManager {

    private static final String PREF_NAME = "auth";
    private static final String KEY_USER = "user_json";
    private static final String KEY_TOKEN = "access_token";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUser(User user) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", user.getId());
            obj.put("name", user.getName());
            obj.put("email", user.getEmail());
            prefs.edit().putString(KEY_USER, obj.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public User getUser() {
        try {
            String jsonStr = prefs.getString(KEY_USER, null);
            if (jsonStr == null) return null;

            JSONObject obj = new JSONObject(jsonStr);
            return new User(
                    obj.optString("name"),
                    obj.optString("email"),
                    null,
                    obj.optString("id")
            );
        } catch (Exception e) {
            return null;
        }
    }
    public void clearSession() {
        prefs.edit().clear().apply();
    }

}
