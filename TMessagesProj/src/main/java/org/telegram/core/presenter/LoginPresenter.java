package org.telegram.core.presenter;

import org.telegram.core.data.repository.LoginRepository;
import org.telegram.core.data.repository.LoginRepositoryImpl;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

public class LoginPresenter {

    public interface LoginView {
    }

    private LoginView view;
    private final LoginRepository repository;
    private final int currentAccount;

    public LoginPresenter(int currentAccount) {
        this(new LoginRepositoryImpl(currentAccount), currentAccount);
    }

    public LoginPresenter(LoginRepository repository, int currentAccount) {
        this.repository = repository;
        this.currentAccount = currentAccount;
    }

    public void attachView(LoginView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public int sendRequest(TLObject request, RequestDelegate delegate) {
        return repository.sendRequest(request, delegate);
    }

    public int sendRequest(TLObject request, RequestDelegate delegate, int flags) {
        return repository.sendRequest(request, delegate, flags);
    }

    public void cancelRequest(int reqId, boolean notifyServer) {
        repository.cancelRequest(reqId, notifyServer);
    }

    public boolean isTestBackend() {
        return repository.isTestBackend();
    }

    public void switchBackend() {
        repository.switchBackend();
    }

    public int getCurrentTime() {
        return repository.getCurrentTime();
    }

    public void updateDcSettings() {
        repository.updateDcSettings();
    }

    public void setUserId(long userId) {
        repository.setUserId(userId);
    }

    public void cleanup() {
        repository.cleanup();
    }

    public void putUser(TLRPC.User user, boolean fromCache) {
        repository.putUser(user, fromCache);
    }

    public void removeSuggestion(long did, int type) {
        repository.removeSuggestion(did, type);
    }
}
