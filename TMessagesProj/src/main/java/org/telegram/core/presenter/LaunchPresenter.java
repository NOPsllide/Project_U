package org.telegram.core.presenter;

import android.os.Bundle;
import org.telegram.core.data.repository.LaunchRepository;
import org.telegram.core.data.repository.LaunchRepositoryImpl;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;

public class LaunchPresenter {

    public interface LaunchView {
    }

    private LaunchView view;
    private final LaunchRepository repository;
    private final int currentAccount;

    public LaunchPresenter(int currentAccount) {
        this(new LaunchRepositoryImpl(currentAccount), currentAccount);
    }

    public LaunchPresenter(LaunchRepository repository, int currentAccount) {
        this.repository = repository;
        this.currentAccount = currentAccount;
    }

    public void attachView(LaunchView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
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

    public TLRPC.ChatFull getChatFull(long chatId) {
        return repository.getChatFull(chatId);
    }

    public TLRPC.InputPeer getInputPeer(long peerId) {
        return repository.getInputPeer(peerId);
    }

    public TLRPC.InputUser getInputUser(TLRPC.User user) {
        return repository.getInputUser(user);
    }

    public TLRPC.InputChannel getInputChannel(long chatId) {
        return repository.getInputChannel(chatId);
    }

    public void putUser(TLRPC.User user, boolean fromCache) {
        repository.putUser(user, fromCache);
    }

    public void putUsers(ArrayList<TLRPC.User> users, boolean fromCache) {
        repository.putUsers(users, fromCache);
    }

    public void putChat(TLRPC.Chat chat, boolean fromCache) {
        repository.putChat(chat, fromCache);
    }

    public void putChats(ArrayList<TLRPC.Chat> chats, boolean fromCache) {
        repository.putChats(chats, fromCache);
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

    public int getConnectionState() {
        return repository.getConnectionState();
    }

    public void setAppPaused(boolean paused, boolean byTimeout) {
        repository.setAppPaused(paused, byTimeout);
    }

    public void processUpdates(TLRPC.Updates updates, boolean isChannel) {
        repository.processUpdates(updates, isChannel);
    }

    public void openByUserName(String username, BaseFragment fragment, int type) {
        repository.openByUserName(username, fragment, type);
    }

    public void checkCanOpenChat(Bundle args, BaseFragment fragment) {
        repository.checkCanOpenChat(args, fragment);
    }

    public void performLogout(int reason) {
        repository.performLogout(reason);
    }
}
