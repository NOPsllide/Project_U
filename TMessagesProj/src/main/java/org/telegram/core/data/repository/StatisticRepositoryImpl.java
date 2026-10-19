package org.telegram.core.data.repository;

import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

public class StatisticRepositoryImpl implements StatisticRepository {

    private final int currentAccount;

    public StatisticRepositoryImpl(int currentAccount) {
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
    public int loadStats(long chatId, boolean isMegagroup, int statsDc, int classGuid, RequestDelegate callback) {
        TLObject req;
        if (isMegagroup) {
            TLRPC.TL_stats_getMegagroupStats getMegagroupStats = new TLRPC.TL_stats_getMegagroupStats();
            getMegagroupStats.channel = MessagesController.getInstance(currentAccount).getInputChannel(chatId);
            req = getMegagroupStats;
        } else {
            TLRPC.TL_stats_getBroadcastStats getBroadcastStats = new TLRPC.TL_stats_getBroadcastStats();
            getBroadcastStats.channel = MessagesController.getInstance(currentAccount).getInputChannel(chatId);
            req = getBroadcastStats;
        }
        int reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, callback, null, null, 0, statsDc, ConnectionsManager.ConnectionTypeGeneric, true);
        ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(reqId, classGuid);
        return reqId;
    }

    @Override
    public int loadAsyncGraph(String token, long x, int statsDc, int classGuid, RequestDelegate callback) {
        TLRPC.TL_stats_loadAsyncGraph request = new TLRPC.TL_stats_loadAsyncGraph();
        request.token = token;
        if (x != 0) {
            request.x = x;
            request.flags |= 1;
        }
        int reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(request, callback, null, null, 0, statsDc, ConnectionsManager.ConnectionTypeGeneric, true);
        ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(reqId, classGuid);
        return reqId;
    }

    @Override
    public void getMessages(long chatId, int count, int lastPostId, int classGuid) {
        MessagesStorage.getInstance(currentAccount).getMessages(-chatId, 0, false, count, lastPostId, 0, 0, classGuid, 0, false, 0, 0, true, false);
    }

    @Override
    public int loadChannelMessages(long chatId, ArrayList<Integer> ids, RequestDelegate callback) {
        TLRPC.TL_channels_getMessages req = new TLRPC.TL_channels_getMessages();
        req.id = ids;
        req.channel = MessagesController.getInstance(currentAccount).getInputChannel(chatId);
        return ConnectionsManager.getInstance(currentAccount).sendRequest(req, callback);
    }

    @Override
    public void putMessages(ArrayList<TLRPC.Message> messages) {
        MessagesStorage.getInstance(currentAccount).putMessages(messages, false, true, true, 0, false, 0);
    }

    @Override
    public void putUser(TLRPC.User user) {
        MessagesController.getInstance(currentAccount).putUser(user, false);
    }

    @Override
    public int getParticipant(long chatId, TLRPC.InputPeer peer, RequestDelegate callback) {
        TLRPC.TL_channels_getParticipant request = new TLRPC.TL_channels_getParticipant();
        request.channel = MessagesController.getInstance(currentAccount).getInputChannel(chatId);
        request.participant = peer;
        return ConnectionsManager.getInstance(currentAccount).sendRequest(request, callback);
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
