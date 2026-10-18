package org.telegram.core.data.repository;

import android.content.Intent;
import org.telegram.messenger.ContactsController;
import org.telegram.tgnet.TLRPC;
import java.util.ArrayList;

public interface ContactRepository {

    TLRPC.User getUser(long userId);

    void addContact(TLRPC.User user, boolean sharePhone);

    boolean needsAddException(long userId);

    void markDialogBarAsProcessed(long userId);

    void notifyContactAdded(long userId);

    int importContact(String firstName, String lastName, String phone, ImportContactCallback callback);

    void putUsers(ArrayList<TLRPC.User> users, boolean inCache);

    String getInviteText(int contactsCount);

    TLRPC.User getCurrentUser();

    ArrayList<ContactsController.Contact> getSortedPhoneBookContacts();

    void checkForceImportContacts();

    Intent createInviteSmsIntent(ArrayList<ContactsController.Contact> contacts);

    interface ImportContactCallback {
        void onSuccess(TLRPC.TL_contacts_importedContacts res);
        void onError(TLRPC.TL_error error, TLRPC.TL_contacts_importContacts req);
    }
}
