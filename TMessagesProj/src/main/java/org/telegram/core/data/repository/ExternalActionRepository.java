package org.telegram.core.data.repository;

import org.telegram.tgnet.TLRPC;

public interface ExternalActionRepository {

    interface PassportAuthCallback {
        void onSuccess(TLRPC.TL_account_authorizationForm form, TLRPC.account_Password password);
        void onError(String errorText);
    }

    int getActivatedAccountsCount();

    void switchToAccount(int account);

    int getAuthorizationForm(int account, long botId, String scope, String publicKey, PassportAuthCallback callback);

    void cancelRequest(int account, int requestId);
}
