package org.telegram.core.data.repository;

import android.os.Bundle;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;

public interface LaunchRepository {

    TLRPC.User getUser(long userId);

    TLRPC.Chat getChat(long chatId);

    TLRPC.UserFull getUserFull(long userId);

    TLRPC.ChatFull getChatFull(long chatId);

    TLRPC.InputPeer getInputPeer(long peerId);

    TLRPC.InputUser getInputUser(TLRPC.User user);

    TLRPC.InputChannel getInputChannel(long chatId);

    void putUser(TLRPC.User user, boolean fromCache);

    void putUsers(ArrayList<TLRPC.User> users, boolean fromCache);

    void putChat(TLRPC.Chat chat, boolean fromCache);

    void putChats(ArrayList<TLRPC.Chat> chats, boolean fromCache);

    int sendRequest(TLObject request, RequestDelegate delegate);

    int sendRequest(TLObject request, RequestDelegate delegate, int flags);

    void cancelRequest(int reqId, boolean notifyServer);

    int getConnectionState();

    void setAppPaused(boolean paused, boolean byTimeout);

    void processUpdates(TLRPC.Updates updates, boolean isChannel);

    void openByUserName(String username, BaseFragment fragment, int type);

    void checkCanOpenChat(Bundle args, BaseFragment fragment);

    void performLogout(int reason);
}
