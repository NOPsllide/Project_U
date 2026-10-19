package org.telegram.core.data.repository;

import org.telegram.messenger.MessageObject;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;

public interface ChannelAdminLogRepository {

    TLRPC.ChatFull getChatFull(long chatId);

    TLRPC.Chat getChat(long chatId);

    TLRPC.User getUser(long userId);

    TLRPC.UserFull getUserFull(long userId);

    TLRPC.InputUser getInputUser(TLRPC.User user);

    TLRPC.InputPeer getInputPeer(long peerId);

    int getAdminLog(TLRPC.TL_channels_getAdminLog req, RequestDelegate callback);

    int getChannelParticipants(TLRPC.TL_channels_getParticipants req, int classGuid, RequestDelegate callback);

    int getDiscussionMessage(TLRPC.TL_messages_getDiscussionMessage req, int classGuid, RequestDelegate callback);

    int sendRequest(TLObject req, RequestDelegate callback);

    void setDialogHistoryTTL(long dialogId, int ttl);

    void putUsers(ArrayList<TLRPC.User> users, boolean fromCache);

    void putChats(ArrayList<TLRPC.Chat> chats, boolean fromCache);

    void saveGif(MessageObject messageObject, TLRPC.Document document);

    boolean checkCanOpenChat(android.os.Bundle args, BaseFragment fragment);

    void openByUserName(String username, BaseFragment fragment, int type);

    void bindRequestToGuid(int reqId, int classGuid);

    void cancelRequest(int reqId);
}
