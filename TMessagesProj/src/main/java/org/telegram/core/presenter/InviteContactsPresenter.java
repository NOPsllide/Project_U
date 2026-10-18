package org.telegram.core.presenter;

import android.app.Activity;
import android.content.Intent;

import org.telegram.core.data.repository.ContactRepository;
import org.telegram.core.data.repository.ContactRepositoryImpl;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;

import java.util.ArrayList;

public class InviteContactsPresenter {

    public interface InviteContactsView {
        void showContacts(ArrayList<ContactsController.Contact> contacts);
        void closeScreen();
    }

    private InviteContactsView view;
    private final ContactRepository repository;

    public InviteContactsPresenter(int currentAccount) {
        this(new ContactRepositoryImpl(currentAccount));
    }

    public InviteContactsPresenter(ContactRepository repository) {
        this.repository = repository;
    }

    public void attachView(InviteContactsView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void onInit() {
        repository.checkForceImportContacts();
        loadContacts();
    }

    public void loadContacts() {
        ArrayList<ContactsController.Contact> contacts = repository.getSortedPhoneBookContacts();
        if (view != null) {
            view.showContacts(contacts);
        }
    }

    public void sendInvites(Activity activity, ArrayList<ContactsController.Contact> contacts) {
        if (activity != null && contacts != null && !contacts.isEmpty()) {
            try {
                Intent intent = repository.createInviteSmsIntent(contacts);
                activity.startActivityForResult(intent, 500);
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
        if (view != null) {
            view.closeScreen();
        }
    }
}
