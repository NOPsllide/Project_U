package org.telegram.core.data.repository;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
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

    @Override
    public void resendPasswordEmail(Callback<TLRPC.Bool> callback) {
        TLRPC.TL_account_resendPasswordEmail req = new TLRPC.TL_account_resendPasswordEmail();
        ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (callback != null) {
                if (error == null && response instanceof TLRPC.Bool) {
                    callback.onSuccess((TLRPC.Bool) response);
                } else {
                    callback.onError(error);
                }
            }
        }));
    }

    @Override
    public void cancelPasswordEmail(Callback<TLRPC.TL_boolTrue> callback) {
        TLRPC.TL_account_cancelPasswordEmail req = new TLRPC.TL_account_cancelPasswordEmail();
        ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (callback != null) {
                if (error == null && response instanceof TLRPC.TL_boolTrue) {
                    callback.onSuccess((TLRPC.TL_boolTrue) response);
                } else {
                    callback.onError(error);
                }
            }
        }));
    }

    @Override
    public void confirmPasswordEmail(String code, Callback<TLRPC.TL_boolTrue> callback) {
        TLRPC.TL_account_confirmPasswordEmail req = new TLRPC.TL_account_confirmPasswordEmail();
        req.code = code;
        ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (callback != null) {
                if (error == null && response instanceof TLRPC.TL_boolTrue) {
                    callback.onSuccess((TLRPC.TL_boolTrue) response);
                } else {
                    callback.onError(error);
                }
            }
        }), ConnectionsManager.RequestFlagFailOnServerErrors | ConnectionsManager.RequestFlagWithoutLogin);
    }

    @Override
    public void recoverPassword(String code, TLRPC.TL_account_passwordInputSettings newSettings, Callback<TLObject> callback) {
        TLRPC.TL_auth_recoverPassword req = new TLRPC.TL_auth_recoverPassword();
        req.code = code;
        if (newSettings != null) {
            req.new_settings = newSettings;
            req.flags |= 1;
        }
        ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (callback != null) {
                if (error == null) {
                    callback.onSuccess(response);
                } else {
                    callback.onError(error);
                }
            }
        }));
    }

    @Override
    public void getPasswordSettings(TLRPC.TL_account_getPasswordSettings req, Callback<TLRPC.TL_account_passwordSettings> callback) {
        ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (callback != null) {
                if (error == null && response instanceof TLRPC.TL_account_passwordSettings) {
                    callback.onSuccess((TLRPC.TL_account_passwordSettings) response);
                } else {
                    callback.onError(error);
                }
            }
        }), ConnectionsManager.RequestFlagFailOnServerErrors | ConnectionsManager.RequestFlagWithoutLogin);
    }

    @Override
    public void sendRequest(TLObject req, RequestDelegate delegate, int flags) {
        ConnectionsManager.getInstance(accountNum).sendRequest(req, delegate, flags);
    }

    @Override
    public void postNotification(int id, Object... args) {
        NotificationCenter.getInstance(accountNum).postNotificationName(id, args);
    }

    @Override
    public void removeSuggestion(String suggestion) {
        MessagesController.getInstance(accountNum).removeSuggestion(0, suggestion);
    }
}
