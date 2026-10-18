package org.telegram.core.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.DownloadController;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.voip.VoIPHelper;

import java.util.HashMap;
import java.util.List;

public class SettingsRepositoryImpl implements SettingsRepository {

    private final Context applicationContext;
    private final int accountNum;
    private static final String PREFS_NAME = "mainconfig";

    public SettingsRepositoryImpl(Context context) {
        this(context, UserConfig.selectedAccount);
    }

    public SettingsRepositoryImpl(int accountNum) {
        this(ApplicationLoader.applicationContext, accountNum);
    }

    public SettingsRepositoryImpl(Context context, int accountNum) {
        this.applicationContext = context != null ? context.getApplicationContext() : ApplicationLoader.applicationContext;
        this.accountNum = accountNum;
    }

    @Override
    public String getQuickReply(int index) {
        SharedPreferences prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("quick_reply_msg" + index, "");
    }

    @Override
    public void saveQuickReply(int index, String text) {
        SharedPreferences prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
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
        SharedPreferences prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("quick_reply_allow_custom", true);
    }

    @Override
    public void setAllowCustomQuickReply(boolean allow) {
        SharedPreferences prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("quick_reply_allow_custom", allow);
        editor.apply();
    }

    @Override
    public String getDoubleTapReaction() {
        return MediaDataController.getInstance(accountNum).getDoubleTapReaction();
    }

    @Override
    public void setDoubleTapReaction(String reaction) {
        MediaDataController.getInstance(accountNum).setDoubleTapReaction(reaction);
    }

    @Override
    public List<TLRPC.TL_availableReaction> getAvailableReactions() {
        return MediaDataController.getInstance(accountNum).getReactionsList();
    }

    @Override
    public TLRPC.TL_availableReaction getAvailableReaction(String key) {
        HashMap<String, TLRPC.TL_availableReaction> map = MediaDataController.getInstance(accountNum).getReactionsMap();
        return map != null ? map.get(key) : null;
    }

    @Override
    public boolean isPremium() {
        return UserConfig.getInstance(accountNum).isPremium();
    }

    @Override
    public void loadAutoDownloadConfig(boolean force) {
        DownloadController.getInstance(accountNum).loadAutoDownloadConfig(force);
    }

    @Override
    public void checkAutodownloadSettings() {
        DownloadController.getInstance(accountNum).checkAutodownloadSettings();
    }

    @Override
    public DownloadController.Preset getPreset(int type) {
        if (type == 0) {
            return DownloadController.getInstance(accountNum).mobilePreset;
        } else if (type == 1) {
            return DownloadController.getInstance(accountNum).wifiPreset;
        } else {
            return DownloadController.getInstance(accountNum).roamingPreset;
        }
    }

    @Override
    public DownloadController.Preset getCurrentPreset(int type) {
        if (type == 0) {
            return DownloadController.getInstance(accountNum).getCurrentMobilePreset();
        } else if (type == 1) {
            return DownloadController.getInstance(accountNum).getCurrentWiFiPreset();
        } else {
            return DownloadController.getInstance(accountNum).getCurrentRoamingPreset();
        }
    }

    @Override
    public boolean toggleAutoDownload(int type, boolean checked) {
        DownloadController.Preset preset;
        DownloadController.Preset defaultPreset;
        String key;
        String key2;
        int num;
        if (type == 0) {
            preset = DownloadController.getInstance(accountNum).mobilePreset;
            defaultPreset = DownloadController.getInstance(accountNum).mediumPreset;
            key = "mobilePreset";
            key2 = "currentMobilePreset";
            num = 0;
        } else if (type == 1) {
            preset = DownloadController.getInstance(accountNum).wifiPreset;
            defaultPreset = DownloadController.getInstance(accountNum).highPreset;
            key = "wifiPreset";
            key2 = "currentWifiPreset";
            num = 1;
        } else {
            preset = DownloadController.getInstance(accountNum).roamingPreset;
            defaultPreset = DownloadController.getInstance(accountNum).lowPreset;
            key = "roamingPreset";
            key2 = "currentRoamingPreset";
            num = 2;
        }
        if (!checked && preset.enabled) {
            preset.set(defaultPreset);
        } else {
            preset.enabled = !preset.enabled;
        }
        SharedPreferences.Editor editor = MessagesController.getMainSettings(accountNum).edit();
        editor.putString(key, preset.toString());
        editor.putInt(key2, 3);
        editor.commit();

        DownloadController.getInstance(accountNum).checkAutodownloadSettings();
        DownloadController.getInstance(accountNum).savePresetToServer(num);
        return preset.enabled;
    }

