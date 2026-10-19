package org.telegram.core.presenter;

import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.tgnet.TLRPC;
import java.util.ArrayList;

public class FilterUsersPresenter {

    public interface FilterUsersView {
        void onContactsLoaded();
        void onUpdateInterfaces(int mask);
        void onChatCreated();
    }

    private FilterUsersView view;
    private final int currentAccount;

    public FilterUsersPresenter(int currentAccount) {
        this.currentAccount = currentAccount;
    }

    public void attachView(FilterUsersView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public int getChatsLimit(boolean isPremium) {
        MessagesController controller = MessagesController.getInstance(currentAccount);
        return isPremium ? controller.dialogFiltersChatsLimitPremium : controller.dialogFiltersChatsLimitDefault;
    }

    public void putUser(TLRPC.User user, boolean inCache) {
        MessagesController.getInstance(currentAccount).putUser(user, inCache);
    }

    public void putChat(TLRPC.Chat chat, boolean inCache) {
        MessagesController.getInstance(currentAccount).putChat(chat, inCache);
    }

    public TLRPC.User getUser(long id) {
        return MessagesController.getInstance(currentAccount).getUser(id);
    }

    public TLRPC.Chat getChat(long id) {
        return MessagesController.getInstance(currentAccount).getChat(id);
    }

    public ArrayList<TLRPC.Dialog> getAllDialogs() {
        return MessagesController.getInstance(currentAccount).getAllDialogs();
    }

    public ArrayList<MessagesController.DialogFilter> getDialogFilters() {
        return MessagesController.getInstance(currentAccount).dialogFilters;
    }

    public void handleNotification(int id, Object... args) {
        if (view == null) {
            return;
        }
        if (id == NotificationCenter.contactsDidLoad) {
            view.onContactsLoaded();
        } else if (id == NotificationCenter.updateInterfaces) {
            int mask = (int) args[0];
            view.onUpdateInterfaces(mask);
        } else if (id == NotificationCenter.chatDidCreated) {
            view.onChatCreated();
        }
    }
}
