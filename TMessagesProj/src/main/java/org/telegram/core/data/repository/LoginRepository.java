package org.telegram.core.data.repository;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

public interface LoginRepository {

    int sendRequest(TLObject request, RequestDelegate delegate);

    int sendRequest(TLObject request, RequestDelegate delegate, int flags);

    void cancelRequest(int reqId, boolean notifyServer);

    boolean isTestBackend();

    void switchBackend();

    int getCurrentTime();

    void updateDcSettings();

    void setUserId(long userId);

    void cleanup();

    void putUser(TLRPC.User user, boolean fromCache);

    void removeSuggestion(long did, int type);
}
