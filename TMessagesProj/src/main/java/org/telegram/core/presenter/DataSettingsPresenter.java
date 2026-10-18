package org.telegram.core.presenter;

import org.telegram.core.data.repository.SettingsRepository;
import org.telegram.core.data.repository.SettingsRepositoryImpl;
import org.telegram.messenger.DownloadController;

public class DataSettingsPresenter {

    public interface DataSettingsView {
        void onAutoDownloadReset();
        void onDraftsCleared();
    }

    private final SettingsRepository settingsRepository;
    private DataSettingsView view;

    public DataSettingsPresenter(int currentAccount) {
        this(new SettingsRepositoryImpl(currentAccount));
    }

    public DataSettingsPresenter(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public void attachView(DataSettingsView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void loadAutoDownloadConfig(boolean force) {
        settingsRepository.loadAutoDownloadConfig(force);
    }

    public void checkAutodownloadSettings() {
        settingsRepository.checkAutodownloadSettings();
    }

    public DownloadController.Preset getPreset(int type) {
        return settingsRepository.getPreset(type);
    }

    public DownloadController.Preset getCurrentPreset(int type) {
        return settingsRepository.getCurrentPreset(type);
    }

    public boolean toggleAutoDownload(int type, boolean checked) {
        return settingsRepository.toggleAutoDownload(type, checked);
    }

    public void resetAutoDownload() {
        settingsRepository.resetAutoDownload();
        if (view != null) {
            view.onAutoDownloadReset();
        }
    }

    public boolean isAutoDownloadResetEnabled() {
        return settingsRepository.isAutoDownloadResetEnabled();
    }

    public boolean isStreamMediaEnabled() {
        return settingsRepository.isStreamMediaEnabled();
    }

    public void toggleStreamMedia() {
        settingsRepository.toggleStreamMedia();
    }

    public boolean isStreamAllVideoEnabled() {
        return settingsRepository.isStreamAllVideoEnabled();
    }

    public void toggleStreamAllVideo() {
        settingsRepository.toggleStreamAllVideo();
    }

    public boolean isStreamMkvEnabled() {
        return settingsRepository.isStreamMkvEnabled();
    }

    public void toggleStreamMkv() {
        settingsRepository.toggleStreamMkv();
    }

    public boolean isSaveStreamMediaEnabled() {
        return settingsRepository.isSaveStreamMediaEnabled();
    }

    public void toggleSaveStreamMedia() {
        settingsRepository.toggleSaveStreamMedia();
    }

    public boolean isAutoplayGifsEnabled() {
        return settingsRepository.isAutoplayGifsEnabled();
    }

    public void toggleAutoplayGifs() {
        settingsRepository.toggleAutoplayGifs();
    }

    public boolean isAutoplayVideoEnabled() {
        return settingsRepository.isAutoplayVideoEnabled();
    }

    public void toggleAutoplayVideo() {
        settingsRepository.toggleAutoplayVideo();
    }

    public int getSaveToGalleryFlags() {
        return settingsRepository.getSaveToGalleryFlags();
    }

    public void toggleSaveToGalleryFlag(int flag) {
        settingsRepository.toggleSaveToGalleryFlag(flag);
    }

    public String getStorageCacheDir() {
        return settingsRepository.getStorageCacheDir();
    }

    public void setStorageCacheDir(String dir) {
        settingsRepository.setStorageCacheDir(dir);
    }

    public int getVoipDataSaving() {
        return settingsRepository.getVoipDataSaving();
    }

    public void setVoipDataSaving(int value) {
        settingsRepository.setVoipDataSaving(value);
    }

    public void clearAllDrafts() {
        settingsRepository.clearAllDrafts(() -> {
            if (view != null) {
                view.onDraftsCleared();
            }
        });
    }
}