    @Override
    public void resetAutoDownload() {
        DownloadController.Preset preset;
        DownloadController.Preset defaultPreset;
        String key;

        SharedPreferences.Editor editor = MessagesController.getMainSettings(accountNum).edit();
        for (int a = 0; a < 3; a++) {
            if (a == 0) {
                preset = DownloadController.getInstance(accountNum).mobilePreset;
                defaultPreset = DownloadController.getInstance(accountNum).mediumPreset;
                key = "mobilePreset";
            } else if (a == 1) {
                preset = DownloadController.getInstance(accountNum).wifiPreset;
                defaultPreset = DownloadController.getInstance(accountNum).highPreset;
                key = "wifiPreset";
            } else {
                preset = DownloadController.getInstance(accountNum).roamingPreset;
                defaultPreset = DownloadController.getInstance(accountNum).lowPreset;
                key = "roamingPreset";
            }
            preset.set(defaultPreset);
            preset.enabled = defaultPreset.isEnabled();
            editor.putInt("currentMobilePreset", DownloadController.getInstance(accountNum).currentMobilePreset = 3);
            editor.putInt("currentWifiPreset", DownloadController.getInstance(accountNum).currentWifiPreset = 3);
            editor.putInt("currentRoamingPreset", DownloadController.getInstance(accountNum).currentRoamingPreset = 3);
            editor.putString(key, preset.toString());
        }
        editor.commit();
        DownloadController.getInstance(accountNum).checkAutodownloadSettings();
        for (int a = 0; a < 3; a++) {
            DownloadController.getInstance(accountNum).savePresetToServer(a);
        }
    }

    @Override
    public boolean isAutoDownloadResetEnabled() {
        DownloadController controller = DownloadController.getInstance(accountNum);
        return !controller.lowPreset.equals(controller.getCurrentRoamingPreset()) || controller.lowPreset.isEnabled() != controller.roamingPreset.enabled ||
                !controller.mediumPreset.equals(controller.getCurrentMobilePreset()) || controller.mediumPreset.isEnabled() != controller.mobilePreset.enabled ||
                !controller.highPreset.equals(controller.getCurrentWiFiPreset()) || controller.highPreset.isEnabled() != controller.wifiPreset.enabled;
    }

    @Override
    public boolean isStreamMediaEnabled() {
        return SharedConfig.streamMedia;
    }

    @Override
    public void toggleStreamMedia() {
        SharedConfig.toggleStreamMedia();
    }

    @Override
    public boolean isStreamAllVideoEnabled() {
        return SharedConfig.streamAllVideo;
    }

    @Override
    public void toggleStreamAllVideo() {
        SharedConfig.toggleStreamAllVideo();
    }

    @Override
    public boolean isStreamMkvEnabled() {
        return SharedConfig.streamMkv;
    }

    @Override
    public void toggleStreamMkv() {
        SharedConfig.toggleStreamMkv();
    }

    @Override
    public boolean isSaveStreamMediaEnabled() {
        return SharedConfig.saveStreamMedia;
    }

    @Override
    public void toggleSaveStreamMedia() {
        SharedConfig.toggleSaveStreamMedia();
    }

    @Override
    public boolean isAutoplayGifsEnabled() {
        return SharedConfig.autoplayGifs;
    }

    @Override
    public void toggleAutoplayGifs() {
        SharedConfig.toggleAutoplayGifs();
    }

    @Override
    public boolean isAutoplayVideoEnabled() {
        return SharedConfig.autoplayVideo;
    }

    @Override
    public void toggleAutoplayVideo() {
        SharedConfig.toggleAutoplayVideo();
    }

    @Override
    public int getSaveToGalleryFlags() {
        return SharedConfig.saveToGalleryFlags;
    }

    @Override
    public void toggleSaveToGalleryFlag(int flag) {
        SharedConfig.toggleSaveToGalleryFlag(flag);
    }

    @Override
    public String getStorageCacheDir() {
        return SharedConfig.storageCacheDir;
    }

    @Override
    public void setStorageCacheDir(String dir) {
        SharedConfig.storageCacheDir = dir;
        SharedConfig.saveConfig();
        ImageLoader.getInstance().checkMediaPaths();
    }

    @Override
    public int getVoipDataSaving() {
        SharedPreferences preferences = MessagesController.getGlobalMainSettings();
        return preferences.getInt("VoipDataSaving", VoIPHelper.getDataSavingDefault());
    }

    @Override
    public void setVoipDataSaving(int value) {
        SharedPreferences preferences = MessagesController.getGlobalMainSettings();
        preferences.edit().putInt("VoipDataSaving", value).commit();
    }

    @Override
    public void clearAllDrafts(Runnable onComplete) {
        TLRPC.TL_messages_clearAllDrafts req = new TLRPC.TL_messages_clearAllDrafts();
        ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            MediaDataController.getInstance(accountNum).clearAllDrafts(true);
            if (onComplete != null) {
                onComplete.run();
            }
        }));
    }
}
