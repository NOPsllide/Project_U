package org.telegram.core.data.repository;

import org.telegram.messenger.DownloadController;
import org.telegram.tgnet.TLRPC;
import java.util.List;

public interface SettingsRepository {

    String getQuickReply(int index);

    void saveQuickReply(int index, String text);

    boolean getAllowCustomQuickReply();

    void setAllowCustomQuickReply(boolean allow);

    String getDoubleTapReaction();

    void setDoubleTapReaction(String reaction);

    List<TLRPC.TL_availableReaction> getAvailableReactions();

    TLRPC.TL_availableReaction getAvailableReaction(String key);

    boolean isPremium();

    void loadAutoDownloadConfig(boolean force);

    void checkAutodownloadSettings();

    DownloadController.Preset getPreset(int type);

    DownloadController.Preset getCurrentPreset(int type);

    boolean toggleAutoDownload(int type, boolean checked);

    void resetAutoDownload();

    boolean isAutoDownloadResetEnabled();

    boolean isStreamMediaEnabled();

    void toggleStreamMedia();

    boolean isStreamAllVideoEnabled();

    void toggleStreamAllVideo();

    boolean isStreamMkvEnabled();

    void toggleStreamMkv();

    boolean isSaveStreamMediaEnabled();

    void toggleSaveStreamMedia();

    boolean isAutoplayGifsEnabled();

    void toggleAutoplayGifs();

    boolean isAutoplayVideoEnabled();

    void toggleAutoplayVideo();

    int getSaveToGalleryFlags();

    void toggleSaveToGalleryFlag(int flag);

    String getStorageCacheDir();

    void setStorageCacheDir(String dir);

    int getVoipDataSaving();

    void setVoipDataSaving(int value);

    void clearAllDrafts(Runnable onComplete);
}
