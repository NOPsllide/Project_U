package org.telegram.core.data.repository;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

public class CallRepositoryImpl implements CallRepository {

    private final int currentAccount;

    public CallRepositoryImpl(int currentAccount) {
        this.currentAccount = currentAccount;
    }

    @Override
    public void loadCalls(int offsetId, int limit, int classGuid, CallsCallback callback) {
        TLRPC.TL_messages_search req = new TLRPC.TL_messages_search();
        req.limit = limit;
        req.peer = new TLRPC.TL_inputPeerEmpty();
        req.filter = new TLRPC.TL_inputMessagesFilterPhoneCalls();
        req.q = "";
        req.offset_id = offsetId;
        int reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (error == null && response instanceof TLRPC.messages_Messages) {
                if (callback != null) {
                    callback.onSuccess((TLRPC.messages_Messages) response);
                }
            } else {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        }), ConnectionsManager.RequestFlagFailOnServerErrors);
        if (classGuid != 0) {
            ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(reqId, classGuid);
        }
    }

    @Override
    public void deleteCallMessages(ArrayList<Integer> messageIds, boolean revoke) {
        MessagesController.getInstance(currentAccount).deleteMessages(messageIds, null, null, 0, revoke, false);
    }

    @Override
    public void deleteAllCallsHistory(boolean revoke) {
        TLRPC.TL_messages_deletePhoneCallHistory req = new TLRPC.TL_messages_deletePhoneCallHistory();
        req.revoke = revoke;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> {
            if (response != null) {
                TLRPC.TL_messages_affectedFoundMessages res = (TLRPC.TL_messages_affectedFoundMessages) response;
                TLRPC.TL_updateDeleteMessages updateDeleteMessages = new TLRPC.TL_updateDeleteMessages();
                updateDeleteMessages.messages = res.messages;
                updateDeleteMessages.pts = res.pts;
                updateDeleteMessages.pts_count = res.pts_count;
                TLRPC.TL_updates updates = new TLRPC.TL_updates();
                updates.updates.add(updateDeleteMessages);
                MessagesController.getInstance(currentAccount).processUpdates(updates, false);
                if (res.offset != 0) {
                    deleteAllCallsHistory(revoke);
                }
            }
        });
    }

    @Override
    public ArrayList<Long> getActiveGroupCalls() {
        return MessagesController.getInstance(currentAccount).getActiveGroupCalls();
    }

    @Override
    public ChatObject.Call getGroupCall(long chatId, boolean force) {
        return MessagesController.getInstance(currentAccount).getGroupCall(chatId, force);
    }

    @Override
    public void loadFullChat(long chatId) {
        MessagesController.getInstance(currentAccount).loadFullChat(chatId, 0, true);
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
}
