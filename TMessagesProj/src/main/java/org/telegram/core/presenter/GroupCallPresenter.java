package org.telegram.core.presenter;

import org.telegram.messenger.AccountInstance;
import org.telegram.core.data.repository.GroupCallRepository;
import org.telegram.core.data.repository.GroupCallRepositoryImpl;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;

public class GroupCallPresenter {

    public interface GroupCallView {
        void showLoading(boolean show);
        void showError(String message);
    }

    private GroupCallView view;
    private final GroupCallRepository repository;

    public GroupCallPresenter(AccountInstance accountInstance) {
        this(new GroupCallRepositoryImpl(accountInstance));
    }

    public GroupCallPresenter(int currentAccount) {
        this(new GroupCallRepositoryImpl(currentAccount));
    }

    public GroupCallPresenter(GroupCallRepository repository) {
        this.repository = repository;
    }

    public void attachView(GroupCallView view) {
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

    public void cancelRequest(int requestId, boolean notify) {
        repository.cancelRequest(requestId, notify);
    }
}
