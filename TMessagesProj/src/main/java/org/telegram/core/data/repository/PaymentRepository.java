package org.telegram.core.data.repository;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

public interface PaymentRepository {

    TLRPC.User getUser(long userId);

    TLRPC.InputPeer getInputPeer(long peerId);

    int sendRequest(TLObject request, RequestDelegate delegate);

    int sendRequest(TLObject request, RequestDelegate delegate, int flags);

    int getCurrentTime();

    void processUpdates(TLRPC.Updates updates, boolean isChannel);

    String getPremiumBotUsername();

    String getPremiumInvoiceSlug();
}
