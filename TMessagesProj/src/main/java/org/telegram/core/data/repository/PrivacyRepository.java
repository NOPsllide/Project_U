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

    boolean isSupportUser(TLRPC.User user);

    void loadPrivacySettings();

    TLRPC.TL_globalPrivacySettings getGlobalPrivacySettings();

    void setAccountTTL(int days, TTLCallback callback);

    void deleteAllContacts(Runnable onComplete);

    void clearPaymentInfo(boolean info, boolean credentials);

    void loadPasswordSettings(PasswordCallback callback);

    void toggleTopPeers(boolean enabled);

    void setGlobalPrivacySettings(boolean archiveAndMute);

    interface TTLCallback {
        void onSuccess();
    }

    interface PasswordCallback {
        void onSuccess(TLRPC.account_Password password);
    }
}
