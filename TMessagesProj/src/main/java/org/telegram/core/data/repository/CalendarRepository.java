package org.telegram.core.data.repository;

import org.telegram.tgnet.TLRPC;

public interface CalendarRepository {

    interface CalendarCallback {
        void onSuccess(TLRPC.TL_messages_searchResultsCalendar result);
        void onError(TLRPC.TL_error error);
    }

    void loadSearchResultsCalendar(long dialogId, int offsetId, int filterType, CalendarCallback callback);
}
