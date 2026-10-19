package org.telegram.core.presenter;

import android.app.Activity;
import org.telegram.core.data.repository.ChatRepository;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.MessagesStorage;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;

public class ChatEditPresenter {

    public interface ChatEditView {
        void onLinksCountLoaded(int count);
    }

    private final ChatRepository chatRepository;
    private final int currentAccount;
    private ChatEditView view;

    public ChatEditPresenter(ChatRepository chatRepository, int currentAccount) {
        this.chatRepository = chatRepository;
        this.currentAccount = currentAccount;
    }

    public void attachView(ChatEditView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public TLRPC.Chat getChat(long chatId) {
        return chatRepository.getChat(chatId);
    }

    public TLRPC.Chat getChatSync(long chatId) {
        return chatRepository.getChatSync(chatId);
    }

    public void putChat(TLRPC.Chat chat, boolean inCache) {
        chatRepository.putChat(chat, inCache);
    }

    public TLRPC.ChatFull loadChatInfo(long chatId, boolean isChannel) {
        return chatRepository.loadChatInfo(chatId, isChannel);
    }

    public TLRPC.ChatFull getChatFull(long chatId) {
        return chatRepository.getChatFull(chatId);
    }

    public void loadFullChat(long chatId, int classGuid, boolean force) {
        chatRepository.loadFullChat(chatId, classGuid, force);
    }

    public int getForumUpgradeParticipantsMin() {
        return chatRepository.getForumUpgradeParticipantsMin();
    }

    public void loadLinksCount(long chatId) {
        chatRepository.loadExportedChatInvites(chatId, chatRepository.getClientUserId(), false, null, 0, new ChatRepository.ExportedChatInvitesCallback() {
            @Override
            public void onSuccess(TLRPC.TL_messages_exportedChatInvites response) {
                chatRepository.saveChatLinksCount(chatId, response.count);
                if (view != null) {
                    view.onLinksCountLoaded(response.count);
                }
            }

            @Override
            public void onError(TLRPC.TL_error error) {
            }
        });
    }

    public void deleteAvatar(long chatId) {
        chatRepository.changeChatAvatar(chatId, null, null, 0, null, null, null);
    }

    public void uploadAvatar(long chatId, TLRPC.InputFile photo, TLRPC.InputFile video, double videoStartTimestamp, String videoPath, TLRPC.FileLocation avatar, TLRPC.FileLocation avatarBig) {
        chatRepository.changeChatAvatar(chatId, photo, video, videoStartTimestamp, videoPath, avatar, avatarBig);
    }

    public void convertToMegaGroup(Activity activity, long chatId, BaseFragment fragment, MessagesStorage.LongCallback callback) {
        chatRepository.convertToMegaGroup(activity, chatId, fragment, callback);
    }

    public void saveChat(long chatId, TLRPC.Chat currentChat, TLRPC.ChatFull info, String newTitle, String newAbout, boolean signMessages, boolean forum, boolean historyHidden) {
        if (info != null) {
            if (ChatObject.isChannel(currentChat) && info.hidden_prehistory != historyHidden) {
                info.hidden_prehistory = historyHidden;
                chatRepository.toggleChannelInvitesHistory(chatId, historyHidden);
            }
        }
        if (!currentChat.title.equals(newTitle)) {
            chatRepository.changeChatTitle(chatId, newTitle);
        }
        String about = info != null && info.about != null ? info.about : "";
        if (newAbout != null && !about.equals(newAbout)) {
            chatRepository.updateChatAbout(chatId, newAbout, info);
        }
        if (signMessages != currentChat.signatures) {
            currentChat.signatures = true;
            chatRepository.toggleChannelSignatures(chatId, signMessages);
        }
        if (forum != currentChat.forum) {
            chatRepository.toggleChannelForum(chatId, forum);
        }
    }
}
