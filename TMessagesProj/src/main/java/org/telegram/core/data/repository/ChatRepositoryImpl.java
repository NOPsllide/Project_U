package org.telegram.core.data.repository;

import android.text.TextUtils;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ChatRepositoryImpl implements ChatRepository {

    private final int accountNum;

    public ChatRepositoryImpl(int accountNum) {
        this.accountNum = accountNum;
    }

    @Override
    public TLRPC.TL_chatInviteExported getExportedInvite(long chatId) {
        return MessagesController.getInstance(accountNum).getExportedInvite(chatId);
    }

    @Override
    public void loadFullChat(long chatId, int classGuid) {
        MessagesController.getInstance(accountNum).loadFullChat(chatId, classGuid, true);
    }

    @Override
    public int generateInviteLink(long chatId, InviteLinkCallback callback) {
        TLRPC.TL_messages_exportChatInvite req = new TLRPC.TL_messages_exportChatInvite();
        req.peer = MessagesController.getInstance(accountNum).getInputPeer(-chatId);

        return ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) ->
                AndroidUtilities.runOnUIThread(() -> {
                    if (error == null) {
                        TLRPC.TL_chatInviteExported invite = (TLRPC.TL_chatInviteExported) response;
                        callback.onNewLinkGenerated(invite);
                    } else {
                        callback.onError();
                    }
                })
        );
    }

    @Override
    public TLRPC.Chat getChat(long chatId) {
        return MessagesController.getInstance(accountNum).getChat(chatId);
    }

    @Override
    public TLRPC.Chat getChatSync(long chatId) {
        return MessagesStorage.getInstance(accountNum).getChatSync(chatId);
    }

    @Override
    public TLRPC.ChatFull loadChatInfo(long chatId, boolean isChannel) {
        return MessagesStorage.getInstance(accountNum).loadChatInfo(chatId, isChannel, new CountDownLatch(1), false, false);
    }

    @Override
    public void putChat(TLRPC.Chat chat, boolean inCache) {
        MessagesController.getInstance(accountNum).putChat(chat, inCache);
    }

    @Override
    public void setChatReactions(long chatId, int selectedType, List<String> reactions) {
        MessagesController.getInstance(accountNum).setChatReactions(chatId, selectedType, reactions);
    }

    @Override
    public List<TLRPC.TL_availableReaction> getEnabledReactionsList() {
        return new ArrayList<>(MediaDataController.getInstance(accountNum).getEnabledReactionsList());
    }

    @Override
    public TLRPC.EncryptedChat getEncryptedChat(int chatId) {
        return MessagesController.getInstance(accountNum).getEncryptedChat(chatId);
    }

    @Override
    public TLRPC.User getUser(long userId) {
        return MessagesController.getInstance(accountNum).getUser(userId);
    }

    @Override
    public int loadInactiveChannels(InactiveChannelsCallback callback) {
        TLRPC.TL_channels_getInactiveChannels inactiveChannelsRequest = new TLRPC.TL_channels_getInactiveChannels();
        return ConnectionsManager.getInstance(accountNum).sendRequest(inactiveChannelsRequest, (response, error) -> {
            if (error == null && response instanceof TLRPC.TL_messages_inactiveChats) {
                TLRPC.TL_messages_inactiveChats chats = (TLRPC.TL_messages_inactiveChats) response;
                AndroidUtilities.runOnUIThread(() -> {
                    if (callback != null) {
                        callback.onSuccess(chats);
                    }
                });
            } else {
                AndroidUtilities.runOnUIThread(() -> {
                    if (callback != null) {
                        callback.onError();
                    }
                });
            }
        });
    }

    @Override
    public void deleteParticipantFromChat(long chatId, TLRPC.User user) {
        MessagesController.getInstance(accountNum).deleteParticipantFromChat(chatId, user);
    }

    @Override
    public long getClientUserId() {
        return UserConfig.getInstance(accountNum).getClientUserId();
    }

    @Override
    public int getCurrentTime() {
        return ConnectionsManager.getInstance(accountNum).getCurrentTime();
    }

    @Override
    public int exportChatInvite(long chatId, String title, int expireDate, int usageLimit, boolean requestNeeded, LinkCallback callback) {
        TLRPC.TL_messages_exportChatInvite req = new TLRPC.TL_messages_exportChatInvite();
        req.peer = MessagesController.getInstance(accountNum).getInputPeer(-chatId);
        req.legacy_revoke_permanent = false;
        req.flags |= 1;
        req.expire_date = expireDate;
        req.flags |= 2;
        req.usage_limit = usageLimit;
        req.request_needed = requestNeeded;
        if (req.request_needed) {
            req.usage_limit = 0;
        }
        req.title = title;
        if (!TextUtils.isEmpty(req.title)) {
            req.flags |= 16;
        }
        return ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (error == null) {
                if (callback != null) {
                    callback.onSuccess(response);
                }
            } else {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        }));
    }

    @Override
    public int editExportedChatInvite(long chatId, String link, String title, boolean titleChanged, int expireDate, boolean expireDateChanged, int usageLimit, boolean usageLimitChanged, boolean requestNeeded, boolean requestNeededChanged, LinkCallback callback) {
        TLRPC.TL_messages_editExportedChatInvite req = new TLRPC.TL_messages_editExportedChatInvite();
        req.link = link;
        req.revoked = false;
        req.peer = MessagesController.getInstance(accountNum).getInputPeer(-chatId);

        if (expireDateChanged) {
            req.flags |= 1;
            req.expire_date = expireDate;
        }
        if (usageLimitChanged) {
            req.flags |= 2;
            req.usage_limit = usageLimit;
        }
        if (requestNeededChanged) {
            req.flags |= 8;
            req.request_needed = requestNeeded;
            if (req.request_needed) {
                req.flags |= 2;
                req.usage_limit = 0;
            }
        }
        if (titleChanged) {
            req.title = title;
            req.flags |= 16;
        }

        return ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (error == null) {
                if (callback != null) {
                    callback.onSuccess(response);
                }
            } else {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        }));
    }

    @Override
    public boolean loadMissingUsers(ArrayList<Long> uids) {
        if (uids == null || uids.isEmpty()) {
            return true;
        }
        ArrayList<Long> usersToLoad = new ArrayList<>();
        for (int a = 0; a < uids.size(); a++) {
            Long uid = uids.get(a);
            if (MessagesController.getInstance(accountNum).getUser(uid) == null) {
                usersToLoad.add(uid);
            }
        }
        if (usersToLoad.isEmpty()) {
            return true;
        }
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ArrayList<TLRPC.User> users = new ArrayList<>();
        MessagesStorage.getInstance(accountNum).getStorageQueue().postRunnable(() -> {
            users.addAll(MessagesStorage.getInstance(accountNum).getUsers(usersToLoad));
            countDownLatch.countDown();
        });
        try {
            countDownLatch.await();
        } catch (Exception e) {
            FileLog.e(e);
        }
        if (usersToLoad.size() != users.size()) {
            return false;
        }
        if (!users.isEmpty()) {
            for (TLRPC.User user : users) {
                MessagesController.getInstance(accountNum).putUser(user, true);
            }
            return true;
        }
        return false;
    }

    @Override
    public int createChat(String title, ArrayList<Long> selectedContacts, String about, int chatType, boolean forImport, TLRPC.InputGeoPoint location, String address, BaseFragment fragment) {
        return MessagesController.getInstance(accountNum).createChat(title, selectedContacts, about, chatType, forImport, location, address, fragment);
    }

    @Override
    public void changeChatAvatar(long chatId, TLRPC.InputFile inputPhoto, TLRPC.InputFile inputVideo, double videoTimestamp, String inputVideoPath, TLRPC.FileLocation avatar, TLRPC.FileLocation avatarBig) {
        MessagesController.getInstance(accountNum).changeChatAvatar(chatId, null, inputPhoto, inputVideo, videoTimestamp, inputVideoPath, avatar, avatarBig, null);
    }

    @Override
    public void cancelRequest(int reqId) {
        ConnectionsManager.getInstance(accountNum).cancelRequest(reqId, true);
    }

    @Override
    public TLRPC.ChatFull getChatFull(long chatId) {
        return MessagesController.getInstance(accountNum).getChatFull(chatId);
    }

    @Override
    public int loadDiscussionGroups(DiscussionGroupsCallback callback) {
        TLRPC.TL_channels_getGroupsForDiscussion req = new TLRPC.TL_channels_getGroupsForDiscussion();
        return ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (error == null && response instanceof TLRPC.messages_Chats) {
                TLRPC.messages_Chats res = (TLRPC.messages_Chats) response;
                MessagesController.getInstance(accountNum).putChats(res.chats, false);
                if (callback != null) {
                    callback.onSuccess(res.chats);
                }
            } else {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        }));
    }

    @Override
    public int setDiscussionGroup(TLRPC.InputChannel broadcast, TLRPC.InputChannel group, LinkCallback callback) {
        TLRPC.TL_channels_setDiscussionGroup req = new TLRPC.TL_channels_setDiscussionGroup();
        req.broadcast = broadcast;
        req.group = group;
        return ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (error == null) {
                if (callback != null) {
                    callback.onSuccess(response);
                }
            } else {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        }), ConnectionsManager.RequestFlagInvokeAfter);
    }

    @Override
    public void toggleChannelInvitesHistory(long chatId, boolean enabled) {
        MessagesController.getInstance(accountNum).toggleChannelInvitesHistory(chatId, enabled);
    }

    @Override
    public void toggleChatJoinRequest(long chatId, boolean enabled, Runnable onComplete) {
        MessagesController.getInstance(accountNum).toggleChatJoinRequest(chatId, enabled, onComplete);
    }

    @Override
    public void toggleChatJoinToSend(long chatId, boolean enabled, Runnable onComplete) {
        MessagesController.getInstance(accountNum).toggleChatJoinToSend(chatId, enabled, onComplete);
    }

    @Override
    public void checkPublicChannelsAdmin(LinkCallback callback) {
        TLRPC.TL_channels_checkUsername req = new TLRPC.TL_channels_checkUsername();
        req.channel = new TLRPC.TL_inputChannelEmpty();
        req.username = "1";
        ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (callback != null) {
                if (error == null) {
                    callback.onSuccess(response);
                } else {
                    callback.onError(error);
                }
            }
        }));
    }

    @Override
    public int checkChannelUsername(long chatId, String username, LinkCallback callback) {
        TLRPC.TL_channels_checkUsername req = new TLRPC.TL_channels_checkUsername();
        req.channel = MessagesController.getInstance(accountNum).getInputChannel(chatId);
        req.username = username;
        return ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (callback != null) {
                if (error == null) {
                    callback.onSuccess(response);
                } else {
                    callback.onError(error);
                }
            }
        }), ConnectionsManager.RequestFlagFailOnServerErrors);
    }

    @Override
    public void updateChannelUserName(BaseFragment fragment, long chatId, String username) {
        MessagesController.getInstance(accountNum).updateChannelUserName(fragment, chatId, username, null, null);
    }

    @Override
    public void updateChannelUserName(BaseFragment fragment, long chatId, String username, Runnable onComplete, Runnable onError) {
        MessagesController.getInstance(accountNum).updateChannelUserName(fragment, chatId, username, onComplete, onError);
    }

    @Override
    public int loadAdminedPublicChannels(LinkCallback callback) {
        TLRPC.TL_channels_getAdminedPublicChannels req = new TLRPC.TL_channels_getAdminedPublicChannels();
        return ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (callback != null) {
                if (error == null) {
                    callback.onSuccess(response);
                } else {
                    callback.onError(error);
                }
            }
        }));
    }

    @Override
    public void toggleChatNoForwards(long chatId, boolean enabled) {
        MessagesController.getInstance(accountNum).toggleChatNoForwards(chatId, enabled);
    }

    @Override
    public int exportPermanentChatInvite(long chatId, LinkCallback callback) {
        TLRPC.TL_messages_exportChatInvite req = new TLRPC.TL_messages_exportChatInvite();
        req.legacy_revoke_permanent = true;
        req.peer = MessagesController.getInstance(accountNum).getInputPeer(-chatId);
        return ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (callback != null) {
                if (error == null) {
                    callback.onSuccess(response);
                } else {
                    callback.onError(error);
                }
            }
        }));
    }

    @Override
    public int deactivateAllUsernames(long chatId, LinkCallback callback) {
        TLRPC.TL_channels_deactivateAllUsernames req = new TLRPC.TL_channels_deactivateAllUsernames();
        req.channel = MessagesController.getInstance(accountNum).getInputChannel(chatId);
        return ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (callback != null) {
                if (error == null) {
                    callback.onSuccess(response);
                } else {
                    callback.onError(error);
                }
            }
        }));
    }

    @Override
    public void putUser(TLRPC.User user, boolean inCache) {
        MessagesController.getInstance(accountNum).putUser(user, inCache);
    }

    @Override
    public TLRPC.InputUser getInputUser(TLRPC.User user) {
        return MessagesController.getInstance(accountNum).getInputUser(user);
    }

    @Override
    public void addUsersToChannel(long chatId, ArrayList<TLRPC.InputUser> users) {
        MessagesController.getInstance(accountNum).addUsersToChannel(chatId, users, null);
    }

    @Override
    public ArrayList<TLRPC.TL_contact> getContacts() {
        return ContactsController.getInstance(accountNum).contacts;
    }

    @Override
    public ArrayList<TLRPC.Dialog> getAllDialogs() {
        return MessagesController.getInstance(accountNum).getAllDialogs();
    }

    @Override
    public int getMaxGroupCount() {
        return MessagesController.getInstance(accountNum).maxGroupCount;
    }

    @Override
    public int getMaxMegagroupCount() {
        return MessagesController.getInstance(accountNum).maxMegagroupCount;
    }

    @Override
    public int getMaxBroadcastCount() {
        return MessagesController.getInstance(accountNum).maxBroadcastCount;
    }

    @Override
    public int loadAdminsWithInvites(long chatId, AdminsWithInvitesCallback callback) {
        TLRPC.TL_messages_getAdminsWithInvites req = new TLRPC.TL_messages_getAdminsWithInvites();
        req.peer = MessagesController.getInstance(accountNum).getInputPeer(-chatId);
        return ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (callback != null) {
                if (error == null && response instanceof TLRPC.TL_messages_chatAdminsWithInvites) {
                    callback.onSuccess((TLRPC.TL_messages_chatAdminsWithInvites) response);
                } else {
                    callback.onError(error);
                }
            }
        }));
    }

    @Override
    public int loadExportedChatInvites(long chatId, long adminId, boolean revoked, String offsetLink, int offsetDate, ExportedChatInvitesCallback callback) {
        TLRPC.TL_messages_getExportedChatInvites req = new TLRPC.TL_messages_getExportedChatInvites();
        req.peer = MessagesController.getInstance(accountNum).getInputPeer(-chatId);
        if (adminId == UserConfig.getInstance(accountNum).getClientUserId()) {
            req.admin_id = MessagesController.getInstance(accountNum).getInputUser(UserConfig.getInstance(accountNum).getCurrentUser());
        } else {
            req.admin_id = MessagesController.getInstance(accountNum).getInputUser(adminId);
        }
        req.revoked = revoked;
        if (offsetLink != null) {
            req.flags |= 4;
            req.offset_link = offsetLink;
            req.offset_date = offsetDate;
        }
        return ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (callback != null) {
                if (error == null && response instanceof TLRPC.TL_messages_exportedChatInvites) {
                    callback.onSuccess((TLRPC.TL_messages_exportedChatInvites) response);
                } else {
                    callback.onError(error);
                }
            }
        }));
    }

    @Override
    public void deleteRevokedExportedChatInvites(long chatId, long adminId, Runnable onComplete) {
        TLRPC.TL_messages_deleteRevokedExportedChatInvites req = new TLRPC.TL_messages_deleteRevokedExportedChatInvites();
        req.peer = MessagesController.getInstance(accountNum).getInputPeer(-chatId);
        if (adminId == UserConfig.getInstance(accountNum).getClientUserId()) {
            req.admin_id = MessagesController.getInstance(accountNum).getInputUser(UserConfig.getInstance(accountNum).getCurrentUser());
        } else {
            req.admin_id = MessagesController.getInstance(accountNum).getInputUser(adminId);
        }
        ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (error == null && onComplete != null) {
                onComplete.run();
            }
        }));
    }

    @Override
    public void deleteExportedChatInvite(long chatId, String link, Runnable onComplete) {
        TLRPC.TL_messages_deleteExportedChatInvite req = new TLRPC.TL_messages_deleteExportedChatInvite();
        req.link = link;
        req.peer = MessagesController.getInstance(accountNum).getInputPeer(-chatId);
        ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (error == null && onComplete != null) {
                onComplete.run();
            }
        }));
    }

    @Override
    public void revokeExportedChatInvite(long chatId, String link, RevokeLinkCallback callback) {
        TLRPC.TL_messages_editExportedChatInvite req = new TLRPC.TL_messages_editExportedChatInvite();
        req.link = link;
        req.revoked = true;
        req.peer = MessagesController.getInstance(accountNum).getInputPeer(-chatId);
        ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (callback != null) {
                if (error == null) {
                    callback.onSuccess(response);
                } else {
                    callback.onError(error);
                }
            }
        }));
    }

    @Override
    public int revokePermanentInvite(long chatId, LinkCallback callback) {
        TLRPC.TL_messages_exportChatInvite req = new TLRPC.TL_messages_exportChatInvite();
        req.peer = MessagesController.getInstance(accountNum).getInputPeer(-chatId);
        req.legacy_revoke_permanent = true;
        return ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (callback != null) {
                if (error == null) {
                    callback.onSuccess(response);
                } else {
                    callback.onError(error);
                }
            }
        }));
    }

    @Override
    public void saveChatLinksCount(long chatId, int count) {
        MessagesStorage.getInstance(accountNum).saveChatLinksCount(chatId, count);
    }

    @Override
    public TLRPC.TL_messages_stickerSet getStickerSetByName(String name) {
        return MediaDataController.getInstance(accountNum).getStickerSetByName(name);
    }

    @Override
    public TLRPC.TL_messages_stickerSet getStickerSetByEmojiOrName(String name) {
        return MediaDataController.getInstance(accountNum).getStickerSetByEmojiOrName(name);
    }

    @Override
    public void loadStickersByEmojiOrName(String name, boolean isDice, boolean forCurrentAccount) {
        MediaDataController.getInstance(accountNum).loadStickersByEmojiOrName(name, isDice, forCurrentAccount);
    }
}
