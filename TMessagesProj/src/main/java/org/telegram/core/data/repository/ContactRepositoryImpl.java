package org.telegram.core.data.repository;

import android.content.SharedPreferences;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import java.util.ArrayList;

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

    @Override
    public int importContact(String firstName, String lastName, String phone, ImportContactCallback callback) {
        final TLRPC.TL_contacts_importContacts req = new TLRPC.TL_contacts_importContacts();
        final TLRPC.TL_inputPhoneContact inputPhoneContact = new TLRPC.TL_inputPhoneContact();
        inputPhoneContact.first_name = firstName;
        inputPhoneContact.last_name = lastName;
        inputPhoneContact.phone = phone;
        req.contacts.add(inputPhoneContact);
        return ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> {
            final TLRPC.TL_contacts_importedContacts res = (TLRPC.TL_contacts_importedContacts) response;
            AndroidUtilities.runOnUIThread(() -> {
                if (callback != null) {
                    if (res != null) {
                        callback.onSuccess(res);
                    } else {
                        callback.onError(error, req);
                    }
                }
            });
        }, ConnectionsManager.RequestFlagFailOnServerErrors);
    }

    @Override
    public void putUsers(ArrayList<TLRPC.User> users, boolean inCache) {
        MessagesController.getInstance(accountNum).putUsers(users, inCache);
    }

    @Override
    public String getInviteText(int contactsCount) {
        return ContactsController.getInstance(accountNum).getInviteText(contactsCount);
    }

    @Override
    public TLRPC.User getCurrentUser() {
        return UserConfig.getInstance(accountNum).getCurrentUser();
    }
}
