package org.telegram.core.presenter;

import org.telegram.core.data.repository.SessionRepository;
import org.telegram.core.data.repository.SessionRepositoryImpl;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLRPC;

public class SessionsPresenter {

    public interface SessionsView {
        void onAuthorizationsLoaded(TLRPC.TL_account_authorizations authorizations);
        void onWebAuthorizationsLoaded(TLRPC.TL_account_webAuthorizations authorizations);
        void onAuthorizationsError(TLRPC.TL_error error);
    }

    private SessionsView view;
    private final SessionRepository repository;

    public SessionsPresenter(int currentAccount) {
        this(new SessionRepositoryImpl(currentAccount));
    }

    public SessionsPresenter(SessionRepository repository) {
        this.repository = repository;
    }

    public void attachView(SessionsView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void loadSessions(int currentType, int classGuid) {
        if (currentType == 0) {
            repository.loadAuthorizations(classGuid, new SessionRepository.AuthorizationsCallback() {
                @Override
                public void onSuccess(TLRPC.TL_account_authorizations authorizations) {
                    if (view != null) {
                        view.onAuthorizationsLoaded(authorizations);
                    }
                }

                @Override
                public void onError(TLRPC.TL_error error) {
                    if (view != null) {
                        view.onAuthorizationsError(error);
                    }
                }
            });
        } else {
            repository.loadWebAuthorizations(classGuid, new SessionRepository.WebAuthorizationsCallback() {
                @Override
                public void onSuccess(TLRPC.TL_account_webAuthorizations authorizations) {
                    if (view != null) {
                        view.onWebAuthorizationsLoaded(authorizations);
                    }
                }

                @Override
                public void onError(TLRPC.TL_error error) {
                    if (view != null) {
                        view.onAuthorizationsError(error);
                    }
                }
            });
        }
    }

    public void terminateSession(long hash, Runnable onComplete) {
        repository.terminateSession(hash, onComplete);
    }

    public void terminateWebSession(long hash, Runnable onComplete) {
        repository.terminateWebSession(hash, onComplete);
    }

    public void terminateAllOtherSessions(RequestDelegate callback) {
        repository.terminateAllOtherSessions(callback);
    }

    public void terminateAllWebSessions(RequestDelegate callback) {
        repository.terminateAllWebSessions(callback);
    }

    public void setAuthorizationTTL(int days) {
        repository.setAuthorizationTTL(days);
    }

    public void acceptLoginToken(byte[] token, RequestDelegate callback) {
        repository.acceptLoginToken(token, callback);
    }
}
