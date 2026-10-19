package org.telegram.core.data.repository;

import org.telegram.tgnet.TLRPC;
import org.telegram.ui.NotificationsSettingsActivity;
import java.util.ArrayList;

public interface NotificationsRepository {

    interface ExceptionsCallback {
        void onLoaded(ArrayList<NotificationsSettingsActivity.NotificationException> users, ArrayList<NotificationsSettingsActivity.NotificationException> chats, ArrayList<NotificationsSettingsActivity.NotificationException> channels);
    }

    long getSoundDocumentId(long dialogId, int topicId, int currentType);

    String getSoundPath(long dialogId, int topicId, int currentType);

    void saveSound(long dialogId, int topicId, int currentType, boolean fromServer, TLRPC.Document document, String title, String uri, boolean isSystemDefault);

    void deleteServerTones(ArrayList<TLRPC.Document> documents);

    void uploadRingtone(String path);

    void loadSignUpNotificationsSettings();

    void resetNotificationsSettings();

    void resetNotificationsSettings(Runnable onComplete);

    void resetNotificationsSettings(int currentAccount, Runnable onComplete);

    void loadExceptions(ExceptionsCallback callback);

    void loadExceptions(int currentAccount, ExceptionsCallback callback);

    boolean isGlobalNotificationsEnabled(long dialogId);

    boolean isNotificationsEnabled(long dialogId, int topicId, boolean addingException);

    void saveDialogNotifications(long dialogId, int topicId, boolean enabled, boolean addingException, Runnable onComplete);

    void resetCustomNotifications(long dialogId, int topicId, Runnable onComplete);

    void saveRingtoneOrSound(long dialogId, int topicId, int requestCode, String name, String ringtonePath);

    void applyCustomKey(long dialogId, int topicId);

    void resetNotify2Key(long dialogId, int topicId);

    boolean isGlobalNotificationsEnabled(int currentType);

    void setGlobalNotificationsEnabled(int currentType, int value);

    void updateServerNotificationsSettings(int currentType);

    void updateServerNotificationsSettings(long dialogId, int topicId, boolean resetSound);

    void muteDialog(long did, int topicId, boolean mute);

    void muteUntil(long did, int topicId, int timeInSeconds);

    void deleteAllExceptions(int currentType, ArrayList<NotificationsSettingsActivity.NotificationException> exceptions, int topicId);
}
