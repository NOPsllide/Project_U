package org.telegram.core.data.repository;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;

public interface ProfileRepository {
    int sendRequest(TLObject request, RequestDelegate delegate);
    int sendRequest(TLObject request, RequestDelegate delegate, int flags);
    void cancelRequest(int requestId, boolean notify);
    void cancelRequestsForGuid(int guid);
}
