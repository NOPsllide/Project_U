package org.telegram.core.data.repository;

import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import java.util.ArrayList;
import java.util.List;

public interface ChatRepository {

    interface InviteLinkCallback {
        void onLinkLoaded(TLRPC.TL_chatInviteExported invite);
        void onNewLinkGenerated(TLRPC.TL_chatInviteExported invite);
        void onError();
    }

    interface LinkCallback {
        void onSuccess(TLObject response);
        void onError(TLRPC.TL_error error);
    }

    int exportChatInvite(long chatId, String title, int expireDate, int usageLimit, boolean requestNeeded, LinkCallback callback);

    int editExportedChatInvite(long chatId, String link, String title, boolean titleChanged, int expireDate, boolean expireDateChanged, int usageLimit, boolean usageLimitChanged, boolean requestNeeded, boolean requestNeededChanged, LinkCallback callback);

    TLRPC.TL_chatInviteExported getExportedInvite(long chatId);

    void loadFullChat(long chatId, int classGuid);

    int generateInviteLink(long chatId, InviteLinkCallback callback);

    TLRPC.Chat getChat(long chatId);

    TLRPC.Chat getChatSync(long chatId);

    TLRPC.ChatFull loadChatInfo(long chatId, boolean isChannel);

    void putChat(TLRPC.Chat chat, boolean inCache);

    void setChatReactions(long chatId, int selectedType, List<String> reactions);

    List<TLRPC.TL_availableReaction> getEnabledReactionsList();

    TLRPC.EncryptedChat getEncryptedChat(int chatId);

    TLRPC.User getUser(long userId);

    int loadInactiveChannels(InactiveChannelsCallback callback);

    void deleteParticipantFromChat(long chatId, TLRPC.User user);

    long getClientUserId();

    int getCurrentTime();

    boolean loadMissingUsers(ArrayList<Long> uids);

    int createChat(String title, ArrayList<Long> selectedContacts, String about, int chatType, boolean forImport, TLRPC.InputGeoPoint location, String address, BaseFragment fragment);

    void changeChatAvatar(long chatId, TLRPC.InputFile inputPhoto, TLRPC.InputFile inputVideo, double videoTimestamp, String inputVideoPath, TLRPC.FileLocation avatar, TLRPC.FileLocation avatarBig);

    void cancelRequest(int reqId);

    interface DiscussionGroupsCallback {
        void onSuccess(ArrayList<TLRPC.Chat> chats);
        void onError(TLRPC.TL_error error);
    }

    TLRPC.ChatFull getChatFull(long chatId);

    int loadDiscussionGroups(DiscussionGroupsCallback callback);

    int setDiscussionGroup(TLRPC.InputChannel broadcast, TLRPC.InputChannel group, LinkCallback callback);

    void toggleChannelInvitesHistory(long chatId, boolean enabled);

    void toggleChatJoinRequest(long chatId, boolean enabled, Runnable onComplete);

    void toggleChatJoinToSend(long chatId, boolean enabled, Runnable onComplete);

    interface InactiveChannelsCallback {
        void onSuccess(TLRPC.TL_messages_inactiveChats chats);
        void onError();
    }

    void checkPublicChannelsAdmin(LinkCallback callback);

    int checkChannelUsername(long chatId, String username, LinkCallback callback);

    void updateChannelUserName(BaseFragment fragment, long chatId, String username);

    void updateChannelUserName(BaseFragment fragment, long chatId, String username, Runnable onComplete, Runnable onError);

    int loadAdminedPublicChannels(LinkCallback callback);

    void toggleChatNoForwards(long chatId, boolean enabled);

    int exportPermanentChatInvite(long chatId, LinkCallback callback);

    int deactivateAllUsernames(long chatId, LinkCallback callback);

    void putUser(TLRPC.User user, boolean inCache);

    TLRPC.InputUser getInputUser(TLRPC.User user);

    void addUsersToChannel(long chatId, ArrayList<TLRPC.InputUser> users);

    ArrayList<TLRPC.TL_contact> getContacts();

    ArrayList<TLRPC.Dialog> getAllDialogs();

    int getMaxGroupCount();

    int getMaxMegagroupCount();

