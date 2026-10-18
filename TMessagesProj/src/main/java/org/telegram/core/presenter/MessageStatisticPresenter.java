package org.telegram.core.presenter;

import org.telegram.core.data.repository.MessageStatisticRepository;
import org.telegram.core.data.repository.MessageStatisticRepositoryImpl;
import org.telegram.messenger.MessageObject;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

public class MessageStatisticPresenter {

    public interface MessageStatisticView {
        void onPublicForwardsLoaded(TLRPC.messages_Messages res);
        void onMessageStatsLoaded(TLRPC.TL_stats_messageStats stats);
        void onStatsLoadingFailed();
    }

    private MessageStatisticView view;
    private final MessageStatisticRepository repository;

    public MessageStatisticPresenter(int currentAccount) {
        this(new MessageStatisticRepositoryImpl(currentAccount));
    }

    public MessageStatisticPresenter(MessageStatisticRepository repository) {
        this.repository = repository;
    }

    public void attachView(MessageStatisticView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public TLRPC.ChatFull getChatFull(long chatId) {
        return repository.getChatFull(chatId);
    }

    public int loadPublicForwards(MessageObject messageObject, ArrayList<TLRPC.Message> messages, int count, int nextRate, int statsDc, int classGuid, RequestDelegate callback) {
        return repository.loadPublicForwards(messageObject, messages, count, nextRate, statsDc, classGuid, callback);
    }

    public int loadMessageStats(MessageObject messageObject, int statsDc, int classGuid, RequestDelegate callback) {
        return repository.loadMessageStats(messageObject, statsDc, classGuid, callback);
    }

    public int loadAsyncGraph(String token, long x, int statsDc, int classGuid, RequestDelegate callback) {
        return repository.loadAsyncGraph(token, x, statsDc, classGuid, callback);
    }

    public void cancelRequest(int reqId) {
        repository.cancelRequest(reqId);
    }
}
