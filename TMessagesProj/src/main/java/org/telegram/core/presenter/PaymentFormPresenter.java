package org.telegram.core.presenter;

import org.telegram.core.data.repository.PaymentRepository;
import org.telegram.core.data.repository.PaymentRepositoryImpl;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

public class PaymentFormPresenter {

    public interface PaymentFormView {
    }

    private final PaymentRepository repository;
    private final int currentAccount;
    private PaymentFormView view;

    public PaymentFormPresenter(int currentAccount) {
        this(new PaymentRepositoryImpl(currentAccount), currentAccount);
    }

    public PaymentFormPresenter(PaymentRepository repository, int currentAccount) {
        this.repository = repository;
        this.currentAccount = currentAccount;
    }

    public void attachView(PaymentFormView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public TLRPC.User getUser(long userId) {
        return repository.getUser(userId);
    }

    public TLRPC.InputPeer getInputPeer(long peerId) {
        return repository.getInputPeer(peerId);
    }

    public int sendRequest(TLObject request, RequestDelegate delegate) {
        return repository.sendRequest(request, delegate);
    }

    public int sendRequest(TLObject request, RequestDelegate delegate, int flags) {
        return repository.sendRequest(request, delegate, flags);
    }

    public int getCurrentTime() {
        return repository.getCurrentTime();
    }

    public void processUpdates(TLRPC.Updates updates, boolean isChannel) {
        repository.processUpdates(updates, isChannel);
    }

    public String getPremiumBotUsername() {
        return repository.getPremiumBotUsername();
    }

    public String getPremiumInvoiceSlug() {
        return repository.getPremiumInvoiceSlug();
    }
}
