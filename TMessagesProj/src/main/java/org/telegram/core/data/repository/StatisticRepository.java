package org.telegram.core.data.repository;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

public interface StatisticRepository {

    TLRPC.ChatFull getChatFull(long chatId);

    TLRPC.Chat getChat(long chatId);

    int loadStats(long chatId, boolean isMegagroup, int statsDc, int classGuid, RequestDelegate callback);

    int loadAsyncGraph(String token, long x, int statsDc, int classGuid, RequestDelegate callback);

    void getMessages(long chatId, int count, int lastPostId, int classGuid);

    int loadChannelMessages(long chatId, ArrayList<Integer> ids, RequestDelegate callback);

    void putMessages(ArrayList<TLRPC.Message> messages);

    void putUser(TLRPC.User user);

    int getParticipant(long chatId, TLRPC.InputPeer peer, RequestDelegate callback);

    void bindRequestToGuid(int reqId, int classGuid);

    void cancelRequest(int reqId);
}
