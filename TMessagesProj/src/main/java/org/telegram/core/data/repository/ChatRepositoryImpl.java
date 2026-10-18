package org.telegram.core.data.repository;

import android.text.TextUtils;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
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
}
