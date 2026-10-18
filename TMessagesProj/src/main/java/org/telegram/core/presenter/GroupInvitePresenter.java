package org.telegram.core.presenter;

import org.telegram.core.data.repository.ChatRepository;
import org.telegram.messenger.NotificationCenter;
import org.telegram.tgnet.TLRPC;

public class GroupInvitePresenter implements NotificationCenter.NotificationCenterDelegate {

    public interface View {
        void showInviteLink(String link);
        void showLoading();
        void hideLoading();
        void showNewLinkDialog();
        void refreshList();
        boolean isViewAlive();
    }

    private final ChatRepository chatRepository;
    private final int accountNum;
    private final long chatId;
    private final int classGuid;
    private View view;
    private TLRPC.TL_chatInviteExported invite;

    public GroupInvitePresenter(ChatRepository chatRepository, int accountNum, long chatId, int classGuid) {
        this.chatRepository = chatRepository;
        this.accountNum = accountNum;
        this.chatId = chatId;
        this.classGuid = classGuid;
    }

    public void attachView(View view) {
        this.view = view;
        NotificationCenter.getInstance(accountNum).addObserver(this, NotificationCenter.chatInfoDidLoad);
    }

    public void detachView() {
        this.view = null;
        NotificationCenter.getInstance(accountNum).removeObserver(this, NotificationCenter.chatInfoDidLoad);
    }

    public void loadInviteLink() {
        chatRepository.loadFullChat(chatId, classGuid);
    }

    public TLRPC.TL_chatInviteExported getInvite() {
        return invite;
    }

    public String getInviteLink() {
        return invite != null ? invite.link : "error";
    }

    public boolean isChannel() {
        TLRPC.Chat chat = chatRepository.getChat(chatId);
        return chat != null && org.telegram.messenger.ChatObject.isChannel(chat) && !chat.megagroup;
    }

    public void revokeLink() {
        generateLink(true);
    }

    private void generateLink(boolean isRevoke) {
        if (view != null) view.showLoading();

        chatRepository.generateInviteLink(chatId, new ChatRepository.InviteLinkCallback() {
            @Override
            public void onLinkLoaded(TLRPC.TL_chatInviteExported loadedInvite) {
                invite = loadedInvite;
                if (view != null) {
                    view.hideLoading();
                    view.refreshList();
                }
            }

            @Override
            public void onNewLinkGenerated(TLRPC.TL_chatInviteExported newInvite) {
                invite = newInvite;
                if (view != null) {
                    view.hideLoading();
                    view.refreshList();
                    if (isRevoke) {
                        view.showNewLinkDialog();
                    }
                }
            }

            @Override
            public void onError() {
                if (view != null) {
                    view.hideLoading();
                }
            }
        });
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.chatInfoDidLoad) {
            TLRPC.ChatFull info = (TLRPC.ChatFull) args[0];
            int guid = (int) args[1];
            if (info.id == chatId && guid == classGuid) {
                invite = chatRepository.getExportedInvite(chatId);
                if (invite == null) {
                    generateLink(false);
                } else {
                    if (view != null) {
                        view.hideLoading();
                        view.refreshList();
                    }
                }
            }
        }
    }
}
