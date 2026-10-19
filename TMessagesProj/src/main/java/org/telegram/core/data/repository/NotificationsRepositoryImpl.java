package org.telegram.core.data.repository;

import android.content.SharedPreferences;
import android.text.TextUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.SendMessagesHelper;
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

    @Override
    public void resetNotificationsSettings(Runnable onComplete) {
        resetNotificationsSettings(currentAccount, onComplete);
    }

    @Override
    public void resetNotificationsSettings(int currentAccount, Runnable onComplete) {
        TLRPC.TL_account_resetNotifySettings req = new TLRPC.TL_account_resetNotifySettings();
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            MessagesController.getInstance(currentAccount).enableJoined = true;
            SharedPreferences preferences = MessagesController.getNotificationsSettings(currentAccount);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.commit();
            MessagesStorage.getInstance(currentAccount).updateMutedDialogsFiltersCounters();
            if (onComplete != null) {
                onComplete.run();
            }
        }));
    }

    @Override
    public void loadExceptions(int account, ExceptionsCallback callback) {
        loadExceptions(callback);
    }

    @Override
    public boolean isGlobalNotificationsEnabled(long dialogId) {
        return NotificationsController.getInstance(currentAccount).isGlobalNotificationsEnabled(dialogId);
    }

    @Override
    public boolean isNotificationsEnabled(long dialogId, int topicId, boolean addingException) {
        boolean defaultEnabled = NotificationsController.getInstance(currentAccount).isGlobalNotificationsEnabled(dialogId);
        if (addingException) {
            return !defaultEnabled;
        }
        SharedPreferences preferences = MessagesController.getNotificationsSettings(currentAccount);
        String key = NotificationsController.getSharedPrefKey(dialogId, topicId);
        boolean hasOverride = preferences.contains("notify2_" + key);
        int value = preferences.getInt("notify2_" + key, 0);
        if (value == 0) {
            if (hasOverride) {
                return true;
            }
            return NotificationsController.getInstance(currentAccount).isGlobalNotificationsEnabled(dialogId);
        } else if (value == 1) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void saveDialogNotifications(long dialogId, int topicId, boolean enabled, boolean addingException, Runnable onComplete) {
        String key = NotificationsController.getSharedPrefKey(dialogId, topicId);
        SharedPreferences preferences = MessagesController.getNotificationsSettings(currentAccount);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("custom_" + key, true);
        TLRPC.Dialog dialog = MessagesController.getInstance(currentAccount).dialogs_dict.get(dialogId);
        if (enabled) {
            editor.putInt("notify2_" + key, 0);
            if (topicId == 0) {
                MessagesStorage.getInstance(currentAccount).setDialogFlags(dialogId, 0);
                if (dialog != null) {
                    dialog.notify_settings = new TLRPC.TL_peerNotifySettings();
                }
            }
        } else {
            editor.putInt("notify2_" + key, 2);
            if (topicId == 0) {
                NotificationsController.getInstance(currentAccount).removeNotificationsForDialog(dialogId);
                MessagesStorage.getInstance(currentAccount).setDialogFlags(dialogId, 1);
                if (dialog != null) {
                    dialog.notify_settings = new TLRPC.TL_peerNotifySettings();
                    dialog.notify_settings.mute_until = Integer.MAX_VALUE;
                }
            }
        }
        editor.apply();
        NotificationsController.getInstance(currentAccount).updateServerNotificationsSettings(dialogId, topicId);
        if (onComplete != null) {
            onComplete.run();
        }
    }

    @Override
    public void resetCustomNotifications(long dialogId, int topicId, Runnable onComplete) {
        String key = NotificationsController.getSharedPrefKey(dialogId, topicId);
        MessagesController.getNotificationsSettings(currentAccount).edit().putBoolean("custom_" + key, false).remove("notify2_" + key).apply();
        if (onComplete != null) {
            onComplete.run();
        }
    }

    @Override
    public void saveRingtoneOrSound(long dialogId, int topicId, int requestCode, String name, String ringtonePath) {
        SharedPreferences preferences = MessagesController.getNotificationsSettings(currentAccount);
        SharedPreferences.Editor editor = preferences.edit();
        String key = NotificationsController.getSharedPrefKey(dialogId, topicId);
        if (requestCode == 12) {
            if (name != null) {
                editor.putString("sound_" + key, name);
                editor.putString("sound_path_" + key, ringtonePath);
            } else {
                editor.putString("sound_" + key, "NoSound");
                editor.putString("sound_path_" + key, "NoSound");
            }
            NotificationsController.getInstance(currentAccount).deleteNotificationChannel(dialogId, topicId);
        } else if (requestCode == 13) {
            if (name != null) {
                editor.putString("ringtone_" + key, name);
                editor.putString("ringtone_path_" + key, ringtonePath);
            } else {
                editor.putString("ringtone_" + key, "NoSound");
                editor.putString("ringtone_path_" + key, "NoSound");
            }
        }
        editor.apply();
    }

    @Override
    public void applyCustomKey(long dialogId, int topicId) {
        String key = NotificationsController.getSharedPrefKey(dialogId, topicId);
        MessagesController.getNotificationsSettings(currentAccount).edit().putBoolean("custom_" + key, true).apply();
    }

    @Override
    public void resetNotify2Key(long dialogId, int topicId) {
        String key = NotificationsController.getSharedPrefKey(dialogId, topicId);
        MessagesController.getNotificationsSettings(currentAccount).edit().putInt("notify2_" + key, 0).apply();
    }

    @Override
    public boolean isGlobalNotificationsEnabled(int currentType) {
        return NotificationsController.getInstance(currentAccount).isGlobalNotificationsEnabled(currentType);
    }

    @Override
    public void setGlobalNotificationsEnabled(int currentType, int value) {
        NotificationsController.getInstance(currentAccount).setGlobalNotificationsEnabled(currentType, value);
    }

    @Override
    public void updateServerNotificationsSettings(int currentType) {
        NotificationsController.getInstance(currentAccount).updateServerNotificationsSettings(currentType);
    }

    @Override
    public void updateServerNotificationsSettings(long dialogId, int topicId, boolean resetSound) {
        NotificationsController.getInstance(currentAccount).updateServerNotificationsSettings(dialogId, topicId, resetSound);
    }

    @Override
    public void muteDialog(long did, int topicId, boolean mute) {
        NotificationsController.getInstance(currentAccount).muteDialog(did, topicId, mute);
    }

    @Override
    public void muteUntil(long did, int topicId, int timeInSeconds) {
        NotificationsController.getInstance(currentAccount).muteUntil(did, topicId, timeInSeconds);
    }

    @Override
    public void deleteAllExceptions(int currentType, ArrayList<NotificationsSettingsActivity.NotificationException> exceptions, int topicId) {
        if (exceptions == null || exceptions.isEmpty()) {
            return;
        }
        SharedPreferences preferences = MessagesController.getNotificationsSettings(currentAccount);
        SharedPreferences.Editor editor = preferences.edit();
        for (int a = 0, N = exceptions.size(); a < N; a++) {
            NotificationsSettingsActivity.NotificationException exception = exceptions.get(a);
            editor.remove("notify2_" + exception.did).remove("custom_" + exception.did);
            MessagesStorage.getInstance(currentAccount).setDialogFlags(exception.did, 0);
            TLRPC.Dialog dialog = MessagesController.getInstance(currentAccount).dialogs_dict.get(exception.did);
            if (dialog != null) {
                dialog.notify_settings = new TLRPC.TL_peerNotifySettings();
            }
        }
        editor.commit();
        for (int a = 0, N = exceptions.size(); a < N; a++) {
            NotificationsSettingsActivity.NotificationException exception = exceptions.get(a);
            NotificationsController.getInstance(currentAccount).updateServerNotificationsSettings(exception.did, topicId, false);
        }
    }

    @Override
    public ArrayList<MessageObject> getPopupMessages(boolean isReply, int forceAccount) {
        ArrayList<MessageObject> result = new ArrayList<>();
        if (isReply) {
            int account = forceAccount;
            if (!UserConfig.isValidAccount(account)) {
                return result;
            }
            result.addAll(NotificationsController.getInstance(account).popupReplyMessages);
        } else {
            for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
                if (UserConfig.getInstance(a).isClientActivated()) {
                    result.addAll(NotificationsController.getInstance(a).popupMessages);
                }
            }
        }
        return result;
    }

    @Override
    public void markDialogAsRead(int account, long dialogId, int id, int date) {
        MessagesController.getInstance(account).markDialogAsRead(dialogId, id, Math.max(0, id), date, true, 0, 0, true, 0);
    }

    @Override
    public void sendTyping(int account, long dialogId, int classGuid) {
        MessagesController.getInstance(account).sendTyping(dialogId, 0, 0, classGuid);
    }

    @Override
    public void sendNotificationCallback(int account, long dialogId, int messageId, byte[] data) {
        SendMessagesHelper.getInstance(account).sendNotificationCallback(dialogId, messageId, data);
    }

    @Override
    public TLRPC.User getUser(int account, long userId) {
        return MessagesController.getInstance(account).getUser(userId);
    }

    @Override
    public TLRPC.Chat getChat(int account, long chatId) {
        return MessagesController.getInstance(account).getChat(chatId);
    }

    @Override
    public TLRPC.EncryptedChat getEncryptedChat(int account, int chatId) {
        return MessagesController.getInstance(account).getEncryptedChat(chatId);
    }

    @Override
    public CharSequence getPrintingString(int account, long dialogId) {
        return MessagesController.getInstance(account).getPrintingString(dialogId, 0, false);
    }

    @Override
    public Integer getPrintingStringType(int account, long dialogId) {
        return MessagesController.getInstance(account).getPrintingStringType(dialogId, 0);
    }

    @Override
    public void setAppPaused(int account, boolean paused) {
        ConnectionsManager.getInstance(account).setAppPaused(paused, false);
    }
}
