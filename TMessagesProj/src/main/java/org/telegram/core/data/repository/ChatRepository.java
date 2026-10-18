package org.telegram.core.data.repository;

import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
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

    interface InactiveChannelsCallback {
        void onSuccess(TLRPC.TL_messages_inactiveChats chats);
        void onError();
    }
}
