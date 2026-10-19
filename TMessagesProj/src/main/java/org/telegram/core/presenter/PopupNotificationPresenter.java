package org.telegram.core.presenter;

import org.telegram.core.data.repository.NotificationsRepository;
import org.telegram.core.data.repository.NotificationsRepositoryImpl;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.tgnet.TLRPC;
import java.util.ArrayList;

public class PopupNotificationPresenter {

    public interface PopupNotificationView {
        void onPushMessagesUpdated();
        void onAppDidLogout(int account);
        void onUpdateInterfaces(int account, int updateMask);
        void onMessagePlayingDidReset(int account, int mid);
        void onMessagePlayingProgressDidChanged(int account, int mid);
        void onEmojiLoaded();
        void onContactsDidLoad(int account);
    }

    private PopupNotificationView view;
    private final NotificationsRepository repository;

    public PopupNotificationPresenter(int currentAccount) {
        this(new NotificationsRepositoryImpl(currentAccount));
    }

    public PopupNotificationPresenter(NotificationsRepository repository) {
        this.repository = repository;
    }

    public void attachView(PopupNotificationView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public ArrayList<MessageObject> loadPopupMessages(boolean isReply, int forceAccount) {
        return repository.getPopupMessages(isReply, forceAccount);
    }

    public void markDialogAsRead(int account, long dialogId, int id, int date) {
        repository.markDialogAsRead(account, dialogId, id, date);
    }

    public void sendTyping(int account, long dialogId, int classGuid) {
        repository.sendTyping(account, dialogId, classGuid);
    }

    public void sendNotificationCallback(int account, long dialogId, int messageId, byte[] data) {
        repository.sendNotificationCallback(account, dialogId, messageId, data);
    }

    public TLRPC.User getUser(int account, long userId) {
        return repository.getUser(account, userId);
    }

    public TLRPC.Chat getChat(int account, long chatId) {
        return repository.getChat(account, chatId);
    }

    public TLRPC.EncryptedChat getEncryptedChat(int account, int chatId) {
        return repository.getEncryptedChat(account, chatId);
    }

    public CharSequence getPrintingString(int account, long dialogId) {
        return repository.getPrintingString(account, dialogId);
    }

    public Integer getPrintingStringType(int account, long dialogId) {
        return repository.getPrintingStringType(account, dialogId);
    }

    public void setAppPaused(int account, boolean paused) {
        repository.setAppPaused(account, paused);
    }

    public void handleNotification(int id, int account, Object... args) {
        if (view == null) {
            return;
        }
        if (id == NotificationCenter.appDidLogout) {
            view.onAppDidLogout(account);
        } else if (id == NotificationCenter.pushMessagesUpdated) {
            view.onPushMessagesUpdated();
        } else if (id == NotificationCenter.updateInterfaces) {
            int updateMask = (Integer) args[0];
            view.onUpdateInterfaces(account, updateMask);
        } else if (id == NotificationCenter.messagePlayingDidReset) {
            Integer mid = (Integer) args[0];
            view.onMessagePlayingDidReset(account, mid);
        } else if (id == NotificationCenter.messagePlayingProgressDidChanged) {
            Integer mid = (Integer) args[0];
            view.onMessagePlayingProgressDidChanged(account, mid);
        } else if (id == NotificationCenter.emojiLoaded) {
            view.onEmojiLoaded();
        } else if (id == NotificationCenter.contactsDidLoad) {
            view.onContactsDidLoad(account);
        }
    }
}
