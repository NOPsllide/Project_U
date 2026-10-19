package org.telegram.core.presenter;

import org.telegram.core.data.repository.ChatRepository;
import org.telegram.core.data.repository.ChatRepositoryImpl;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import java.util.ArrayList;

public class ChannelCreatePresenter {

    public interface ChannelCreateView {
        void onChatCreated(long chatId);
        void onChatCreateFailed();
        void onUsernameCheckResult(String name, boolean available, String error);
        void onAdminedChannelsLoaded(ArrayList<TLRPC.Chat> channels);
        void onCanCreatePublicChannels(boolean canCreate);
    }

    private ChannelCreateView view;
    private final ChatRepository repository;

    public ChannelCreatePresenter(int currentAccount) {
        this(new ChatRepositoryImpl(currentAccount));
    }

    public ChannelCreatePresenter(ChatRepository repository) {
        this.repository = repository;
    }

    public void attachView(ChannelCreateView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public int createChannel(String title, String description, BaseFragment fragment) {
        return repository.createChat(title, new ArrayList<>(), description, ChatObject.CHAT_TYPE_CHANNEL, false, null, null, fragment);
    }

    public void changeAvatar(long chatId, TLRPC.InputFile inputPhoto, TLRPC.InputFile inputVideo, double videoTimestamp, String inputVideoPath, TLRPC.FileLocation avatar, TLRPC.FileLocation avatarBig) {
        repository.changeChatAvatar(chatId, inputPhoto, inputVideo, videoTimestamp, inputVideoPath, avatar, avatarBig);
    }

    public int checkUsername(long chatId, String username) {
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

    public void updateChannelUserName(BaseFragment fragment, long chatId, String username) {
        repository.updateChannelUserName(fragment, chatId, username);
    }

    public void checkPublicChannelsAdmin() {
        repository.checkPublicChannelsAdmin(new ChatRepository.LinkCallback() {
            @Override
            public void onSuccess(TLObject response) {
                if (view != null) {
                    view.onCanCreatePublicChannels(true);
                }
            }

            @Override
            public void onError(TLRPC.TL_error error) {
                if (view != null) {
                    boolean canCreate = error == null || !error.text.equals("CHANNELS_ADMIN_PUBLIC_TOO_MUCH");
                    view.onCanCreatePublicChannels(canCreate);
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

    public void cancelRequest(int reqId) {
        repository.cancelRequest(reqId);
    }

    public void handleNotification(int id, Object... args) {
        if (view == null) {
            return;
        }
        if (id == NotificationCenter.chatDidCreated) {
            long chatId = (Long) args[0];
            view.onChatCreated(chatId);
        } else if (id == NotificationCenter.chatDidFailCreate) {
            view.onChatCreateFailed();
        }
    }
}
