package org.telegram.core.data.repository;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

public interface DialogsRepository {
    int sendRequest(TLObject request, RequestDelegate delegate);
    int sendRequest(TLObject request, RequestDelegate delegate, int flags);
    void cancelRequest(int requestId, boolean notify);
    void loadDialogs(int folderId, int offsetId, int count, boolean fromCache);
    void deleteDialog(long did, int keepHistory, boolean revoke);
    boolean pinDialog(long dialogId, boolean pin, TLRPC.InputPeer peer, int folderId);
}
