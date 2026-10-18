package org.telegram.core.data.repository;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;

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
}
