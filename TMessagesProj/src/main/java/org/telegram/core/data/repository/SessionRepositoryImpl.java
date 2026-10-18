package org.telegram.core.data.repository;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLRPC;

public class SessionRepositoryImpl implements SessionRepository {

    private final int currentAccount;

    public SessionRepositoryImpl(int currentAccount) {
        this.currentAccount = currentAccount;
    }

    @Override
    public void loadAuthorizations(int classGuid, AuthorizationsCallback callback) {
        TLRPC.TL_account_getAuthorizations req = new TLRPC.TL_account_getAuthorizations();
        int reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (error == null && response instanceof TLRPC.TL_account_authorizations) {
                if (callback != null) {
                    callback.onSuccess((TLRPC.TL_account_authorizations) response);
                }
            } else {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        }));
        if (classGuid != 0) {
            ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(reqId, classGuid);
        }
    }

    @Override
    public void loadWebAuthorizations(int classGuid, WebAuthorizationsCallback callback) {
        TLRPC.TL_account_getWebAuthorizations req = new TLRPC.TL_account_getWebAuthorizations();
        int reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (error == null && response instanceof TLRPC.TL_account_webAuthorizations) {
                TLRPC.TL_account_webAuthorizations res = (TLRPC.TL_account_webAuthorizations) response;
                MessagesController.getInstance(currentAccount).putUsers(res.users, false);
                if (callback != null) {
                    callback.onSuccess(res);
                }
            } else {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        }));
        if (classGuid != 0) {
            ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(reqId, classGuid);
        }
    }

    @Override
    public void terminateSession(long hash, Runnable onComplete) {
        TLRPC.TL_account_resetAuthorization req = new TLRPC.TL_account_resetAuthorization();
        req.hash = hash;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (onComplete != null) {
                onComplete.run();
            }
        }));
    }

    @Override
    public void terminateWebSession(long hash, Runnable onComplete) {
        TLRPC.TL_account_resetWebAuthorization req = new TLRPC.TL_account_resetWebAuthorization();
        req.hash = hash;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (onComplete != null) {
                onComplete.run();
            }
        }));
    }

    @Override
    public void terminateAllOtherSessions(RequestDelegate callback) {
        TLRPC.TL_auth_resetAuthorizations req = new TLRPC.TL_auth_resetAuthorizations();
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> {
            AndroidUtilities.runOnUIThread(() -> {
                if (callback != null) {
                    callback.run(response, error);
                }
            });
            for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
                UserConfig userConfig = UserConfig.getInstance(a);
                if (!userConfig.isClientActivated()) {
                    continue;
                }
                userConfig.registeredForPush = false;
                userConfig.saveConfig(false);
                MessagesController.getInstance(a).registerForPush(SharedConfig.pushType, SharedConfig.pushString);
                ConnectionsManager.getInstance(a).setUserId(userConfig.getClientUserId());
            }
        });
    }

    @Override
    public void terminateAllWebSessions(RequestDelegate callback) {
        TLRPC.TL_account_resetWebAuthorizations req = new TLRPC.TL_account_resetWebAuthorizations();
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (callback != null) {
                callback.run(response, error);
            }
        }));
    }

    @Override
    public void setAuthorizationTTL(int days) {
        TLRPC.TL_account_setAuthorizationTTL req = new TLRPC.TL_account_setAuthorizationTTL();
        req.authorization_ttl_days = days;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> {});
    }

    @Override
    public void acceptLoginToken(byte[] token, RequestDelegate callback) {
        TLRPC.TL_auth_acceptLoginToken req = new TLRPC.TL_auth_acceptLoginToken();
        req.token = token;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (callback != null) {
                callback.run(response, error);
            }
        }));
    }
}
