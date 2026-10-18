package org.telegram.core.data.repository;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.FeedWidgetProvider;
import org.telegram.messenger.MessagesStorage;

import java.util.ArrayList;

public class WidgetRepositoryImpl implements WidgetRepository {

    @Override
    public int extractAppWidgetId(Intent intent) {
        if (intent == null) {
            return AppWidgetManager.INVALID_APPWIDGET_ID;
        }
        Bundle extras = intent.getExtras();
        if (extras != null) {
            return extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        return AppWidgetManager.INVALID_APPWIDGET_ID;
    }

    @Override
    public void saveFeedWidgetConfig(Context context, int account, int appWidgetId, ArrayList<MessagesStorage.TopicKey> dids) {
        if (context == null || dids == null || dids.isEmpty()) {
            return;
        }
        AccountInstance.getInstance(account).getMessagesStorage().putWidgetDialogs(appWidgetId, dids);

        SharedPreferences preferences = context.getSharedPreferences("shortcut_widget", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("account" + appWidgetId, account);
        editor.putLong("dialogId" + appWidgetId, dids.get(0).dialogId);
        editor.commit();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        FeedWidgetProvider.updateWidget(context, appWidgetManager, appWidgetId);
    }

    @Override
    public void saveWidgetDialogs(int account, int appWidgetId, ArrayList<MessagesStorage.TopicKey> dids) {
        AccountInstance.getInstance(account).getMessagesStorage().putWidgetDialogs(appWidgetId, dids);
    }
}
