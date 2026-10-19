package org.telegram.core.data.repository;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
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

    @Override
    public ArrayList<ContactsController.Contact> getSortedPhoneBookContacts() {
        ArrayList<ContactsController.Contact> contacts = new ArrayList<>(ContactsController.getInstance(accountNum).phoneBookContacts);
        java.util.Collections.sort(contacts, (o1, o2) -> {
            if (o1.imported > o2.imported) {
                return -1;
            } else if (o1.imported < o2.imported) {
                return 1;
            }
            return 0;
        });
        return contacts;
    }

    @Override
    public void checkForceImportContacts() {
        if (!UserConfig.getInstance(accountNum).contactsReimported) {
            ContactsController.getInstance(accountNum).forceImportContacts();
            UserConfig.getInstance(accountNum).contactsReimported = true;
            UserConfig.getInstance(accountNum).saveConfig(false);
        }
    }

    @Override
    public Intent createInviteSmsIntent(ArrayList<ContactsController.Contact> contacts) {
        StringBuilder builder = new StringBuilder();
        int num = 0;
        for (int a = 0; a < contacts.size(); a++) {
            ContactsController.Contact contact = contacts.get(a);
            if (builder.length() != 0) {
                builder.append(';');
            }
            if (contact.phones != null && !contact.phones.isEmpty()) {
                builder.append(contact.phones.get(0));
            }
            if (a == 0 && contacts.size() == 1) {
                num = contact.imported;
            }
        }
        Intent intent = new Intent(Intent.ACTION_SENDTO, android.net.Uri.parse("smsto:" + builder.toString()));
        intent.putExtra("sms_body", ContactsController.getInstance(accountNum).getInviteText(num));
        return intent;
    }

    @Override
    public void checkInviteText() {
        ContactsController.getInstance(accountNum).checkInviteText();
    }

    @Override
    public void reloadContactsStatusesMaybe() {
        ContactsController.getInstance(accountNum).reloadContactsStatusesMaybe();
    }

    @Override
    public void forceImportContacts() {
        ContactsController.getInstance(accountNum).forceImportContacts();
    }

    @Override
    public TLRPC.Chat getChat(long chatId) {
        return MessagesController.getInstance(accountNum).getChat(chatId);
    }

    @Override
    public boolean checkCanOpenChat(Bundle args, BaseFragment fragment) {
        return MessagesController.getInstance(accountNum).checkCanOpenChat(args, fragment);
    }

    @Override
    public void setAskAboutContacts(boolean ask) {
        MessagesController.getGlobalNotificationsSettings().edit().putBoolean("askAboutContacts", ask).commit();
    }

    @Override
    public boolean getAskAboutContacts() {
        return MessagesController.getGlobalNotificationsSettings().getBoolean("askAboutContacts", true);
    }
}
