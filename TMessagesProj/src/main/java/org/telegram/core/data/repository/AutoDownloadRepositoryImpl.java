package org.telegram.core.data.repository;

import android.content.SharedPreferences;

import org.telegram.messenger.DownloadController;
import org.telegram.messenger.MessagesController;

import java.util.ArrayList;
import java.util.Collections;

public class AutoDownloadRepositoryImpl implements AutoDownloadRepository {

    private final int currentAccount;

    public AutoDownloadRepositoryImpl(int currentAccount) {
        this.currentAccount = currentAccount;
    }

    @Override
    public DownloadController.Preset getLowPreset() {
        return DownloadController.getInstance(currentAccount).lowPreset;
    }

    @Override
    public DownloadController.Preset getMediumPreset() {
        return DownloadController.getInstance(currentAccount).mediumPreset;
    }

    @Override
    public DownloadController.Preset getHighPreset() {
        return DownloadController.getInstance(currentAccount).highPreset;
    }

    @Override
    public DownloadController.Preset getTypePreset(int currentType) {
        if (currentType == 0) {
            return DownloadController.getInstance(currentAccount).mobilePreset;
        } else if (currentType == 1) {
            return DownloadController.getInstance(currentAccount).wifiPreset;
        } else {
            return DownloadController.getInstance(currentAccount).roamingPreset;
        }
    }

    @Override
    public int getCurrentPresetNum(int currentType) {
        if (currentType == 0) {
            return DownloadController.getInstance(currentAccount).currentMobilePreset;
        } else if (currentType == 1) {
            return DownloadController.getInstance(currentAccount).currentWifiPreset;
        } else {
            return DownloadController.getInstance(currentAccount).currentRoamingPreset;
        }
    }

    @Override
    public DownloadController.Preset getCurrentPreset(int currentType) {
        if (currentType == 0) {
            return DownloadController.getInstance(currentAccount).getCurrentMobilePreset();
        } else if (currentType == 1) {
            return DownloadController.getInstance(currentAccount).getCurrentWiFiPreset();
        } else {
            return DownloadController.getInstance(currentAccount).getCurrentRoamingPreset();
        }
    }

    @Override
    public void savePreset(int currentType, DownloadController.Preset typePreset, int currentPresetNum) {
        String key;
        String key2;
        if (currentType == 0) {
            key = "mobilePreset";
            key2 = "currentMobilePreset";
            DownloadController.getInstance(currentAccount).currentMobilePreset = currentPresetNum;
        } else if (currentType == 1) {
            key = "wifiPreset";
            key2 = "currentWifiPreset";
            DownloadController.getInstance(currentAccount).currentWifiPreset = currentPresetNum;
        } else {
            key = "roamingPreset";
            key2 = "currentRoamingPreset";
            DownloadController.getInstance(currentAccount).currentRoamingPreset = currentPresetNum;
        }
        SharedPreferences.Editor editor = MessagesController.getMainSettings(currentAccount).edit();
        if (typePreset != null) {
            editor.putString(key, typePreset.toString());
        }
        editor.putInt(key2, currentPresetNum);
        editor.commit();
        DownloadController.getInstance(currentAccount).checkAutodownloadSettings();
    }

    @Override
    public void setCurrentPresetNum(int currentType, int currentPresetNum) {
        savePreset(currentType, null, currentPresetNum);
    }

    @Override
    public void checkAutodownloadSettings() {
        DownloadController.getInstance(currentAccount).checkAutodownloadSettings();
    }

    @Override
    public void savePresetToServer(int currentType) {
        DownloadController.getInstance(currentAccount).savePresetToServer(currentType);
    }

    @Override
    public void sortPresets(ArrayList<DownloadController.Preset> presets) {
        Collections.sort(presets, (o1, o2) -> {
            int index1 = DownloadController.typeToIndex(DownloadController.AUTODOWNLOAD_TYPE_VIDEO);
            int index2 = DownloadController.typeToIndex(DownloadController.AUTODOWNLOAD_TYPE_DOCUMENT);
            boolean video1 = false;
            boolean doc1 = false;
            for (int a = 0; a < o1.mask.length; a++) {
                if ((o1.mask[a] & DownloadController.AUTODOWNLOAD_TYPE_VIDEO) != 0) {
                    video1 = true;
                }
                if ((o1.mask[a] & DownloadController.AUTODOWNLOAD_TYPE_DOCUMENT) != 0) {
                    doc1 = true;
                }
                if (video1 && doc1) {
                    break;
                }
            }
            boolean video2 = false;
            boolean doc2 = false;
            for (int a = 0; a < o2.mask.length; a++) {
                if ((o2.mask[a] & DownloadController.AUTODOWNLOAD_TYPE_VIDEO) != 0) {
                    video2 = true;
                }
                if ((o2.mask[a] & DownloadController.AUTODOWNLOAD_TYPE_DOCUMENT) != 0) {
                    doc2 = true;
                }
                if (video2 && doc2) {
                    break;
                }
            }
            long size1 = (video1 ? o1.sizes[index1] : 0) + (doc1 ? o1.sizes[index2] : 0);
            long size2 = (video2 ? o2.sizes[index1] : 0) + (doc2 ? o2.sizes[index2] : 0);
            if (size1 > size2) {
                return 1;
            } else if (size1 < size2) {
                return -1;
            }
            return 0;
        });
    }
}
