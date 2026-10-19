package org.telegram.core.presenter;

import org.telegram.core.data.repository.PassportRepository;
import org.telegram.core.data.repository.PassportRepositoryImpl;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;

public class PassportPresenter {

    public interface PassportView {
        void showLoading(boolean show);
        void showError(String message);
    }

    private PassportView view;
    private final PassportRepository repository;

    public PassportPresenter(int currentAccount) {
        this(new PassportRepositoryImpl(currentAccount));
    }

    public PassportPresenter(PassportRepository repository) {
        this.repository = repository;
    }

    public void attachView(PassportView view) {
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

    public void cancelRequestsForGuid(int guid) {
        repository.cancelRequestsForGuid(guid);
    }
}
