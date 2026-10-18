package org.telegram.core.data.repository;

import android.content.Context;
import android.content.Intent;
import org.telegram.messenger.MessagesStorage;
import java.util.ArrayList;

public interface WidgetRepository {

    int extractAppWidgetId(Intent intent);

    void saveFeedWidgetConfig(Context context, int account, int appWidgetId, ArrayList<MessagesStorage.TopicKey> dids);

    void saveWidgetDialogs(int account, int appWidgetId, ArrayList<MessagesStorage.TopicKey> dids);
}
