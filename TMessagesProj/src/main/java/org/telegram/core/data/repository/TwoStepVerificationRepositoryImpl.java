package org.telegram.core.data.repository;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;

public class TwoStepVerificationRepositoryImpl implements TwoStepVerificationRepository {

    private final int accountNum;

    public TwoStepVerificationRepositoryImpl(int accountNum) {
        this.accountNum = accountNum;
    }

    @Override
    public void loadPassword(Callback<TLRPC.account_Password> callback) {
        TLRPC.TL_account_getPassword req = new TLRPC.TL_account_getPassword();
        ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (callback != null) {
                if (error == null && response instanceof TLRPC.account_Password) {
                    callback.onSuccess((TLRPC.account_Password) response);
                } else {
                    callback.onError(error);
                }
            }
        }), ConnectionsManager.RequestFlagFailOnServerErrors | ConnectionsManager.RequestFlagWithoutLogin);
    }

    @Override
    public void resetPassword(Callback<TLRPC.account_ResetPasswordResult> callback) {
        TLRPC.TL_account_resetPassword req = new TLRPC.TL_account_resetPassword();
        ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (callback != null) {
                if (error == null && response instanceof TLRPC.account_ResetPasswordResult) {
                    callback.onSuccess((TLRPC.account_ResetPasswordResult) response);
                } else {
                    callback.onError(error);
                }
            }
        }), ConnectionsManager.RequestFlagFailOnServerErrors | ConnectionsManager.RequestFlagWithoutLogin);
    }

    @Override
    public void cancelReset(Callback<TLRPC.account_ResetPasswordResult> callback) {
        TLRPC.TL_account_declinePasswordReset req = new TLRPC.TL_account_declinePasswordReset();
        ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (callback != null) {
                if (error == null && response instanceof TLRPC.account_ResetPasswordResult) {
                    callback.onSuccess((TLRPC.account_ResetPasswordResult) response);
                } else {
                    callback.onError(error);
                }
            }
        }), ConnectionsManager.RequestFlagFailOnServerErrors | ConnectionsManager.RequestFlagWithoutLogin);
    }

    @Override
    public int getCurrentTime() {
        return ConnectionsManager.getInstance(accountNum).getCurrentTime();
    }

    @Override
    public void resetSavedPassword() {
        UserConfig.getInstance(accountNum).resetSavedPassword();
    }

    @Override
    public void postPasswordNotification(TLRPC.account_Password password) {
        NotificationCenter.getInstance(accountNum).postNotificationName(NotificationCenter.didSetOrRemoveTwoStepPassword, password);
    }
}
