package org.telegram.core.data.repository;

import org.telegram.tgnet.TLRPC;
import java.util.List;

public interface ChatRepository {

    interface InviteLinkCallback {
        void onLinkLoaded(TLRPC.TL_chatInviteExported invite);
        void onNewLinkGenerated(TLRPC.TL_chatInviteExported invite);
        void onError();
    }

    TLRPC.TL_chatInviteExported getExportedInvite(long chatId);

    void loadFullChat(long chatId, int classGuid);

    int generateInviteLink(long chatId, InviteLinkCallback callback);

    TLRPC.Chat getChat(long chatId);

    TLRPC.Chat getChatSync(long chatId);

    TLRPC.ChatFull loadChatInfo(long chatId, boolean isChannel);

    void putChat(TLRPC.Chat chat, boolean inCache);

    void setChatReactions(long chatId, int selectedType, List<String> reactions);

    List<TLRPC.TL_availableReaction> getEnabledReactionsList();
}
