package org.telegram.core.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class SettingsRepositoryImpl implements SettingsRepository {

    private final Context context;
    private static final String PREFS_NAME = "mainconfig";

    public SettingsRepositoryImpl(Context context) {
        this.context = context;
    }

    @Override
    public String getQuickReply(int index) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("quick_reply_msg" + index, "");
    }

    @Override
    public void saveQuickReply(int index, String text) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (!TextUtils.isEmpty(text)) {
            editor.putString("quick_reply_msg" + index, text);
        } else {
            editor.remove("quick_reply_msg" + index);
        }
        editor.apply();
    }

    @Override
    public boolean getAllowCustomQuickReply() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("quick_reply_allow_custom", true);
    }

    @Override
    public void setAllowCustomQuickReply(boolean allow) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("quick_reply_allow_custom", allow);
        editor.apply();
    }
}
