package org.telegram.core.data.repository;

import android.content.SharedPreferences;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.tgnet.TLRPC;

public class ContactRepositoryImpl implements ContactRepository {

    private final int accountNum;

    public ContactRepositoryImpl(int accountNum) {
        this.accountNum = accountNum;
    }

    @Override
    public TLRPC.User getUser(long userId) {
        return MessagesController.getInstance(accountNum).getUser(userId);
    }

    @Override
    public void addContact(TLRPC.User user, boolean sharePhone) {
        ContactsController.getInstance(accountNum).addContact(user, sharePhone);
    }

    @Override
    public boolean needsAddException(long userId) {
        SharedPreferences prefs = MessagesController.getNotificationsSettings(accountNum);
        return prefs.getBoolean("dialog_bar_exception" + userId, false);
    }

    @Override
    public void markDialogBarAsProcessed(long userId) {
        SharedPreferences prefs = MessagesController.getNotificationsSettings(accountNum);
        prefs.edit().putInt("dialog_bar_vis3" + userId, 3).apply();
    }

    @Override
    public void notifyContactAdded(long userId) {
        NotificationCenter.getInstance(accountNum).postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_NAME);
        NotificationCenter.getInstance(accountNum).postNotificationName(NotificationCenter.peerSettingsDidLoad, userId);
    }
}
