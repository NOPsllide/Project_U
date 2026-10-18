package org.telegram.core.data.repository;

import org.telegram.messenger.support.LongSparseIntArray;
import org.telegram.tgnet.TLRPC;

public interface PrivacyRepository {

    LongSparseIntArray getBlockedPeers();

    int getTotalBlockedCount();

    boolean isBlockedEndReached();

    void loadBlockedPeers(boolean reset);

    void blockPeer(long peerId);

    void unblockPeer(long peerId);

    TLRPC.User getUser(long userId);

    TLRPC.Chat getChat(long chatId);
}
