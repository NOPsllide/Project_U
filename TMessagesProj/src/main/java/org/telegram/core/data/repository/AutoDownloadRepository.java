package org.telegram.core.data.repository;

import org.telegram.messenger.DownloadController;
import java.util.ArrayList;

public interface AutoDownloadRepository {

    DownloadController.Preset getLowPreset();
    DownloadController.Preset getMediumPreset();
    DownloadController.Preset getHighPreset();
    DownloadController.Preset getTypePreset(int currentType);
    int getCurrentPresetNum(int currentType);
    DownloadController.Preset getCurrentPreset(int currentType);

    void savePreset(int currentType, DownloadController.Preset typePreset, int currentPresetNum);
    void setCurrentPresetNum(int currentType, int currentPresetNum);
    void checkAutodownloadSettings();
    void savePresetToServer(int currentType);
    void sortPresets(ArrayList<DownloadController.Preset> presets);
}
