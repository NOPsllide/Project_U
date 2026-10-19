package org.telegram.core.data.repository;

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
}
