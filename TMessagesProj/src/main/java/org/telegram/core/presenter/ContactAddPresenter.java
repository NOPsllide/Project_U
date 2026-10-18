package org.telegram.core.presenter;

import android.text.TextUtils;
import org.telegram.core.data.repository.ContactRepository;
import org.telegram.tgnet.TLRPC;

public class ContactAddPresenter {

    public interface View {
        void closeScreen();
        void notifyContactAdded();
    }

    private final ContactRepository contactRepository;
    private View view;
    private final long userId;
    private final boolean addContact;

    public ContactAddPresenter(ContactRepository contactRepository, long userId, boolean addContact) {
        this.contactRepository = contactRepository;
        this.userId = userId;
        this.addContact = addContact;
    }

    public void attachView(View view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public TLRPC.User getUser() {
        return contactRepository.getUser(userId);
    }

    public boolean isAddContactMode() {
        return addContact;
    }

    public boolean needsAddException() {
        return contactRepository.needsAddException(userId);
    }

    public void saveContact(String firstName, String lastName, boolean sharePhone) {
        if (view == null || TextUtils.isEmpty(firstName)) {
            return;
        }

        TLRPC.User user = contactRepository.getUser(userId);
        if (user == null) {
            return;
        }

        user.first_name = firstName;
        user.last_name = lastName;

        contactRepository.addContact(user, sharePhone);
        contactRepository.markDialogBarAsProcessed(userId);
        contactRepository.notifyContactAdded(userId);

        view.closeScreen();
        view.notifyContactAdded();
    }
}
