package org.telegram.core.presenter;

import org.telegram.core.data.repository.ExternalActionRepository;
import org.telegram.core.data.repository.ExternalActionRepositoryImpl;
import org.telegram.tgnet.TLRPC;

public class ExternalActionPresenter {

    public interface ExternalActionView {
        void showLoadingProgress();
        void hideLoadingProgress();
        void onPassportAuthSuccess(TLRPC.TL_account_authorizationForm form, TLRPC.account_Password password);
        void onPassportAuthError(String errorText);
    }

    private final ExternalActionRepository repository;
    private ExternalActionView view;
    private int currentRequestId;

    public ExternalActionPresenter() {
        this(new ExternalActionRepositoryImpl());
    }

    public ExternalActionPresenter(ExternalActionRepository repository) {
        this.repository = repository;
    }

    public void attachView(ExternalActionView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public int getActivatedAccountsCount() {
        return repository.getActivatedAccountsCount();
    }

    public void switchToAccount(int account) {
        repository.switchToAccount(account);
    }

    public void loadAuthorizationForm(int account, long botId, String scope, String publicKey) {
        if (view != null) {
            view.showLoadingProgress();
        }
        currentRequestId = repository.getAuthorizationForm(account, botId, scope, publicKey, new ExternalActionRepository.PassportAuthCallback() {
            @Override
            public void onSuccess(TLRPC.TL_account_authorizationForm form, TLRPC.account_Password password) {
                currentRequestId = 0;
                if (view != null) {
                    view.hideLoadingProgress();
                    view.onPassportAuthSuccess(form, password);
                }
            }

            @Override
            public void onError(String errorText) {
                currentRequestId = 0;
                if (view != null) {
                    view.hideLoadingProgress();
                    view.onPassportAuthError(errorText);
                }
            }
        });
    }

    public void cancelCurrentRequest(int account) {
        if (currentRequestId != 0) {
            repository.cancelRequest(account, currentRequestId);
            currentRequestId = 0;
        }
    }
}
