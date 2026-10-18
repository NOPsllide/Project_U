package org.telegram.core.presenter;

import org.telegram.core.data.repository.ChatRepository;
import org.telegram.core.data.repository.ChatRepositoryImpl;
import org.telegram.messenger.ChatObject;

public class MemberRequestsPresenter {

    public interface MemberRequestsView {
        void setSearchFieldText(String text);
    }

    private final int currentAccount;
    private final long chatId;
    private final ChatRepository chatRepository;
    private MemberRequestsView view;

    public MemberRequestsPresenter(int currentAccount, long chatId) {
        this(currentAccount, chatId, new ChatRepositoryImpl(currentAccount));
    }

    public MemberRequestsPresenter(int currentAccount, long chatId, ChatRepository chatRepository) {
        this.currentAccount = currentAccount;
        this.chatId = chatId;
        this.chatRepository = chatRepository;
    }

    public void attachView(MemberRequestsView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public long getChatId() {
        return chatId;
    }

    public boolean isChannel() {
        return ChatObject.isChannelAndNotMegaGroup(chatId, currentAccount);
    }

    public void onImportersChanged(boolean fromHide) {
        if (fromHide && view != null) {
            view.setSearchFieldText("");
        }
    }
}
