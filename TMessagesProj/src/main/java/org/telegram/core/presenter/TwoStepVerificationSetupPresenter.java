package org.telegram.core.presenter;

import org.telegram.core.data.repository.TwoStepVerificationRepository;
import org.telegram.core.data.repository.TwoStepVerificationRepositoryImpl;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

public class TwoStepVerificationSetupPresenter {

    public interface TwoStepVerificationSetupView {
        void onPasswordLoaded(TLRPC.account_Password password);
        void onPasswordLoadError(TLRPC.TL_error error);
    }

    private TwoStepVerificationSetupView view;
    private final TwoStepVerificationRepository repository;
    private final int currentAccount;

    public TwoStepVerificationSetupPresenter(int currentAccount) {
        this(new TwoStepVerificationRepositoryImpl(currentAccount), currentAccount);
    }

    public TwoStepVerificationSetupPresenter(TwoStepVerificationRepository repository, int currentAccount) {
        this.repository = repository;
        this.currentAccount = currentAccount;
    }

    public void attachView(TwoStepVerificationSetupView view) {
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

    public void recoverPassword(String code, TLRPC.TL_account_passwordInputSettings newSettings, TwoStepVerificationRepository.Callback<TLObject> callback) {
        repository.recoverPassword(code, newSettings, callback);
    }

    public void resendPasswordEmail(TwoStepVerificationRepository.Callback<TLRPC.account_PasswordEmailStatus> callback) {
        repository.resendPasswordEmail(callback);
    }

    public void cancelPasswordEmail(TwoStepVerificationRepository.Callback<TLRPC.TL_boolTrue> callback) {
        repository.cancelPasswordEmail(callback);
    }

    public void confirmPasswordEmail(String code, TwoStepVerificationRepository.Callback<TLRPC.TL_boolTrue> callback) {
        repository.confirmPasswordEmail(code, callback);
    }

    public void getPasswordSettings(TLRPC.TL_account_getPasswordSettings req, TwoStepVerificationRepository.Callback<TLRPC.account_PasswordSettings> callback) {
        repository.getPasswordSettings(req, callback);
    }

    public void sendRequest(TLObject req, RequestDelegate delegate, int flags) {
        repository.sendRequest(req, delegate, flags);
    }

    public void resetSavedPassword() {
        repository.resetSavedPassword();
    }

    public void postNotification(int id, Object... args) {
        repository.postNotification(id, args);
    }

    public void removeSuggestion(String suggestion) {
        repository.removeSuggestion(suggestion);
    }
}
