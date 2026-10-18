package org.telegram.core.data.repository;

import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

public class MessageStatisticRepositoryImpl implements MessageStatisticRepository {

    private final int currentAccount;

    public MessageStatisticRepositoryImpl(int currentAccount) {
        this.currentAccount = currentAccount;
    }

    @Override
    public TLRPC.ChatFull getChatFull(long chatId) {
        return MessagesController.getInstance(currentAccount).getChatFull(chatId);
    }

    @Override
    public int loadPublicForwards(MessageObject messageObject, ArrayList<TLRPC.Message> messages, int count, int nextRate, int statsDc, int classGuid, RequestDelegate callback) {
        TLRPC.TL_stats_getMessagePublicForwards req = new TLRPC.TL_stats_getMessagePublicForwards();
        req.limit = count;
        if (messageObject.messageOwner != null && messageObject.messageOwner.fwd_from != null) {
            req.msg_id = messageObject.messageOwner.fwd_from.saved_from_msg_id;
            req.channel = MessagesController.getInstance(currentAccount).getInputChannel(-messageObject.getFromChatId());
        } else {
            req.msg_id = messageObject.getId();
            req.channel = MessagesController.getInstance(currentAccount).getInputChannel(-messageObject.getDialogId());
        }
        if (messages != null && !messages.isEmpty()) {
            TLRPC.Message message = messages.get(messages.size() - 1);
            req.offset_id = message.id;
            req.offset_peer = MessagesController.getInstance(currentAccount).getInputPeer(MessageObject.getDialogId(message));
            req.offset_rate = nextRate;
        } else {
            req.offset_peer = new TLRPC.TL_inputPeerEmpty();
        }
        int reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, callback, null, null, 0, statsDc, ConnectionsManager.ConnectionTypeGeneric, true);
        ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(reqId, classGuid);
        return reqId;
    }

    @Override
    public int loadMessageStats(MessageObject messageObject, int statsDc, int classGuid, RequestDelegate callback) {
        TLRPC.TL_stats_getMessageStats req = new TLRPC.TL_stats_getMessageStats();
        if (messageObject.messageOwner != null && messageObject.messageOwner.fwd_from != null) {
            req.msg_id = messageObject.messageOwner.fwd_from.saved_from_msg_id;
            req.channel = MessagesController.getInstance(currentAccount).getInputChannel(-messageObject.getFromChatId());
        } else {
            req.msg_id = messageObject.getId();
            req.channel = MessagesController.getInstance(currentAccount).getInputChannel(-messageObject.getDialogId());
        }
        int reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, callback, null, null, 0, statsDc, ConnectionsManager.ConnectionTypeGeneric, true);
        ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(reqId, classGuid);
        return reqId;
    }

    @Override
    public int loadAsyncGraph(String token, long x, int statsDc, int classGuid, RequestDelegate callback) {
        TLRPC.TL_stats_loadAsyncGraph request = new TLRPC.TL_stats_loadAsyncGraph();
        request.token = token;
        request.x = x;
        request.flags |= 1;
        int reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(request, callback, null, null, 0, statsDc, ConnectionsManager.ConnectionTypeGeneric, true);
        ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(reqId, classGuid);
        return reqId;
    }

    @Override
    public void cancelRequest(int reqId) {
        ConnectionsManager.getInstance(currentAccount).cancelRequest(reqId, true);
    }
}
