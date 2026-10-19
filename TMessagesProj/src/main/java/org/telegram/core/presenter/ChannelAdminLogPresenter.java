package org.telegram.core.presenter;

import android.os.Bundle;
import org.telegram.core.data.repository.ChannelAdminLogRepository;
import org.telegram.core.data.repository.ChannelAdminLogRepositoryImpl;
import org.telegram.messenger.MessageObject;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;

public class ChannelAdminLogPresenter {

    public interface ChannelAdminLogView {
    }

    private ChannelAdminLogView view;
    private final ChannelAdminLogRepository repository;

    public ChannelAdminLogPresenter(int currentAccount) {
        this(new ChannelAdminLogRepositoryImpl(currentAccount));
    }

    public ChannelAdminLogPresenter(ChannelAdminLogRepository repository) {
        this.repository = repository;
    }

    public void attachView(ChannelAdminLogView view) {
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

    public TLRPC.User getUser(long userId) {
        return repository.getUser(userId);
    }

    public TLRPC.UserFull getUserFull(long userId) {
        return repository.getUserFull(userId);
    }

    public TLRPC.InputUser getInputUser(TLRPC.User user) {
        return repository.getInputUser(user);
    }

    public TLRPC.InputPeer getInputPeer(long peerId) {
        return repository.getInputPeer(peerId);
    }

    public int getAdminLog(TLRPC.TL_channels_getAdminLog req, RequestDelegate callback) {
        return repository.getAdminLog(req, callback);
    }

    public int getChannelParticipants(TLRPC.TL_channels_getParticipants req, int classGuid, RequestDelegate callback) {
        return repository.getChannelParticipants(req, classGuid, callback);
    }

    public int getDiscussionMessage(TLRPC.TL_messages_getDiscussionMessage req, int classGuid, RequestDelegate callback) {
        return repository.getDiscussionMessage(req, classGuid, callback);
    }

    public int sendRequest(TLObject req, RequestDelegate callback) {
        return repository.sendRequest(req, callback);
    }

    public void setDialogHistoryTTL(long dialogId, int ttl) {
        repository.setDialogHistoryTTL(dialogId, ttl);
    }

    public void putUsers(ArrayList<TLRPC.User> users, boolean fromCache) {
        repository.putUsers(users, fromCache);
    }

    public void putChats(ArrayList<TLRPC.Chat> chats, boolean fromCache) {
        repository.putChats(chats, fromCache);
    }

    public void saveGif(MessageObject messageObject, TLRPC.Document document) {
        repository.saveGif(messageObject, document);
    }

    public boolean checkCanOpenChat(Bundle args, BaseFragment fragment) {
        return repository.checkCanOpenChat(args, fragment);
    }

    public void openByUserName(String username, BaseFragment fragment, int type) {
        repository.openByUserName(username, fragment, type);
    }

    public void bindRequestToGuid(int reqId, int classGuid) {
        repository.bindRequestToGuid(reqId, classGuid);
    }

    public void cancelRequest(int reqId) {
        repository.cancelRequest(reqId);
    }
}
