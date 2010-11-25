package com.mandorf.coffeegutchi;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {

    public static final String KEY_NAME = "name";


    private static final String PREFERENCE = "com.mandorf.coffeegutchi";

    private static PreferencesManager sInstance = null;
    private static SharedPreferences sPrefs = null;


    private PreferencesManager(Context context) {
        if(sPrefs == null) {
            sPrefs = context.getSharedPreferences(PREFERENCE,
                    Context.MODE_PRIVATE);
        }
    }

    public static PreferencesManager getInstance(Context context){
        if(sInstance == null) {
            sInstance = new PreferencesManager(context);
        }

        return sInstance;
    }

    public String getString(String key) {
        return sPrefs.getString(key, null);
    }

    public void setString(String key, String value) {
        sPrefs.edit().putString(key, value).commit();
    }

}
