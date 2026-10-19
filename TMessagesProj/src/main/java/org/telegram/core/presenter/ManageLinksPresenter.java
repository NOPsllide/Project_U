package org.telegram.core.presenter;

import org.telegram.core.data.repository.ChatRepository;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import java.util.ArrayList;
import java.util.HashMap;

public class ManageLinksPresenter {

    public interface ManageLinksView {
        void onAdminsLoaded(ArrayList<TLRPC.TL_chatAdminWithInvites> admins, HashMap<Long, TLRPC.User> users);
        void onInvitesLoaded(ArrayList<TLRPC.TL_chatInviteExported> invites, HashMap<Long, TLRPC.User> users, boolean revoked, int totalCount, TLRPC.TL_chatInviteExported permanentLink);
        void onInvitesLoadFailed();
        void onRevokedDeleted();
        void onLinkDeleted(TLRPC.TL_chatInviteExported invite);
        void onLinkRevoked(TLRPC.TL_chatInviteExported oldInvite, TLObject response);
        void onPermanentRevoked(TLRPC.TL_chatInviteExported newInvite);
    }

    private final ChatRepository chatRepository;
    private final int currentAccount;
    private ManageLinksView view;

    public ManageLinksPresenter(ChatRepository chatRepository, int currentAccount) {
        this.chatRepository = chatRepository;
        this.currentAccount = currentAccount;
    }

    public void attachView(ManageLinksView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public TLRPC.Chat getChat(long chatId) {
        return chatRepository.getChat(chatId);
    }

    public TLRPC.User getUser(long userId) {
        return chatRepository.getUser(userId);
    }

    public void saveChatLinksCount(long chatId, int count) {
        chatRepository.saveChatLinksCount(chatId, count);
    }

    public TLRPC.TL_messages_stickerSet getStickerSetByName(String name) {
        return chatRepository.getStickerSetByName(name);
    }

    public TLRPC.TL_messages_stickerSet getStickerSetByEmojiOrName(String name) {
        return chatRepository.getStickerSetByEmojiOrName(name);
    }

    public void loadStickersByEmojiOrName(String name, boolean isDice, boolean forCurrentAccount) {
        chatRepository.loadStickersByEmojiOrName(name, isDice, forCurrentAccount);
    }

    public void loadAdmins(long chatId) {
        chatRepository.loadAdminsWithInvites(chatId, new ChatRepository.AdminsWithInvitesCallback() {
            @Override
            public void onSuccess(TLRPC.TL_messages_chatAdminsWithInvites response) {
                ArrayList<TLRPC.TL_chatAdminWithInvites> admins = new ArrayList<>();
                HashMap<Long, TLRPC.User> users = new HashMap<>();
                long clientUserId = chatRepository.getClientUserId();
                for (int i = 0; i < response.admins.size(); i++) {
                    TLRPC.TL_chatAdminWithInvites admin = response.admins.get(i);
                    if (admin.admin_id != clientUserId) {
                        admins.add(admin);
                    }
                }
                for (int i = 0; i < response.users.size(); i++) {
                    TLRPC.User user = response.users.get(i);
                    users.put(user.id, user);
                }
                if (view != null) {
                    view.onAdminsLoaded(admins, users);
                }
            }

            @Override
            public void onError(TLRPC.TL_error error) {
                if (view != null) {
                    view.onInvitesLoadFailed();
                }
            }
        });
    }

    public void loadExportedInvites(long chatId, long adminId, boolean revoked, String offsetLink, int offsetDate, TLRPC.TL_chatInviteExported inviteFinal) {
        chatRepository.loadExportedChatInvites(chatId, adminId, revoked, offsetLink, offsetDate, new ChatRepository.ExportedChatInvitesCallback() {
            @Override
            public void onSuccess(TLRPC.TL_messages_exportedChatInvites response) {
                TLRPC.TL_chatInviteExported permanentLink = null;
                if (response.invites.size() > 0 && inviteFinal != null) {
                    for (int i = 0; i < response.invites.size(); i++) {
                        if (((TLRPC.TL_chatInviteExported) response.invites.get(i)).link.equals(inviteFinal.link)) {
                            permanentLink = (TLRPC.TL_chatInviteExported) response.invites.remove(i);
                            break;
                        }
                    }
                }
                ArrayList<TLRPC.TL_chatInviteExported> result = new ArrayList<>();
                for (int i = 0; i < response.invites.size(); i++) {
                    result.add((TLRPC.TL_chatInviteExported) response.invites.get(i));
                }
                HashMap<Long, TLRPC.User> users = new HashMap<>();
                for (int i = 0; i < response.users.size(); i++) {
                    users.put(response.users.get(i).id, response.users.get(i));
                }
                if (view != null) {
                    view.onInvitesLoaded(result, users, revoked, response.count, permanentLink);
                }
            }

            @Override
            public void onError(TLRPC.TL_error error) {
                if (view != null) {
                    view.onInvitesLoadFailed();
                }
            }
        });
    }

    public void deleteRevokedLinks(long chatId, long adminId) {
        chatRepository.deleteRevokedExportedChatInvites(chatId, adminId, () -> {
            if (view != null) {
                view.onRevokedDeleted();
            }
        });
    }

    public void deleteLink(long chatId, TLRPC.TL_chatInviteExported invite) {
        chatRepository.deleteExportedChatInvite(chatId, invite.link, () -> {
            if (view != null) {
                view.onLinkDeleted(invite);
            }
        });
    }

    public void revokeLink(long chatId, TLRPC.TL_chatInviteExported invite) {
        chatRepository.revokeExportedChatInvite(chatId, invite.link, new ChatRepository.RevokeLinkCallback() {
            @Override
            public void onSuccess(TLObject response) {
                if (view != null) {
                    view.onLinkRevoked(invite, response);
                }
            }

            @Override
            public void onError(TLRPC.TL_error error) {
            }
        });
    }

    public void revokePermanent(long chatId) {
        chatRepository.revokePermanentInvite(chatId, new ChatRepository.LinkCallback() {
            @Override
            public void onSuccess(TLObject response) {
                if (view != null && response instanceof TLRPC.TL_chatInviteExported) {
                    view.onPermanentRevoked((TLRPC.TL_chatInviteExported) response);
                }
            }

            @Override
            public void onError(TLRPC.TL_error error) {
            }
        });
    }
}
