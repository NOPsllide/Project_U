package org.telegram.core.data.repository;

import org.telegram.messenger.MessagesController;
import org.telegram.messenger.support.LongSparseIntArray;
import org.telegram.tgnet.TLRPC;

public class PrivacyRepositoryImpl implements PrivacyRepository {

    private final int accountNum;

    public PrivacyRepositoryImpl(int accountNum) {
        this.accountNum = accountNum;
    }

    @Override
    public LongSparseIntArray getBlockedPeers() {
        return MessagesController.getInstance(accountNum).blockePeers;
    }

    @Override
    public int getTotalBlockedCount() {
        return MessagesController.getInstance(accountNum).totalBlockedCount;
    }

    @Override
    public boolean isBlockedEndReached() {
        return MessagesController.getInstance(accountNum).blockedEndReached;
    }

    @Override
    public void loadBlockedPeers(boolean reset) {
        MessagesController.getInstance(accountNum).getBlockedPeers(reset);
    }

    @Override
    public void blockPeer(long peerId) {
        MessagesController.getInstance(accountNum).blockPeer(peerId);
    }

    @Override
    public void unblockPeer(long peerId) {
        MessagesController.getInstance(accountNum).unblockPeer(peerId);
    }

    @Override
    public TLRPC.User getUser(long userId) {
        return MessagesController.getInstance(accountNum).getUser(userId);
    }

    @Override
    public TLRPC.Chat getChat(long chatId) {
        return MessagesController.getInstance(accountNum).getChat(chatId);
    }
}
