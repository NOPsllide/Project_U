package org.telegram.core.data.repository;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;

public class ExternalActionRepositoryImpl implements ExternalActionRepository {

    @Override
    public int getActivatedAccountsCount() {
        return UserConfig.getActivatedAccountsCount();
    }

    @Override
    public void switchToAccount(int account) {
        if (account == UserConfig.selectedAccount) {
            return;
        }
        ConnectionsManager.getInstance(UserConfig.selectedAccount).setAppPaused(true, false);
        UserConfig.selectedAccount = account;
        UserConfig.getInstance(0).saveConfig(false);
        if (!ApplicationLoader.mainInterfacePaused) {
            ConnectionsManager.getInstance(UserConfig.selectedAccount).setAppPaused(false, false);
        }
    }

    @Override
    public int getAuthorizationForm(int account, long botId, String scope, String publicKey, PassportAuthCallback callback) {
        TLRPC.TL_account_getAuthorizationForm req = new TLRPC.TL_account_getAuthorizationForm();
        req.bot_id = botId;
        req.scope = scope;
        req.public_key = publicKey;

        final int[] requestId = {0};
        requestId[0] = ConnectionsManager.getInstance(account).sendRequest(req, (response, error) -> {
            if (response != null) {
                TLRPC.TL_account_authorizationForm authorizationForm = (TLRPC.TL_account_authorizationForm) response;
                TLRPC.TL_account_getPassword req2 = new TLRPC.TL_account_getPassword();
                requestId[0] = ConnectionsManager.getInstance(account).sendRequest(req2, (response1, error1) -> {
                    if (response1 != null) {
                        TLRPC.account_Password accountPassword = (TLRPC.account_Password) response1;
                        MessagesController.getInstance(account).putUsers(authorizationForm.users, false);
                        AndroidUtilities.runOnUIThread(() -> {
                            if (callback != null) {
                                callback.onSuccess(authorizationForm, accountPassword);
                            }
                        });
                    } else {
                        AndroidUtilities.runOnUIThread(() -> {
                            if (callback != null) {
                                callback.onError(error1 != null ? error1.text : "UNKNOWN_ERROR");
                            }
                        });
                    }
                });
            } else {
                AndroidUtilities.runOnUIThread(() -> {
                    if (callback != null) {
                        callback.onError(error != null ? error.text : "UNKNOWN_ERROR");
                    }
                });
            }
        }, ConnectionsManager.RequestFlagFailOnServerErrors | ConnectionsManager.RequestFlagWithoutLogin);
        return requestId[0];
    }

    @Override
    public void cancelRequest(int account, int requestId) {
        ConnectionsManager.getInstance(account).cancelRequest(requestId, true);
    }
}
