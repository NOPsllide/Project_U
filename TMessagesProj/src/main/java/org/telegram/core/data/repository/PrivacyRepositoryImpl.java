package org.telegram.core.data.repository;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.support.LongSparseIntArray;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

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

    @Override
    public boolean isSupportUser(TLRPC.User user) {
        return MessagesController.isSupportUser(user);
    }

    @Override
    public void loadPrivacySettings() {
        ContactsController.getInstance(accountNum).loadPrivacySettings();
    }

    @Override
    public TLRPC.TL_globalPrivacySettings getGlobalPrivacySettings() {
        return ContactsController.getInstance(accountNum).getGlobalPrivacySettings();
    }

    @Override
    public void setAccountTTL(int days, TTLCallback callback) {
        TLRPC.TL_account_setAccountTTL req = new TLRPC.TL_account_setAccountTTL();
        req.ttl = new TLRPC.TL_accountDaysTTL();
        req.ttl.days = days;
        ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (response instanceof TLRPC.TL_boolTrue) {
                ContactsController.getInstance(accountNum).setDeleteAccountTTL(req.ttl.days);
                if (callback != null) {
                    callback.onSuccess();
                }
            }
        }));
    }

    @Override
    public void deleteAllContacts(Runnable onComplete) {
        ContactsController.getInstance(accountNum).deleteAllContacts(onComplete);
    }

    @Override
    public void clearPaymentInfo(boolean info, boolean credentials) {
        TLRPC.TL_payments_clearSavedInfo req = new TLRPC.TL_payments_clearSavedInfo();
        req.credentials = credentials;
        req.info = info;
        UserConfig.getInstance(accountNum).tmpPassword = null;
        UserConfig.getInstance(accountNum).saveConfig(false);
        ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> {});
    }

    @Override
    public void loadPasswordSettings(PasswordCallback callback) {
        TLRPC.TL_account_getPassword req = new TLRPC.TL_account_getPassword();
        ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> {
            if (response instanceof TLRPC.account_Password) {
                TLRPC.account_Password password = (TLRPC.account_Password) response;
                AndroidUtilities.runOnUIThread(() -> {
                    if (callback != null) {
                        callback.onSuccess(password);
                    }
                });
            }
        }, ConnectionsManager.RequestFlagFailOnServerErrors | ConnectionsManager.RequestFlagWithoutLogin);
    }

    @Override
    public void toggleTopPeers(boolean enabled) {
        TLRPC.TL_contacts_toggleTopPeers req = new TLRPC.TL_contacts_toggleTopPeers();
        req.enabled = enabled;
        ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> {});
    }

    @Override
    public void setGlobalPrivacySettings(boolean archiveAndMute) {
        TLRPC.TL_globalPrivacySettings globalPrivacySettings = ContactsController.getInstance(accountNum).getGlobalPrivacySettings();
        if (globalPrivacySettings != null) {
            globalPrivacySettings.archive_and_mute_new_noncontact_peers = archiveAndMute;
        }
        TLRPC.TL_account_setGlobalPrivacySettings req = new TLRPC.TL_account_setGlobalPrivacySettings();
        req.settings = new TLRPC.TL_globalPrivacySettings();
        req.settings.flags |= 1;
        req.settings.archive_and_mute_new_noncontact_peers = archiveAndMute;
        ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> {});
    }

    @Override
    public ArrayList<TLRPC.PrivacyRule> getPrivacyRules(int rulesType) {
        return ContactsController.getInstance(accountNum).getPrivacyRules(rulesType);
    }

    @Override
    public void setPrivacyRules(TLRPC.TL_account_setPrivacy req, int rulesType, SetPrivacyCallback callback) {
        ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (error == null && response instanceof TLRPC.TL_account_privacyRules) {
                TLRPC.TL_account_privacyRules privacyRules = (TLRPC.TL_account_privacyRules) response;
                MessagesController.getInstance(accountNum).putUsers(privacyRules.users, false);
                MessagesController.getInstance(accountNum).putChats(privacyRules.chats, false);
                ContactsController.getInstance(accountNum).setPrivacyRules(privacyRules.rules, rulesType);
                if (callback != null) {
                    callback.onComplete(true, null);
                }
            } else {
                if (callback != null) {
                    callback.onComplete(false, error);
                }
            }
        }), ConnectionsManager.RequestFlagFailOnServerErrors);
    }

    @Override
    public void setPhoneAddedPrivacyRules(int currentSubType) {
        TLRPC.TL_account_setPrivacy req2 = new TLRPC.TL_account_setPrivacy();
        req2.key = new TLRPC.TL_inputPrivacyKeyAddedByPhone();
        if (currentSubType == 0) {
            req2.rules.add(new TLRPC.TL_inputPrivacyValueAllowAll());
        } else {
            req2.rules.add(new TLRPC.TL_inputPrivacyValueAllowContacts());
        }
        ConnectionsManager.getInstance(accountNum).sendRequest(req2, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (error == null && response instanceof TLRPC.TL_account_privacyRules) {
                TLRPC.TL_account_privacyRules privacyRules = (TLRPC.TL_account_privacyRules) response;
                ContactsController.getInstance(accountNum).setPrivacyRules(privacyRules.rules, ContactsController.PRIVACY_RULES_TYPE_ADDED_BY_PHONE);
            }
        }), ConnectionsManager.RequestFlagFailOnServerErrors);
    }
}
