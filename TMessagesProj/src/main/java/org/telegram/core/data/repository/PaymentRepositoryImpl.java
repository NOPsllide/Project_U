package org.telegram.core.data.repository;

import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

public class PaymentRepositoryImpl implements PaymentRepository {

    private final int currentAccount;

    public PaymentRepositoryImpl(int currentAccount) {
        this.currentAccount = currentAccount;
    }

    @Override
    public TLRPC.User getUser(long userId) {
        return MessagesController.getInstance(currentAccount).getUser(userId);
    }

    @Override
    public TLRPC.InputPeer getInputPeer(long peerId) {
        return MessagesController.getInstance(currentAccount).getInputPeer(peerId);
    }

    @Override
    public int sendRequest(TLObject request, RequestDelegate delegate) {
        return ConnectionsManager.getInstance(currentAccount).sendRequest(request, delegate);
    }

    @Override
    public int sendRequest(TLObject request, RequestDelegate delegate, int flags) {
        return ConnectionsManager.getInstance(currentAccount).sendRequest(request, delegate, flags);
    }

    @Override
    public int getCurrentTime() {
        return ConnectionsManager.getInstance(currentAccount).getCurrentTime();
    }

    @Override
    public void processUpdates(TLRPC.Updates updates, boolean isChannel) {
        MessagesController.getInstance(currentAccount).processUpdates(updates, isChannel);
    }

    @Override
    public String getPremiumBotUsername() {
        return MessagesController.getInstance(currentAccount).premiumBotUsername;
    }

    @Override
    public String getPremiumInvoiceSlug() {
        return MessagesController.getInstance(currentAccount).premiumInvoiceSlug;
    }
}
