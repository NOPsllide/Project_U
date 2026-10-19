package org.telegram.core.presenter;

import org.telegram.core.data.repository.StatisticRepository;
import org.telegram.core.data.repository.StatisticRepositoryImpl;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

public class StatisticPresenter {

    public interface StatisticView {
    }

    private StatisticView view;
    private final StatisticRepository repository;

    public StatisticPresenter(int currentAccount) {
        this(new StatisticRepositoryImpl(currentAccount));
    }

    public StatisticPresenter(StatisticRepository repository) {
        this.repository = repository;
    }

    public void attachView(StatisticView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public TLRPC.ChatFull getChatFull(long chatId) {
        return repository.getChatFull(chatId);
    }

    public TLRPC.Chat getChat(long chatId) {
        return repository.getChat(chatId);
    }

    public int loadStats(long chatId, boolean isMegagroup, int statsDc, int classGuid, RequestDelegate callback) {
        return repository.loadStats(chatId, isMegagroup, statsDc, classGuid, callback);
    }

    public int loadAsyncGraph(String token, long x, int statsDc, int classGuid, RequestDelegate callback) {
        return repository.loadAsyncGraph(token, x, statsDc, classGuid, callback);
    }

    public void getMessages(long chatId, int count, int lastPostId, int classGuid) {
        repository.getMessages(chatId, count, lastPostId, classGuid);
    }

    public int loadChannelMessages(long chatId, ArrayList<Integer> ids, RequestDelegate callback) {
        return repository.loadChannelMessages(chatId, ids, callback);
    }

    public void putMessages(ArrayList<TLRPC.Message> messages) {
        repository.putMessages(messages);
    }

    public void putUser(TLRPC.User user) {
        repository.putUser(user);
    }

    public int getParticipant(long chatId, TLRPC.InputPeer peer, RequestDelegate callback) {
        return repository.getParticipant(chatId, peer, callback);
    }

    public void bindRequestToGuid(int reqId, int classGuid) {
        repository.bindRequestToGuid(reqId, classGuid);
    }

    public void cancelRequest(int reqId) {
        repository.cancelRequest(reqId);
    }
}
