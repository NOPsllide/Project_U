package org.telegram.core.presenter;

import android.os.Bundle;
import org.telegram.core.data.repository.ContactRepository;
import org.telegram.core.data.repository.ContactRepositoryImpl;
import org.telegram.messenger.NotificationCenter;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import java.util.ArrayList;

public class ContactsPresenter {

    public interface ContactsView {
        void onContactsLoaded();
        void onUpdateInterfaces(int mask);
        void onEncryptedChatCreated(TLRPC.EncryptedChat encryptedChat);
        void onCloseChats();
    }

    private ContactsView view;
    private final ContactRepository repository;

    public ContactsPresenter(int currentAccount) {
        this(new ContactRepositoryImpl(currentAccount));
    }

    public ContactsPresenter(ContactRepository repository) {
        this.repository = repository;
    }

    public void attachView(ContactsView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void checkInviteText() {
        repository.checkInviteText();
    }

    public void reloadContactsStatusesMaybe() {
        repository.reloadContactsStatusesMaybe();
    }

    public void forceImportContacts() {
        repository.forceImportContacts();
    }

    public TLRPC.Chat getChat(long chatId) {
        return repository.getChat(chatId);
    }

    public boolean checkCanOpenChat(Bundle args, BaseFragment fragment) {
        return repository.checkCanOpenChat(args, fragment);
    }

    public void putUsers(ArrayList<TLRPC.User> users, boolean inCache) {
        repository.putUsers(users, inCache);
    }

    public String getInviteText(int count) {
        return repository.getInviteText(count);
    }

    public void setAskAboutContacts(boolean ask) {
        repository.setAskAboutContacts(ask);
    }

    public boolean getAskAboutContacts() {
        return repository.getAskAboutContacts();
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
        } else if (id == NotificationCenter.encryptedChatCreated) {
            view.onEncryptedChatCreated((TLRPC.EncryptedChat) args[0]);
        } else if (id == NotificationCenter.closeChats) {
            view.onCloseChats();
        }
    }
}
