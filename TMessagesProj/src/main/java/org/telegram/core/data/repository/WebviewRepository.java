package org.telegram.core.data.repository;

import android.app.Activity;
import org.telegram.messenger.MessageObject;

public interface WebviewRepository {

    interface StatsUrlCallback {
        void onStatsUrlLoaded(String url);
    }

    String getGameShareLink(String botName, String startParam);

    void sendGameTyping(long dialogId);

    void loadStatsUrl(long dialogId, String params, boolean isDark, StatsUrlCallback callback);

    void openGameInBrowser(String urlStr, MessageObject messageObject, Activity parentActivity, String short_name, String username);
}
