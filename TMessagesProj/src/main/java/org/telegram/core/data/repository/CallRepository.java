package org.telegram.core.data.repository;

import org.telegram.messenger.ChatObject;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

public interface CallRepository {

    void loadCalls(int offsetId, int limit, int classGuid, CallsCallback callback);

    void deleteCallMessages(ArrayList<Integer> messageIds, boolean revoke);

    void deleteAllCallsHistory(boolean revoke);

    ArrayList<Long> getActiveGroupCalls();

    ChatObject.Call getGroupCall(long chatId, boolean force);

    void loadFullChat(long chatId);

    TLRPC.User getUser(long userId);

    TLRPC.Chat getChat(long chatId);

    TLRPC.UserFull getUserFull(long userId);

    interface CallsCallback {
        void onSuccess(TLRPC.messages_Messages response);
        void onError(TLRPC.TL_error error);
    }
}
