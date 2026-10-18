package org.telegram.core.data.repository;

import org.telegram.tgnet.TLRPC;
import java.util.ArrayList;

public interface NotificationsRepository {

    long getSoundDocumentId(long dialogId, int topicId, int currentType);

    String getSoundPath(long dialogId, int topicId, int currentType);

    void saveSound(long dialogId, int topicId, int currentType, boolean fromServer, TLRPC.Document document, String title, String uri, boolean isSystemDefault);

    void deleteServerTones(ArrayList<TLRPC.Document> documents);

    void uploadRingtone(String path);
}
