package org.telegram.core.data.repository;

import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

public class DialogsRepositoryImpl implements DialogsRepository {

    private final int currentAccount;

    public DialogsRepositoryImpl(int currentAccount) {
        this.currentAccount = currentAccount;
    }

    @Override
    public int sendRequest(TLObject request, RequestDelegate delegate) {
        return ConnectionsManager.getInstance(currentAccount).sendRequest(request, delegate);
    }

    @Override
    public int sendRequest(TLObject request, RequestDelegate delegate, int flags) {
        return ConnectionsManager.getInstance(currentAccount).sendRequest(request, delegate, flags);
    }

    @Override
    public void cancelRequest(int requestId, boolean notify) {
        ConnectionsManager.getInstance(currentAccount).cancelRequest(requestId, notify);
    }

    @Override
    public void loadDialogs(int folderId, int offsetId, int count, boolean fromCache) {
        MessagesController.getInstance(currentAccount).loadDialogs(folderId, offsetId, count, fromCache);
    }

    @Override
    public void deleteDialog(long did, int keepHistory, boolean revoke) {
        MessagesController.getInstance(currentAccount).deleteDialog(did, keepHistory, revoke);
    }

    @Override
    public boolean pinDialog(long dialogId, boolean pin, TLRPC.InputPeer peer, int folderId) {
        return MessagesController.getInstance(currentAccount).pinDialog(dialogId, pin, peer, folderId);
    }
}
