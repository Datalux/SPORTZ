package it.crisoftdev.sportz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Objects;


public class SharedPreferencesManager {
    @SuppressLint("StaticFieldLeak")
    private static SharedPreferencesManager mInstance;
    @SuppressLint("StaticFieldLeak")
    private static Context mCtx;

    private static final String SHARED_PREF_NAME = "sportzpref";

    private static final String KEY_UID = "uid";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHOTO_URL = "photo_url";

    private static final String KEY_NO_SENSOR = "no_sensor";



    private SharedPreferencesManager(Context context) {
        mCtx = context;
    }

    public static synchronized SharedPreferencesManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPreferencesManager(context);
        }
        return mInstance;
    }

    public void setUid(String uid){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_UID, uid);
        editor.apply();
    }

    public String getUid(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_UID, "");
    }

    public void setEmail(String email){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    public String getEmail(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_EMAIL, "");
    }

    public void setName(String name){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_NAME, name);
        editor.apply();
    }

    public String getName(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_NAME, "");
    }

    public void setPhotoUrl(String url){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PHOTO_URL, url);
        editor.apply();
    }

    public String getPhotoUrl(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_PHOTO_URL, "");
    }

    public void setNoSensorFlag(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_NO_SENSOR, false);
        editor.apply();
    }

    public boolean getSensorFlag(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_NO_SENSOR, true);
    }

    public float getStepSize(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mCtx);
        return Float.parseFloat(Objects.requireNonNull(pref.getString("pref_step_size", "80")))/100.0f;
    }

    public int getUserHeight(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mCtx);
        return Integer.parseInt(Objects.requireNonNull(pref.getString("pref_height", "170")));
    }

    public int getUserAge(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mCtx);
        return Integer.parseInt(Objects.requireNonNull(pref.getString("pref_age", "18")));
    }

    public float getUserWeight(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mCtx);
        return Float.parseFloat(Objects.requireNonNull(pref.getString("pref_weight", "70")))/100.0f;
    }

    public String getUserSex(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mCtx);
        return pref.getString("pref_mof", "80");
    }

    public int getTrackInterval(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mCtx);
        return Integer.parseInt(Objects.requireNonNull(pref.getString("track", "10")));
    }

    public boolean showNotification(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mCtx);
        return pref.getBoolean("track_notification", true);
    }
}
