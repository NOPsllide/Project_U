package org.telegram.core.data.repository;

import android.content.SharedPreferences;
import android.text.TextUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.NotificationsSettingsActivity;

import java.util.ArrayList;
import java.util.Map;

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

    @Override
    public void loadSignUpNotificationsSettings() {
        MessagesController.getInstance(currentAccount).loadSignUpNotificationsSettings();
    }

    @Override
    public void resetNotificationsSettings() {
        TLRPC.TL_account_resetNotifySettings req = new TLRPC.TL_account_resetNotifySettings();
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            MessagesController.getInstance(currentAccount).enableJoined = true;
            SharedPreferences preferences = MessagesController.getNotificationsSettings(currentAccount);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.commit();
            MessagesStorage.getInstance(currentAccount).updateMutedDialogsFiltersCounters();
        }));
    }

    @Override
    public void loadExceptions(ExceptionsCallback callback) {
        MessagesStorage.getInstance(currentAccount).getStorageQueue().postRunnable(() -> {
            ArrayList<NotificationsSettingsActivity.NotificationException> usersResult = new ArrayList<>();
            ArrayList<NotificationsSettingsActivity.NotificationException> chatsResult = new ArrayList<>();
            ArrayList<NotificationsSettingsActivity.NotificationException> channelsResult = new ArrayList<>();
            android.util.LongSparseArray<NotificationsSettingsActivity.NotificationException> waitingForLoadExceptions = new android.util.LongSparseArray<>();

            ArrayList<Long> usersToLoad = new ArrayList<>();
            ArrayList<Long> chatsToLoad = new ArrayList<>();
            ArrayList<Integer> encryptedChatsToLoad = new ArrayList<>();

            ArrayList<TLRPC.User> users = new ArrayList<>();
            ArrayList<TLRPC.Chat> chats = new ArrayList<>();
            ArrayList<TLRPC.EncryptedChat> encryptedChats = new ArrayList<>();
            long selfId = UserConfig.getInstance(currentAccount).clientUserId;

            SharedPreferences preferences = MessagesController.getNotificationsSettings(currentAccount);
            Map<String, ?> values = preferences.getAll();
            for (Map.Entry<String, ?> entry : values.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("notify2_")) {
                    key = key.replace("notify2_", "");
                    if (key.contains("_")) {
                        continue;
                    }

                    long did = Utilities.parseLong(key);
                    if (did != 0 && did != selfId) {
                        NotificationsSettingsActivity.NotificationException exception = new NotificationsSettingsActivity.NotificationException();
                        exception.did = did;
                        exception.hasCustom = preferences.getBoolean("custom_" + did, false);
                        exception.notify = (Integer) entry.getValue();
                        if (exception.notify != 0) {
                            Integer time = (Integer) values.get("notifyuntil_" + key);
                            if (time != null) {
                                exception.muteUntil = time;
                            }
                        }

                        if (DialogObject.isEncryptedDialog(did)) {
                            int encryptedChatId = DialogObject.getEncryptedChatId(did);
                            TLRPC.EncryptedChat encryptedChat = MessagesController.getInstance(currentAccount).getEncryptedChat(encryptedChatId);
                            if (encryptedChat == null) {
                                encryptedChatsToLoad.add(encryptedChatId);
                                waitingForLoadExceptions.put(did, exception);
                            } else {
                                TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(encryptedChat.user_id);
                                if (user == null) {
                                    usersToLoad.add(encryptedChat.user_id);
                                    waitingForLoadExceptions.put(encryptedChat.user_id, exception);
                                } else if (user.deleted) {
                                    continue;
                                }
                            }
                            usersResult.add(exception);
                        } else if (DialogObject.isUserDialog(did)) {
                            TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(did);
                            if (user == null) {
                                usersToLoad.add(did);
                                waitingForLoadExceptions.put(did, exception);
                            } else if (user.deleted) {
                                continue;
                            }
                            usersResult.add(exception);
                        } else {
                            TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-did);
                            if (chat == null) {
                                chatsToLoad.add(-did);
                                waitingForLoadExceptions.put(did, exception);
                                continue;
                            } else if (chat.left || chat.kicked || chat.migrated_to != null) {
                                continue;
                            }
                            if (ChatObject.isChannel(chat) && !chat.megagroup) {
                                channelsResult.add(exception);
                            } else {
                                chatsResult.add(exception);
                            }
                        }
                    }
                }
            }
            if (waitingForLoadExceptions.size() != 0) {
                try {
                    if (!encryptedChatsToLoad.isEmpty()) {
                        MessagesStorage.getInstance(currentAccount).getEncryptedChatsInternal(TextUtils.join(",", encryptedChatsToLoad), encryptedChats, usersToLoad);
                    }
                    if (!usersToLoad.isEmpty()) {
                        MessagesStorage.getInstance(currentAccount).getUsersInternal(TextUtils.join(",", usersToLoad), users);
                    }
                    if (!chatsToLoad.isEmpty()) {
                        MessagesStorage.getInstance(currentAccount).getChatsInternal(TextUtils.join(",", chatsToLoad), chats);
                    }
                } catch (Exception e) {
                    FileLog.e(e);
                }
                for (int a = 0, size = chats.size(); a < size; a++) {
                    TLRPC.Chat chat = chats.get(a);
                    if (chat.left || chat.kicked || chat.migrated_to != null) {
                        continue;
                    }
                    NotificationsSettingsActivity.NotificationException exception = waitingForLoadExceptions.get(-chat.id);
                    waitingForLoadExceptions.remove(-chat.id);

                    if (exception != null) {
                        if (ChatObject.isChannel(chat) && !chat.megagroup) {
                            channelsResult.add(exception);
                        } else {
                            chatsResult.add(exception);
                        }
                    }
                }
                for (int a = 0, size = users.size(); a < size; a++) {
                    TLRPC.User user = users.get(a);
                    if (user.deleted) {
                        continue;
                    }
                    waitingForLoadExceptions.remove(user.id);
                }
                for (int a = 0, size = encryptedChats.size(); a < size; a++) {
                    TLRPC.EncryptedChat encryptedChat = encryptedChats.get(a);
                    waitingForLoadExceptions.remove(DialogObject.makeEncryptedDialogId(encryptedChat.id));
                }
                for (int a = 0, size = waitingForLoadExceptions.size(); a < size; a++) {
                    long did = waitingForLoadExceptions.keyAt(a);
                    if (DialogObject.isChatDialog(did)) {
                        chatsResult.remove(waitingForLoadExceptions.valueAt(a));
                        channelsResult.remove(waitingForLoadExceptions.valueAt(a));
                    } else {
                        usersResult.remove(waitingForLoadExceptions.valueAt(a));
                    }
                }
            }
            AndroidUtilities.runOnUIThread(() -> {
                MessagesController.getInstance(currentAccount).putUsers(users, true);
                MessagesController.getInstance(currentAccount).putChats(chats, true);
                MessagesController.getInstance(currentAccount).putEncryptedChats(encryptedChats, true);
                if (callback != null) {
                    callback.onLoaded(usersResult, chatsResult, channelsResult);
                }
            });
        });
    }
}
