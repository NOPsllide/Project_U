package org.telegram.core.presenter;

import org.telegram.core.data.repository.ChatRepository;
import org.telegram.core.data.repository.ChatRepositoryImpl;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

public class ChatPresenter {

    public interface ChatView {
        void showLoading(boolean show);
        void showError(String message);
    }

    private ChatView view;
    private final ChatRepository repository;

    public ChatPresenter(int currentAccount) {
        this(new ChatRepositoryImpl(currentAccount));
    }

    public ChatPresenter(ChatRepository repository) {
        this.repository = repository;
    }

    public void attachView(ChatView view) {
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

    public void cancelRequest(int requestId) {
        repository.cancelRequest(requestId);
    }

    public void cancelRequestsForGuid(int guid) {
        repository.cancelRequestsForGuid(guid);
    }

    public TLRPC.Chat getChat(long chatId) {
        return repository.getChat(chatId);
    }

    public TLRPC.User getUser(long userId) {
        return repository.getUser(userId);
    }
}
