package org.telegram.core.presenter;

import org.telegram.core.data.repository.DialogsRepository;
import org.telegram.core.data.repository.DialogsRepositoryImpl;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

public class DialogsPresenter {

    public interface DialogsView {
        void showLoading(boolean show);
        void showError(String message);
    }

    private DialogsView view;
    private final DialogsRepository repository;

    public DialogsPresenter(int currentAccount) {
        this(new DialogsRepositoryImpl(currentAccount));
    }

    public DialogsPresenter(DialogsRepository repository) {
        this.repository = repository;
    }

    public void attachView(DialogsView view) {
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

    public void loadDialogs(int folderId, int offsetId, int count, boolean fromCache) {
        repository.loadDialogs(folderId, offsetId, count, fromCache);
    }

    public void deleteDialog(long did, int keepHistory, boolean revoke) {
        repository.deleteDialog(did, keepHistory, revoke);
    }

    public boolean pinDialog(long dialogId, boolean pin, TLRPC.InputPeer peer, int folderId) {
        return repository.pinDialog(dialogId, pin, peer, folderId);
    }
}
