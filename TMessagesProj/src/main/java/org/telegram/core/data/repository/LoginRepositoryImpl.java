package org.telegram.core.data.repository;

import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

public class LoginRepositoryImpl implements LoginRepository {

    private final int currentAccount;

    public LoginRepositoryImpl(int currentAccount) {
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
    public void cancelRequest(int reqId, boolean notifyServer) {
        ConnectionsManager.getInstance(currentAccount).cancelRequest(reqId, notifyServer);
    }

    @Override
    public boolean isTestBackend() {
        return ConnectionsManager.getInstance(currentAccount).isTestBackend();
    }

    @Override
    public void switchBackend() {
        ConnectionsManager.getInstance(currentAccount).switchBackend(false);
    }

    @Override
    public int getCurrentTime() {
        return ConnectionsManager.getInstance(currentAccount).getCurrentTime();
    }

    @Override
    public void updateDcSettings() {
        ConnectionsManager.getInstance(currentAccount).updateDcSettings();
    }

    @Override
    public void setUserId(long userId) {
        ConnectionsManager.getInstance(currentAccount).setUserId(userId);
    }

    @Override
    public void cleanup() {
        ConnectionsManager.getInstance(currentAccount).cleanup(false);
        MessagesController.getInstance(currentAccount).cleanup();
    }

    @Override
    public void putUser(TLRPC.User user, boolean fromCache) {
        MessagesController.getInstance(currentAccount).putUser(user, fromCache);
    }

    @Override
    public void removeSuggestion(long did, String suggestion) {
        MessagesController.getInstance(currentAccount).removeSuggestion(did, suggestion);
    }
}
