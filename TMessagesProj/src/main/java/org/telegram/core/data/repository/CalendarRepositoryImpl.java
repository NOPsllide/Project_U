package org.telegram.core.data.repository;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.SharedMediaLayout;

public class CalendarRepositoryImpl implements CalendarRepository {

    private final int currentAccount;

    public CalendarRepositoryImpl(int currentAccount) {
        this.currentAccount = currentAccount;
    }

    @Override
    public void loadSearchResultsCalendar(long dialogId, int offsetId, int filterType, CalendarCallback callback) {
        TLRPC.TL_messages_getSearchResultsCalendar req = new TLRPC.TL_messages_getSearchResultsCalendar();
        if (filterType == SharedMediaLayout.FILTER_PHOTOS_ONLY) {
            req.filter = new TLRPC.TL_inputMessagesFilterPhotos();
        } else if (filterType == SharedMediaLayout.FILTER_VIDEOS_ONLY) {
            req.filter = new TLRPC.TL_inputMessagesFilterVideo();
        } else {
            req.filter = new TLRPC.TL_inputMessagesFilterPhotoVideo();
        }
        req.peer = MessagesController.getInstance(currentAccount).getInputPeer(dialogId);
        req.offset_id = offsetId;

        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (callback != null) {
                if (error == null && response instanceof TLRPC.TL_messages_searchResultsCalendar) {
                    callback.onSuccess((TLRPC.TL_messages_searchResultsCalendar) response);
                } else {
                    callback.onError(error);
                }
            }
        }));
    }
}
