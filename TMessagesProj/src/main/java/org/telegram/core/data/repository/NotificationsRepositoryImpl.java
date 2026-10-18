package org.telegram.core.data.repository;

import android.content.SharedPreferences;

import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationsController;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

public class NotificationsRepositoryImpl implements NotificationsRepository {

    private final int currentAccount;

    public NotificationsRepositoryImpl(int currentAccount) {
        this.currentAccount = currentAccount;
    }

    private String getSharedPrefKey(long dialogId, int topicId) {
        return NotificationsController.getSharedPrefKey(dialogId, topicId);
    }

    private SharedPreferences getPreferences() {
        return MessagesController.getNotificationsSettings(currentAccount);
    }

    @Override
    public long getSoundDocumentId(long dialogId, int topicId, int currentType) {
        String prefDocId;
        if (dialogId != 0) {
            prefDocId = "sound_document_id_" + getSharedPrefKey(dialogId, topicId);
        } else {
            if (currentType == NotificationsController.TYPE_PRIVATE) {
                prefDocId = "GlobalSoundDocId";
            } else if (currentType == NotificationsController.TYPE_GROUP) {
                prefDocId = "GroupSoundDocId";
            } else if (currentType == NotificationsController.TYPE_CHANNEL) {
                prefDocId = "ChannelSoundDocId";
            } else {
                return 0;
            }
        }
        return getPreferences().getLong(prefDocId, 0);
    }

    @Override
    public String getSoundPath(long dialogId, int topicId, int currentType) {
        String prefPath;
        if (dialogId != 0) {
            prefPath = "sound_path_" + getSharedPrefKey(dialogId, topicId);
        } else {
            if (currentType == NotificationsController.TYPE_PRIVATE) {
                prefPath = "GlobalSoundPath";
            } else if (currentType == NotificationsController.TYPE_GROUP) {
                prefPath = "GroupSoundPath";
            } else if (currentType == NotificationsController.TYPE_CHANNEL) {
                prefPath = "ChannelSoundPath";
            } else {
                return "NoSound";
            }
        }
        return getPreferences().getString(prefPath, "NoSound");
    }

    @Override
    public void saveSound(long dialogId, int topicId, int currentType, boolean fromServer, TLRPC.Document document, String title, String uri, boolean isSystemDefault) {
        SharedPreferences preferences = getPreferences();
        SharedPreferences.Editor editor = preferences.edit();

        String prefName;
        String prefPath;
        String prefDocId;

        if (dialogId != 0) {
            String key = getSharedPrefKey(dialogId, topicId);
            prefName = "sound_" + key;
            prefPath = "sound_path_" + key;
            prefDocId = "sound_document_id_" + key;
            editor.putBoolean("sound_enabled_" + key, true);
        } else {
            if (currentType == NotificationsController.TYPE_PRIVATE) {
                prefName = "GlobalSound";
                prefPath = "GlobalSoundPath";
                prefDocId = "GlobalSoundDocId";
            } else if (currentType == NotificationsController.TYPE_GROUP) {
                prefName = "GroupSound";
                prefPath = "GroupSoundPath";
                prefDocId = "GroupSoundDocId";
            } else if (currentType == NotificationsController.TYPE_CHANNEL) {
                prefName = "ChannelSound";
                prefPath = "ChannelSoundPath";
                prefDocId = "ChannelSoundDocId";
            } else {
                return;
            }
        }

        if (fromServer && document != null) {
            editor.putLong(prefDocId, document.id);
            editor.putString(prefName, title);
            editor.putString(prefPath, "NoSound");
        } else if (uri != null) {
            editor.putString(prefName, title);
            editor.putString(prefPath, uri);
            editor.remove(prefDocId);
        } else if (isSystemDefault) {
            editor.putString(prefName, "Default");
            editor.putString(prefPath, "Default");
            editor.remove(prefDocId);
        } else {
            editor.putString(prefName, "NoSound");
            editor.putString(prefPath, "NoSound");
            editor.remove(prefDocId);
        }

        editor.apply();

        NotificationsController notificationsController = NotificationsController.getInstance(currentAccount);
        if (dialogId != 0) {
            notificationsController.updateServerNotificationsSettings(dialogId, topicId);
        } else {
            notificationsController.updateServerNotificationsSettings(currentType);
            NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.notificationsSettingsUpdated);
        }
    }

    @Override
    public void deleteServerTones(ArrayList<TLRPC.Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }
        for (int i = 0; i < documents.size(); i++) {
            TLRPC.Document document = documents.get(i);
            TLRPC.TL_account_saveRingtone req = new TLRPC.TL_account_saveRingtone();
            req.id = new TLRPC.TL_inputDocument();
            req.id.id = document.id;
            req.id.access_hash = document.access_hash;
            req.id.file_reference = document.file_reference;
            if (req.id.file_reference == null) {
                req.id.file_reference = new byte[0];
            }
            req.unsave = true;
            ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> {});
        }
    }

    @Override
    public void uploadRingtone(String path) {
        MediaDataController.getInstance(currentAccount).uploadRingtone(path);
    }
}
