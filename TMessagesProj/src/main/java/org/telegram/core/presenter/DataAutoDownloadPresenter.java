package org.telegram.core.presenter;

import org.telegram.core.data.repository.AutoDownloadRepository;
import org.telegram.core.data.repository.AutoDownloadRepositoryImpl;
import org.telegram.messenger.DownloadController;

import java.util.ArrayList;

public class DataAutoDownloadPresenter {

    public interface DataAutoDownloadView {
    }

    private final int currentAccount;
    private final AutoDownloadRepository repository;
    private DataAutoDownloadView view;

    public DataAutoDownloadPresenter(int currentAccount) {
        this(currentAccount, new AutoDownloadRepositoryImpl(currentAccount));
    }

    public DataAutoDownloadPresenter(int currentAccount, AutoDownloadRepository repository) {
        this.currentAccount = currentAccount;
        this.repository = repository;
    }

    public void attachView(DataAutoDownloadView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public DownloadController.Preset getLowPreset() {
        return repository.getLowPreset();
    }

    public DownloadController.Preset getMediumPreset() {
        return repository.getMediumPreset();
    }

    public DownloadController.Preset getHighPreset() {
        return repository.getHighPreset();
    }

    public DownloadController.Preset getTypePreset(int currentType) {
        return repository.getTypePreset(currentType);
    }

    public int getCurrentPresetNum(int currentType) {
        return repository.getCurrentPresetNum(currentType);
    }

    public DownloadController.Preset getCurrentPreset(int currentType) {
        return repository.getCurrentPreset(currentType);
    }

    public void savePreset(int currentType, DownloadController.Preset typePreset, int currentPresetNum) {
        repository.savePreset(currentType, typePreset, currentPresetNum);
    }

    public void setCurrentPresetNum(int currentType, int currentPresetNum) {
        repository.setCurrentPresetNum(currentType, currentPresetNum);
    }

    public void checkAutodownloadSettings() {
        repository.checkAutodownloadSettings();
    }

    public void savePresetToServer(int currentType) {
        repository.savePresetToServer(currentType);
    }

    public void sortPresets(ArrayList<DownloadController.Preset> presets) {
        repository.sortPresets(presets);
    }
}
