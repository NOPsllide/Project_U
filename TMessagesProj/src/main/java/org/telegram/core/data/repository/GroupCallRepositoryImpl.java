package org.telegram.core.data.repository;

import org.telegram.messenger.AccountInstance;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;

public class GroupCallRepositoryImpl implements GroupCallRepository {

    private final AccountInstance accountInstance;

    public GroupCallRepositoryImpl(AccountInstance accountInstance) {
        this.accountInstance = accountInstance;
    }

    public GroupCallRepositoryImpl(int currentAccount) {
        this(AccountInstance.getInstance(currentAccount));
    }

    @Override
    public int sendRequest(TLObject request, RequestDelegate delegate) {
        return accountInstance.getConnectionsManager().sendRequest(request, delegate);
    }

    @Override
    public int sendRequest(TLObject request, RequestDelegate delegate, int flags) {
        return accountInstance.getConnectionsManager().sendRequest(request, delegate, flags);
    }

    @Override
    public void cancelRequest(int requestId, boolean notify) {
        accountInstance.getConnectionsManager().cancelRequest(requestId, notify);
    }
}
