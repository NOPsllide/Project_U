package org.telegram.core.data.repository;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.FilterCreateActivity;

import java.util.ArrayList;

public class FilterRepositoryImpl implements FilterRepository {

    private final int currentAccount;

    public FilterRepositoryImpl(int currentAccount) {
        this.currentAccount = currentAccount;
    }

    @Override
    public void loadFilters(boolean force) {
        MessagesController.getInstance(currentAccount).loadRemoteFilters(force);
        if (MessagesController.getInstance(currentAccount).suggestedFilters.isEmpty()) {
            MessagesController.getInstance(currentAccount).loadSuggestedFilters();
        }
    }

    @Override
    public void saveDialogFiltersOrder() {
        MessagesStorage.getInstance(currentAccount).saveDialogFiltersOrder();
        TLRPC.TL_messages_updateDialogFiltersOrder req = new TLRPC.TL_messages_updateDialogFiltersOrder();
        ArrayList<MessagesController.DialogFilter> filters = MessagesController.getInstance(currentAccount).dialogFilters;
        for (int a = 0, N = filters.size(); a < N; a++) {
            MessagesController.DialogFilter filter = filters.get(a);
            req.order.add(filter.id);
        }
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> {});
    }

    @Override
    public void deleteDialogFilter(MessagesController.DialogFilter filter, Runnable onComplete) {
        TLRPC.TL_messages_updateDialogFilter req = new TLRPC.TL_messages_updateDialogFilter();
        req.id = filter.id;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            MessagesController.getInstance(currentAccount).removeFilter(filter);
            MessagesStorage.getInstance(currentAccount).deleteDialogFilter(filter);
            if (onComplete != null) {
                onComplete.run();
            }
        }));
    }

    @Override
    public void addSuggestedFilter(TLRPC.TL_dialogFilterSuggested suggested, BaseFragment fragment, Runnable onComplete) {
        MessagesController.DialogFilter filter = new MessagesController.DialogFilter();
        filter.name = suggested.filter.title;
        filter.id = 2;
        while (MessagesController.getInstance(currentAccount).dialogFiltersById.get(filter.id) != null) {
            filter.id++;
        }
        filter.pendingUnreadCount = filter.unreadCount = -1;
        for (int b = 0; b < 2; b++) {
            ArrayList<TLRPC.InputPeer> fromArray = b == 0 ? suggested.filter.include_peers : suggested.filter.exclude_peers;
            ArrayList<Long> toArray = b == 0 ? filter.alwaysShow : filter.neverShow;
            for (int a = 0, N = fromArray.size(); a < N; a++) {
                TLRPC.InputPeer peer = fromArray.get(a);
                long lowerId;
                if (peer.user_id != 0) {
                    lowerId = peer.user_id;
                } else if (peer.chat_id != 0) {
                    lowerId = -peer.chat_id;
                } else {
                    lowerId = -peer.channel_id;
                }
                toArray.add(lowerId);
            }
        }
        if (suggested.filter.groups) {
            filter.flags |= MessagesController.DIALOG_FILTER_FLAG_GROUPS;
        }
        if (suggested.filter.bots) {
            filter.flags |= MessagesController.DIALOG_FILTER_FLAG_BOTS;
        }
        if (suggested.filter.contacts) {
            filter.flags |= MessagesController.DIALOG_FILTER_FLAG_CONTACTS;
        }
        if (suggested.filter.non_contacts) {
            filter.flags |= MessagesController.DIALOG_FILTER_FLAG_NON_CONTACTS;
        }
        if (suggested.filter.broadcasts) {
            filter.flags |= MessagesController.DIALOG_FILTER_FLAG_CHANNELS;
        }
        if (suggested.filter.exclude_archived) {
            filter.flags |= MessagesController.DIALOG_FILTER_FLAG_EXCLUDE_ARCHIVED;
        }
        if (suggested.filter.exclude_read) {
            filter.flags |= MessagesController.DIALOG_FILTER_FLAG_EXCLUDE_READ;
        }
        if (suggested.filter.exclude_muted) {
            filter.flags |= MessagesController.DIALOG_FILTER_FLAG_EXCLUDE_MUTED;
        }
        FilterCreateActivity.saveFilterToServer(filter, filter.flags, filter.name, filter.alwaysShow, filter.neverShow, filter.pinnedDialogs, true, true, true, true, false, fragment, onComplete);
    }

    @Override
    public ArrayList<MessagesController.DialogFilter> getDialogFilters() {
        return MessagesController.getInstance(currentAccount).dialogFilters;
    }

    @Override
    public ArrayList<TLRPC.TL_dialogFilterSuggested> getSuggestedFilters() {
        return MessagesController.getInstance(currentAccount).suggestedFilters;
    }
}
