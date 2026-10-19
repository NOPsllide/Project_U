package org.telegram.core.presenter;

import org.telegram.core.data.repository.ChatRepository;
import org.telegram.core.data.repository.ChatRepositoryImpl;
import org.telegram.messenger.NotificationCenter;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import java.util.ArrayList;

public class ChatEditTypePresenter {

    public interface ChatEditTypeView {
        void onChatInfoLoaded(TLRPC.ChatFull chatFull);
        void onAdminedChannelsLoaded(ArrayList<TLRPC.Chat> channels);
        void onUsernameCheckResult(String name, boolean available, String error);
        void onCanCreatePublic(boolean canCreate);
        void onInviteExported(TLRPC.TL_chatInviteExported invite, boolean isNew);
        void onDeactivateAllLinksSuccess();
    }

    private ChatEditTypeView view;
    private final ChatRepository repository;

    public ChatEditTypePresenter(int currentAccount) {
        this(new ChatRepositoryImpl(currentAccount));
    }

    public ChatEditTypePresenter(ChatRepository repository) {
        this.repository = repository;
    }

    public void attachView(ChatEditTypeView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public TLRPC.Chat getChat(long chatId) {
        return repository.getChat(chatId);
    }

    public TLRPC.Chat getChatSync(long chatId) {
        return repository.getChatSync(chatId);
    }

    public void putChat(TLRPC.Chat chat, boolean inCache) {
        repository.putChat(chat, inCache);
    }

    public TLRPC.ChatFull loadChatInfo(long chatId, boolean isChannel) {
        return repository.loadChatInfo(chatId, isChannel);
    }

    public void loadFullChat(long chatId, int classGuid) {
        repository.loadFullChat(chatId, classGuid);
    }

    public void checkPublicChannelsAdmin() {
        repository.checkPublicChannelsAdmin(new ChatRepository.LinkCallback() {
            @Override
            public void onSuccess(TLObject response) {
                if (view != null) {
                    view.onCanCreatePublic(true);
                }
            }

            @Override
            public void onError(TLRPC.TL_error error) {
                if (view != null) {
                    boolean canCreate = error == null || !error.text.equals("CHANNELS_ADMIN_PUBLIC_TOO_MUCH");
                    view.onCanCreatePublic(canCreate);
                }
            }
        });
    }

    public int loadAdminedPublicChannels() {
        return repository.loadAdminedPublicChannels(new ChatRepository.LinkCallback() {
            @Override
            public void onSuccess(TLObject response) {
                if (response instanceof TLRPC.TL_messages_chats && view != null) {
                    TLRPC.TL_messages_chats res = (TLRPC.TL_messages_chats) response;
                    view.onAdminedChannelsLoaded(res.chats);
                }
            }

            @Override
            public void onError(TLRPC.TL_error error) {
            }
        });
    }

    public int checkChannelUsername(long chatId, String username) {
        return repository.checkChannelUsername(chatId, username, new ChatRepository.LinkCallback() {
            @Override
            public void onSuccess(TLObject response) {
                if (view != null) {
                    view.onUsernameCheckResult(username, response instanceof TLRPC.TL_boolTrue, null);
                }
            }

            @Override
            public void onError(TLRPC.TL_error error) {
                if (view != null) {
                    view.onUsernameCheckResult(username, false, error != null ? error.text : null);
                }
            }
        });
    }

    public void cancelRequest(int reqId) {
        repository.cancelRequest(reqId);
    }

    public void toggleChatNoForwards(long chatId, boolean enabled) {
        repository.toggleChatNoForwards(chatId, enabled);
    }

    public void toggleChatJoinToSend(long chatId, boolean enabled, Runnable onComplete) {
        repository.toggleChatJoinToSend(chatId, enabled, onComplete);
    }

    public void toggleChatJoinRequest(long chatId, boolean enabled, Runnable onComplete) {
        repository.toggleChatJoinRequest(chatId, enabled, onComplete);
    }

    public void updateChannelUserName(BaseFragment fragment, long chatId, String username, Runnable onComplete, Runnable onError) {
        repository.updateChannelUserName(fragment, chatId, username, onComplete, onError);
    }

    public int exportPermanentChatInvite(long chatId, boolean isNew) {
        return repository.exportPermanentChatInvite(chatId, new ChatRepository.LinkCallback() {
            @Override
            public void onSuccess(TLObject response) {
                if (response instanceof TLRPC.TL_chatInviteExported && view != null) {
                    view.onInviteExported((TLRPC.TL_chatInviteExported) response, isNew);
                }
            }

            @Override
            public void onError(TLRPC.TL_error error) {
            }
        });
    }

    public int deactivateAllUsernames(long chatId) {
        return repository.deactivateAllUsernames(chatId, new ChatRepository.LinkCallback() {
            @Override
            public void onSuccess(TLObject response) {
                if (response instanceof TLRPC.TL_boolTrue && view != null) {
                    view.onDeactivateAllLinksSuccess();
                }
            }

            @Override
            public void onError(TLRPC.TL_error error) {
            }
        });
    }

    public void handleNotification(int id, long currentChatId, Object... args) {
        if (view == null) {
            return;
        }
        if (id == NotificationCenter.chatInfoDidLoad) {
            TLRPC.ChatFull chatFull = (TLRPC.ChatFull) args[0];
            if (chatFull != null && chatFull.id == currentChatId) {
                view.onChatInfoLoaded(chatFull);
            }
        }
    }
}
