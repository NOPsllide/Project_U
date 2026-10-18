package org.telegram.core.data.repository;

import android.app.Activity;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLRPC;

import java.net.URLEncoder;

public class WebviewRepositoryImpl implements WebviewRepository {

    private final int currentAccount;

    public WebviewRepositoryImpl(int currentAccount) {
        this.currentAccount = currentAccount;
    }

    @Override
    public String getGameShareLink(String botName, String startParam) {
        return "https://" + MessagesController.getInstance(currentAccount).linkPrefix + "/" + botName + (TextUtils.isEmpty(startParam) ? "" : "?game=" + startParam);
    }

    @Override
    public void sendGameTyping(long dialogId) {
        MessagesController.getInstance(currentAccount).sendTyping(dialogId, 0, 6, 0);
    }

    @Override
    public void loadStatsUrl(long dialogId, String params, boolean isDark, StatsUrlCallback callback) {
        TLRPC.TL_messages_getStatsURL req = new TLRPC.TL_messages_getStatsURL();
        req.peer = MessagesController.getInstance(currentAccount).getInputPeer(dialogId);
        req.params = params != null ? params : "";
        req.dark = isDark;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (callback != null) {
                if (response != null) {
                    TLRPC.TL_statsURL url = (TLRPC.TL_statsURL) response;
                    callback.onStatsUrlLoaded(url.url);
                } else {
                    callback.onStatsUrlLoaded(null);
                }
            }
        }));
    }

    @Override
    public void openGameInBrowser(String urlStr, MessageObject messageObject, Activity parentActivity, String short_name, String username) {
        if (messageObject == null || parentActivity == null) {
            return;
        }
        try {
            String url = urlStr;
            SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("botshare", Activity.MODE_PRIVATE);
            String existing = sharedPreferences.getString("" + messageObject.getId(), null);
            StringBuilder hash = new StringBuilder(existing != null ? existing : "");
            StringBuilder addHash = new StringBuilder("tgShareScoreUrl=" + URLEncoder.encode("tgb://share_game_score?hash=", "UTF-8"));
            if (existing == null) {
                final char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
                for (int i = 0; i < 20; i++) {
                    hash.append(chars[Utilities.random.nextInt(chars.length)]);
                }
            }
            addHash.append(hash);
            int index = url.indexOf('#');
            if (index < 0) {
                url += "#" + addHash;
            } else {
                String curHash = url.substring(index + 1);
                if (curHash.indexOf('=') >= 0 || curHash.indexOf('?') >= 0) {
                    url += "&" + addHash;
                } else {
                    if (curHash.length() > 0) {
                        url += "?" + addHash;
                    } else {
                        url += addHash;
                    }
                }
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(hash + "_date", (int) (System.currentTimeMillis() / 1000));
            SerializedData serializedData = new SerializedData(messageObject.messageOwner.getObjectSize());
            messageObject.messageOwner.serializeToStream(serializedData);
            editor.putString(hash + "_m", Utilities.bytesToHex(serializedData.toByteArray()));
            int account = messageObject.currentAccount >= 0 ? messageObject.currentAccount : currentAccount;
            editor.putString(hash + "_link", "https://" + MessagesController.getInstance(account).linkPrefix + "/" + username + (TextUtils.isEmpty(short_name) ? "" : "?game=" + short_name));
            editor.commit();
            Browser.openUrl(parentActivity, url, false);
            serializedData.cleanup();
        } catch (Exception e) {
            FileLog.e(e);
        }
    }
}
