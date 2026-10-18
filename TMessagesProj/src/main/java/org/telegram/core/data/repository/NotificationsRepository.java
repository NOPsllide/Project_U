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

    void loadExceptions(ExceptionsCallback callback);
}
