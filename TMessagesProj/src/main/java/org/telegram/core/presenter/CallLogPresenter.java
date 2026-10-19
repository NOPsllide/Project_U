package org.telegram.core.presenter;

import org.telegram.core.data.repository.CallRepository;
import org.telegram.core.data.repository.CallRepositoryImpl;
import org.telegram.messenger.ChatObject;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

public class CallLogPresenter {

    public interface CallLogView {
        void onCallsLoaded(TLRPC.messages_Messages response);
        void onCallsError(TLRPC.TL_error error);
    }

    private CallLogView view;
    private final CallRepository repository;

    public CallLogPresenter(int currentAccount) {
        this(new CallRepositoryImpl(currentAccount));
    }

    public CallLogPresenter(CallRepository repository) {
        this.repository = repository;
    }

    public void attachView(CallLogView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void loadCalls(int maxId, int count, int classGuid) {
        repository.loadCalls(maxId, count, classGuid, new CallRepository.CallsCallback() {
            @Override
            public void onSuccess(TLRPC.messages_Messages response) {
                if (view != null) {
                    view.onCallsLoaded(response);
                }
            }

            @Override
            public void onError(TLRPC.TL_error error) {
                if (view != null) {
                    view.onCallsError(error);
                }
            }
        });
    }

    public void deleteMessages(ArrayList<Integer> selectedIds, boolean revoke) {
        repository.deleteCallMessages(selectedIds, revoke);
    }

    public void deleteAllCallsHistory(boolean revoke) {
        repository.deleteAllCallsHistory(revoke);
    }

    public ArrayList<Long> getActiveGroupCalls() {
        return repository.getActiveGroupCalls();
    }

    public ChatObject.Call getGroupCall(long chatId, boolean force) {
        return repository.getGroupCall(chatId, force);
    }

    public void loadFullChat(long chatId) {
        repository.loadFullChat(chatId);
    }

    public TLRPC.User getUser(long userId) {
        return repository.getUser(userId);
    }

    public TLRPC.Chat getChat(long chatId) {
        return repository.getChat(chatId);
    }

    public TLRPC.UserFull getUserFull(long userId) {
        return repository.getUserFull(userId);
    }
}
