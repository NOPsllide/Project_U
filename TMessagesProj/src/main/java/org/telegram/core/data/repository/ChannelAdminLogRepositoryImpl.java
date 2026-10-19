package org.telegram.core.data.repository;

import android.os.Bundle;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;

public class ChannelAdminLogRepositoryImpl implements ChannelAdminLogRepository {

    private final int currentAccount;

    public ChannelAdminLogRepositoryImpl(int currentAccount) {
        this.currentAccount = currentAccount;
    }

    @Override
    public TLRPC.ChatFull getChatFull(long chatId) {
        return MessagesController.getInstance(currentAccount).getChatFull(chatId);
    }

    @Override
    public TLRPC.Chat getChat(long chatId) {
        return MessagesController.getInstance(currentAccount).getChat(chatId);
    }

    @Override
    public TLRPC.User getUser(long userId) {
        return MessagesController.getInstance(currentAccount).getUser(userId);
    }

    @Override
    public TLRPC.UserFull getUserFull(long userId) {
        return MessagesController.getInstance(currentAccount).getUserFull(userId);
    }

    @Override
    public TLRPC.InputUser getInputUser(TLRPC.User user) {
        return MessagesController.getInstance(currentAccount).getInputUser(user);
    }

    @Override
    public TLRPC.InputPeer getInputPeer(long peerId) {
        return MessagesController.getInstance(currentAccount).getInputPeer(peerId);
    }

    @Override
    public int getAdminLog(TLRPC.TL_channels_getAdminLog req, RequestDelegate callback) {
        return ConnectionsManager.getInstance(currentAccount).sendRequest(req, callback);
    }

    @Override
    public int getChannelParticipants(TLRPC.TL_channels_getParticipants req, int classGuid, RequestDelegate callback) {
        int reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, callback);
        if (classGuid != 0) {
            ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(reqId, classGuid);
        }
        return reqId;
    }

    @Override
    public int getDiscussionMessage(TLRPC.TL_messages_getDiscussionMessage req, int classGuid, RequestDelegate callback) {
        int reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, callback);
        if (classGuid != 0) {
            ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(reqId, classGuid);
        }
        return reqId;
    }

    @Override
    public int sendRequest(TLObject req, RequestDelegate callback) {
        return ConnectionsManager.getInstance(currentAccount).sendRequest(req, callback);
    }

    @Override
    public void setDialogHistoryTTL(long dialogId, int ttl) {
        MessagesController.getInstance(currentAccount).setDialogHistoryTTL(dialogId, ttl);
    }

    @Override
    public void putUsers(ArrayList<TLRPC.User> users, boolean fromCache) {
        MessagesController.getInstance(currentAccount).putUsers(users, fromCache);
    }

    @Override
    public void putChats(ArrayList<TLRPC.Chat> chats, boolean fromCache) {
        MessagesController.getInstance(currentAccount).putChats(chats, fromCache);
    }

    @Override
    public void saveGif(MessageObject messageObject, TLRPC.Document document) {
        MessagesController.getInstance(currentAccount).saveGif(messageObject, document);
    }

    @Override
    public boolean checkCanOpenChat(Bundle args, BaseFragment fragment) {
        return MessagesController.getInstance(currentAccount).checkCanOpenChat(args, fragment);
    }

    @Override
    public void openByUserName(String username, BaseFragment fragment, int type) {
        MessagesController.getInstance(currentAccount).openByUserName(username, fragment, type);
    }

    @Override
    public void bindRequestToGuid(int reqId, int classGuid) {
        ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(reqId, classGuid);
    }

    @Override
    public void cancelRequest(int reqId) {
        ConnectionsManager.getInstance(currentAccount).cancelRequest(reqId, true);
    }
}