    int getMaxBroadcastCount();

    interface AdminsWithInvitesCallback {
        void onSuccess(TLRPC.TL_messages_chatAdminsWithInvites response);
        void onError(TLRPC.TL_error error);
    }

    interface ExportedChatInvitesCallback {
        void onSuccess(TLRPC.TL_messages_exportedChatInvites response);
        void onError(TLRPC.TL_error error);
    }

    interface RevokeLinkCallback {
        void onSuccess(TLObject response);
        void onError(TLRPC.TL_error error);
    }

    int loadAdminsWithInvites(long chatId, AdminsWithInvitesCallback callback);

    int loadExportedChatInvites(long chatId, long adminId, boolean revoked, String offsetLink, int offsetDate, ExportedChatInvitesCallback callback);

    void deleteRevokedExportedChatInvites(long chatId, long adminId, Runnable onComplete);

    void deleteExportedChatInvite(long chatId, String link, Runnable onComplete);

    void revokeExportedChatInvite(long chatId, String link, RevokeLinkCallback callback);

    int revokePermanentInvite(long chatId, LinkCallback callback);

    void saveChatLinksCount(long chatId, int count);

    TLRPC.TL_messages_stickerSet getStickerSetByName(String name);

    TLRPC.TL_messages_stickerSet getStickerSetByEmojiOrName(String name);

    void loadStickersByEmojiOrName(String name, boolean isDice, boolean forCurrentAccount);

    void loadFullChat(long chatId, int classGuid, boolean force);

    void changeChatTitle(long chatId, String title);

    void updateChatAbout(long chatId, String about, TLRPC.ChatFull info);

    void toggleChannelSignatures(long chatId, boolean signatures);

    void toggleChannelForum(long chatId, boolean forum);

    void convertToMegaGroup(android.app.Activity activity, long chatId, BaseFragment fragment, org.telegram.messenger.MessagesStorage.LongCallback callback);

    int getForumUpgradeParticipantsMin();

    TLRPC.UserFull getUserFull(long userId);

    void setUserAdminRole(long chatId, TLRPC.User user, TLRPC.TL_chatAdminRights adminRights, String rank, boolean isChannel, BaseFragment fragment, boolean isAddingNew, boolean asAdmin, String botHash, Runnable onFinish);

    void setUserAdminRole(long chatId, TLRPC.User user, TLRPC.TL_chatAdminRights adminRights, String rank, boolean isChannel, BaseFragment fragment, boolean isAddingNew, boolean asAdmin, String botHash, Runnable onFinish, org.telegram.messenger.MessagesController.ErrorDelegate onError);

    void setParticipantBannedRole(long chatId, TLRPC.User user, TLRPC.TL_chatBannedRights rights, TLRPC.TL_chatBannedRights bannedRights, boolean isChannel, BaseFragment fragment);

    void addUserToChat(long chatId, TLRPC.User user, int fwdCount, String botHash, BaseFragment fragment, boolean check, Runnable onFinish, org.telegram.messenger.MessagesController.ErrorDelegate onError);

    void convertToGigaGroup(android.app.Activity activity, TLRPC.Chat chat, BaseFragment fragment, org.telegram.messenger.MessagesStorage.BooleanCallback callback);

    void setDefaultBannedRole(long chatId, TLRPC.TL_chatBannedRights rights, boolean isChannel, BaseFragment fragment);

    void setChannelSlowMode(long chatId, int slowmode);

    int sendRequest(TLObject request, org.telegram.tgnet.RequestDelegate delegate);

    int sendRequest(TLObject request, org.telegram.tgnet.RequestDelegate delegate, int flags);

    void cancelRequestsForGuid(int guid);

    void bindRequestToGuid(int reqId, int classGuid);

    void putUsers(ArrayList<TLRPC.User> users, boolean fromCache);

    void putChats(ArrayList<TLRPC.Chat> chats, boolean fromCache);

    void processUpdates(TLRPC.Updates updates, boolean isChannel);

    TLRPC.InputPeer getInputPeer(long peerId);

    TLRPC.InputChannel getInputChannel(long chatId);

    void processLoadedAdminsResponse(long chatId, ArrayList<TLRPC.ChatParticipant> participants);
}

