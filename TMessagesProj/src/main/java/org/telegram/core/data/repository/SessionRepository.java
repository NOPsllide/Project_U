package org.telegram.core.data.repository;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLRPC;

public interface SessionRepository {

    void loadAuthorizations(int classGuid, AuthorizationsCallback callback);

    void loadWebAuthorizations(int classGuid, WebAuthorizationsCallback callback);

    void terminateSession(long hash, Runnable onComplete);

    void terminateWebSession(long hash, Runnable onComplete);

    void terminateAllOtherSessions(RequestDelegate callback);

    void terminateAllWebSessions(RequestDelegate callback);

    void setAuthorizationTTL(int days);

    void acceptLoginToken(byte[] token, RequestDelegate callback);

    interface AuthorizationsCallback {
        void onSuccess(TLRPC.TL_account_authorizations authorizations);
        void onError(TLRPC.TL_error error);
    }

    interface WebAuthorizationsCallback {
        void onSuccess(TLRPC.TL_account_webAuthorizations authorizations);
        void onError(TLRPC.TL_error error);
    }
}
