package rttc.dssmv_projectdroid_1231562_1230985.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;
import rttc.dssmv_projectdroid_1231562_1230985.model.User;

/**
 * Class that manages user's session using SharedPreferences
 * Can save, get and clear the logged-in user's info
 */
public class SessionManager {

    private static final String PREF_NAME = "auth";
    private static final String KEY_USER = "user_json";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }


    /**
     * Saves User object on SharedPreferences
     * First converts to a json string
     * @param user User object to save
     */
    public void saveUser(User user) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", user.getId());
            obj.put("name", user.getName());
            obj.put("email", user.getEmail());
            obj.put("preferred_language", user.getPreferredLanguage());
            prefs.edit().putString(KEY_USER, obj.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the currently logged-in User from SharedPrefs
     * Parses the string back to a User object
     * @return The user objects, or returns null if no user is saved
     */
    public User getUser() {
        try {
            String jsonStr = prefs.getString(KEY_USER, null);
            if (jsonStr == null) return null;

            JSONObject obj = new JSONObject(jsonStr);
            return new User(
                    obj.optString("name"),
                    obj.optString("email"),
                    null,
                    obj.optString("id"),
                    obj.optString("preferred_language", "en")
            );
        } catch (Exception e) {
            return null;
        }
    }


    public boolean isLoggedIn(){
        return getUser() != null;
    } // checks if user session exists
    public void clearSession() {
        prefs.edit().clear().apply();
    } // clears the currently logged-in user session, clear all data from SharedPrefs
}