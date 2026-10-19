package org.telegram.core.presenter;

import org.telegram.core.data.repository.ChatRepository;
import org.telegram.core.data.repository.ChatRepositoryImpl;
import org.telegram.core.data.repository.UserRepository;
import org.telegram.core.data.repository.UserRepositoryImpl;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.TLRPC;

public class QrPresenter {

    public interface QrActivityView {
        void onQrDataLoaded(String link, String title, TLRPC.User user, TLRPC.Chat chat);
    }

    private QrActivityView view;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final int currentAccount;

    public QrPresenter(int currentAccount) {
        this(currentAccount, new UserRepositoryImpl(currentAccount), new ChatRepositoryImpl(currentAccount));
    }

    public QrPresenter(int currentAccount, UserRepository userRepository, ChatRepository chatRepository) {
        this.currentAccount = currentAccount;
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
    }

    public void attachView(QrActivityView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public TLRRPC_Data loadQrData(long userId, long chatId, String customLink) {
        String link = customLink;
        String title = null;
        TLRPC.User user = null;
        TLRPC.Chat chat = null;

        if (userId != 0) {
            user = userRepository.getCurrentUser();
            if (user == null || user.id != userId) {
                user = chatRepository.getUser(userId);
            }
            if (user != null) {
                title = user.first_name;
                if (link == null && user.username != null) {
                    link = "https://" + MessagesController.getInstance(currentAccount).linkPrefix + "/" + user.username;
                }
            }
        } else if (chatId != 0) {
            chat = chatRepository.getChat(chatId);
            if (chat != null) {
                title = chat.title;
                if (link == null) {
                    String username = ChatObject.getPublicUsername(chat);
                    if (username != null) {
                        link = "https://" + MessagesController.getInstance(currentAccount).linkPrefix + "/" + username;
                    }
                }
            }
        }

        if (view != null) {
            view.onQrDataLoaded(link, title, user, chat);
        }

        return new TLRRPC_Data(link, title, user, chat);
    }

    public static class TLRRPC_Data {
        public final String link;
        public final String title;
        public final TLRPC.User user;
        public final TLRPC.Chat chat;

        public TLRRPC_Data(String link, String title, TLRPC.User user, TLRPC.Chat chat) {
            this.link = link;
            this.title = title;
            this.user = user;
            this.chat = chat;
        }
    }
}
