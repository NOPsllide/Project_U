package org.telegram.core.data.repository;

import org.telegram.messenger.MessageObject;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

public interface MessageStatisticRepository {

    TLRPC.ChatFull getChatFull(long chatId);

    int loadPublicForwards(MessageObject messageObject, ArrayList<TLRPC.Message> messages, int count, int nextRate, int statsDc, int classGuid, RequestDelegate callback);

    int loadMessageStats(MessageObject messageObject, int statsDc, int classGuid, RequestDelegate callback);

    int loadAsyncGraph(String token, long x, int statsDc, int classGuid, RequestDelegate callback);

    void cancelRequest(int reqId);
}
