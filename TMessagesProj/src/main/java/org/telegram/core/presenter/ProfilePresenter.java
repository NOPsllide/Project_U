package org.telegram.core.presenter;

import org.telegram.core.data.repository.ProfileRepository;
import org.telegram.core.data.repository.ProfileRepositoryImpl;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;

public class ProfilePresenter {

    public interface ProfileView {
        void showLoading(boolean show);
        void showError(String message);
    }

    private ProfileView view;
    private final ProfileRepository repository;

    public ProfilePresenter(int currentAccount) {
        this(new ProfileRepositoryImpl(currentAccount));
    }

    public ProfilePresenter(ProfileRepository repository) {
        this.repository = repository;
    }

    public void attachView(ProfileView view) {
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
