package org.telegram.core.data.repository;

import org.telegram.tgnet.TLRPC;

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
}
