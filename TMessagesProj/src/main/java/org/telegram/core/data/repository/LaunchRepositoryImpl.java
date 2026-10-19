package org.telegram.core.data.repository;

import android.os.Bundle;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;

public class LaunchRepositoryImpl implements LaunchRepository {

    private final int currentAccount;

    public LaunchRepositoryImpl(int currentAccount) {
        this.currentAccount = currentAccount;
    }

    @Override
    public TLRPC.User getUser(long userId) {
        return MessagesController.getInstance(currentAccount).getUser(userId);
    }

    @Override
    public TLRPC.Chat getChat(long chatId) {
        return MessagesController.getInstance(currentAccount).getChat(chatId);
    }

    @Override
    public TLRPC.UserFull getUserFull(long userId) {
        return MessagesController.getInstance(currentAccount).getUserFull(userId);
    }

    @Override
    public TLRPC.ChatFull getChatFull(long chatId) {
        return MessagesController.getInstance(currentAccount).getChatFull(chatId);
    }

    @Override
    public TLRPC.InputPeer getInputPeer(long peerId) {
        return MessagesController.getInstance(currentAccount).getInputPeer(peerId);
    }

    @Override
    public TLRPC.InputUser getInputUser(TLRPC.User user) {
        return MessagesController.getInstance(currentAccount).getInputUser(user);
    }

    @Override
    public TLRPC.InputChannel getInputChannel(long chatId) {
        return MessagesController.getInstance(currentAccount).getInputChannel(chatId);
    }

    @Override
    public void putUser(TLRPC.User user, boolean fromCache) {
        MessagesController.getInstance(currentAccount).putUser(user, fromCache);
    }

    @Override
    public void putUsers(ArrayList<TLRPC.User> users, boolean fromCache) {
        MessagesController.getInstance(currentAccount).putUsers(users, fromCache);
    }

    @Override
    public void putChat(TLRPC.Chat chat, boolean fromCache) {
        MessagesController.getInstance(currentAccount).putChat(chat, fromCache);
    }

    @Override
    public void putChats(ArrayList<TLRPC.Chat> chats, boolean fromCache) {
        MessagesController.getInstance(currentAccount).putChats(chats, fromCache);
    }

    @Override
    public int sendRequest(TLObject request, RequestDelegate delegate) {
        return ConnectionsManager.getInstance(currentAccount).sendRequest(request, delegate);
    }

    @Override
    public int sendRequest(TLObject request, RequestDelegate delegate, int flags) {
        return ConnectionsManager.getInstance(currentAccount).sendRequest(request, delegate, flags);
    }

    @Override
    public void cancelRequest(int reqId, boolean notifyServer) {
        ConnectionsManager.getInstance(currentAccount).cancelRequest(reqId, notifyServer);
    }

    @Override
    public int getConnectionState() {
        return ConnectionsManager.getInstance(currentAccount).getConnectionState();
    }

    @Override
    public void setAppPaused(boolean paused, boolean byTimeout) {
        ConnectionsManager.getInstance(currentAccount).setAppPaused(paused, byTimeout);
    }

    @Override
    public void processUpdates(TLRPC.Updates updates, boolean isChannel) {
        MessagesController.getInstance(currentAccount).processUpdates(updates, isChannel);
    }

    @Override
    public void openByUserName(String username, BaseFragment fragment, int type) {
        MessagesController.getInstance(currentAccount).openByUserName(username, fragment, type);
    }

    @Override
    public void checkCanOpenChat(Bundle args, BaseFragment fragment) {
        MessagesController.getInstance(currentAccount).checkCanOpenChat(args, fragment);
    }

    @Override
    public void performLogout(int reason) {
        MessagesController.getInstance(currentAccount).performLogout(reason);
    }
}
