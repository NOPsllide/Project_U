package org.telegram.core.data.repository;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

public interface TwoStepVerificationRepository {

    interface Callback<T> {
        void onSuccess(T result);
        void onError(TLRPC.TL_error error);
    }

    void loadPassword(Callback<TLRPC.account_Password> callback);

    void resetPassword(Callback<TLRPC.account_ResetPasswordResult> callback);

    void cancelReset(Callback<TLRPC.account_ResetPasswordResult> callback);

    int getCurrentTime();

    void resetSavedPassword();

    void postPasswordNotification(TLRPC.account_Password password);

    void resendPasswordEmail(Callback<TLRPC.account_PasswordEmailStatus> callback);

    void cancelPasswordEmail(Callback<TLRPC.TL_boolTrue> callback);

    void confirmPasswordEmail(String code, Callback<TLRPC.TL_boolTrue> callback);

    void recoverPassword(String code, TLRPC.TL_account_passwordInputSettings newSettings, Callback<TLObject> callback);

    void getPasswordSettings(TLRPC.TL_account_getPasswordSettings req, Callback<TLRPC.account_PasswordSettings> callback);

    void sendRequest(TLObject req, RequestDelegate delegate, int flags);

    void postNotification(int id, Object... args);

    void removeSuggestion(String suggestion);
}
