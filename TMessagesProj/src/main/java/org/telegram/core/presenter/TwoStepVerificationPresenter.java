package org.telegram.core.presenter;

import org.telegram.core.data.repository.TwoStepVerificationRepository;
import org.telegram.core.data.repository.TwoStepVerificationRepositoryImpl;
import org.telegram.messenger.NotificationCenter;
import org.telegram.tgnet.TLRPC;

public class TwoStepVerificationPresenter {

    public interface TwoStepVerificationView {
        void onPasswordLoaded(TLRPC.account_Password password);
        void onPasswordLoadError(TLRPC.TL_error error);
        void onPasswordResetResult(TLRPC.account_ResetPasswordResult result);
        void onPasswordResetError(TLRPC.TL_error error);
        void onPasswordResetDeclined(TLRPC.account_ResetPasswordResult result);
        void onPasswordResetDeclineError(TLRPC.TL_error error);
        void onTwoStepPasswordChanged();
    }

    private TwoStepVerificationView view;
    private final TwoStepVerificationRepository repository;

    public TwoStepVerificationPresenter(int currentAccount) {
        this(new TwoStepVerificationRepositoryImpl(currentAccount));
    }

    public TwoStepVerificationPresenter(TwoStepVerificationRepository repository) {
        this.repository = repository;
    }

    public void attachView(TwoStepVerificationView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void loadPassword() {
        repository.loadPassword(new TwoStepVerificationRepository.Callback<TLRPC.account_Password>() {
            @Override
            public void onSuccess(TLRPC.account_Password result) {
                if (view != null) {
                    view.onPasswordLoaded(result);
                }
            }

            @Override
            public void onError(TLRPC.TL_error error) {
                if (view != null) {
                    view.onPasswordLoadError(error);
                }
            }
        });
    }

    public void resetPassword() {
        repository.resetPassword(new TwoStepVerificationRepository.Callback<TLRPC.account_ResetPasswordResult>() {
            @Override
            public void onSuccess(TLRPC.account_ResetPasswordResult result) {
                if (view != null) {
                    view.onPasswordResetResult(result);
                }
            }

            @Override
            public void onError(TLRPC.TL_error error) {
                if (view != null) {
                    view.onPasswordResetError(error);
                }
            }
        });
    }

    public void cancelReset() {
        repository.cancelReset(new TwoStepVerificationRepository.Callback<TLRPC.account_ResetPasswordResult>() {
            @Override
            public void onSuccess(TLRPC.account_ResetPasswordResult result) {
                if (view != null) {
                    view.onPasswordResetDeclined(result);
                }
            }

            @Override
            public void onError(TLRPC.TL_error error) {
                if (view != null) {
                    view.onPasswordResetDeclineError(error);
                }
            }
        });
    }

    public int getCurrentTime() {
        return repository.getCurrentTime();
    }

    public void resetSavedPassword() {
        repository.resetSavedPassword();
    }

    public void postPasswordNotification(TLRPC.account_Password password) {
        repository.postPasswordNotification(password);
    }

    public void handleNotification(int id, Object... args) {
        if (view == null) {
            return;
        }
        if (id == NotificationCenter.twoStepPasswordChanged) {
            view.onTwoStepPasswordChanged();
        }
    }
}
