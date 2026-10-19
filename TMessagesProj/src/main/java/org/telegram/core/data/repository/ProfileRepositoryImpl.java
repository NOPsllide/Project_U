package org.telegram.core.data.repository;

import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;

public class ProfileRepositoryImpl implements ProfileRepository {

    private final int currentAccount;

    public ProfileRepositoryImpl(int currentAccount) {
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
    public void cancelRequestsForGuid(int guid) {
        ConnectionsManager.getInstance(currentAccount).cancelRequestsForGuid(guid);
    }
}
