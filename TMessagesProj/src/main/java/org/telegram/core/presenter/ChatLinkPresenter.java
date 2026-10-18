package org.telegram.core.presenter;

import org.telegram.core.data.repository.ChatRepository;
import org.telegram.core.data.repository.ChatRepositoryImpl;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

public class ChatLinkPresenter {

    public interface ChatLinkView {
        void onDiscussionGroupsLoaded(ArrayList<TLRPC.Chat> chats);
        void onDiscussionGroupsFailed();
        void onDiscussionGroupSet();
        void onDiscussionGroupSetFailed();
    }

    private ChatLinkView view;
    private final ChatRepository repository;

    public ChatLinkPresenter(int currentAccount) {
        this(new ChatRepositoryImpl(currentAccount));
    }

    public ChatLinkPresenter(ChatRepository repository) {
        this.repository = repository;
    }

    public void attachView(ChatLinkView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public TLRPC.Chat getChat(long chatId) {
        return repository.getChat(chatId);
    }

    public TLRPC.ChatFull getChatFull(long chatId) {
        return repository.getChatFull(chatId);
    }

    public void loadFullChat(long chatId, int classGuid) {
        repository.loadFullChat(chatId, classGuid);
    }

    public int loadDiscussionGroups() {
        return repository.loadDiscussionGroups(new ChatRepository.DiscussionGroupsCallback() {
            @Override
            public void onSuccess(ArrayList<TLRPC.Chat> chats) {
                if (view != null) {
                    view.onDiscussionGroupsLoaded(chats);
                }
            }

            @Override
            public void onError(TLRPC.TL_error error) {
                if (view != null) {
                    view.onDiscussionGroupsFailed();
                }
            }
        });
    }

    public int linkChat(TLRPC.Chat currentChat, TLRPC.Chat chat) {
        TLRPC.InputChannel broadcast = MessagesController.getInputChannel(currentChat);
        TLRPC.InputChannel group = MessagesController.getInputChannel(chat);
        return repository.setDiscussionGroup(broadcast, group, new ChatRepository.LinkCallback() {
            @Override
            public void onSuccess(org.telegram.tgnet.TLObject response) {
                if (view != null) {
                    view.onDiscussionGroupSet();
                }
            }

            @Override
            public void onError(TLRPC.TL_error error) {
                if (view != null) {
                    view.onDiscussionGroupSetFailed();
                }
            }
        });
    }

    public int unlinkChat(TLRPC.Chat currentChat, boolean isChannel) {
        TLRPC.InputChannel broadcast;
        TLRPC.InputChannel group;
        if (isChannel) {
            broadcast = MessagesController.getInputChannel(currentChat);
            group = new TLRPC.TL_inputChannelEmpty();
        } else {
            broadcast = new TLRPC.TL_inputChannelEmpty();
            group = MessagesController.getInputChannel(currentChat);
        }
        return repository.setDiscussionGroup(broadcast, group, new ChatRepository.LinkCallback() {
            @Override
            public void onSuccess(org.telegram.tgnet.TLObject response) {
                if (view != null) {
                    view.onDiscussionGroupSet();
                }
            }

            @Override
            public void onError(TLRPC.TL_error error) {
                if (view != null) {
                    view.onDiscussionGroupSetFailed();
                }
            }
        });
    }

    public void toggleChannelInvitesHistory(long chatId, boolean enabled) {
        repository.toggleChannelInvitesHistory(chatId, enabled);
    }

    public void toggleChatJoinRequest(long chatId, boolean enabled, Runnable onComplete) {
        repository.toggleChatJoinRequest(chatId, enabled, onComplete);
    }

    public void toggleChatJoinToSend(long chatId, boolean enabled, Runnable onComplete) {
        repository.toggleChatJoinToSend(chatId, enabled, onComplete);
    }

    public void cancelRequest(int reqId) {
        if (reqId != 0) {
            repository.cancelRequest(reqId);
        }
    }
}
