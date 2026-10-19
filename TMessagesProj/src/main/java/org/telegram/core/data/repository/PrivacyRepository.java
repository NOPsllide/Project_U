package org.telegram.core.data.repository;

import org.telegram.messenger.support.LongSparseIntArray;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

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

    ArrayList<TLRPC.PrivacyRule> getPrivacyRules(int rulesType);

    void setPrivacyRules(TLRPC.TL_account_setPrivacy req, int rulesType, SetPrivacyCallback callback);

    void setPhoneAddedPrivacyRules(int currentSubType);

    interface TTLCallback {
        void onSuccess();
    }

    interface PasswordCallback {
        void onSuccess(TLRPC.account_Password password);
    }

    interface SetPrivacyCallback {
        void onComplete(boolean success, TLRPC.TL_error error);
    }
}
