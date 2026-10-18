package org.telegram.core.presenter;

import android.text.TextUtils;

import org.telegram.core.data.repository.ContactRepository;
import org.telegram.core.data.repository.ContactRepositoryImpl;
import org.telegram.tgnet.TLRPC;

public class NewContactPresenter {

    public interface NewContactView {
        void shakeFirstName();
        void shakeCode();
        void shakePhone();
        void showEditDoneProgress(boolean show, boolean animated);
        void openChatWithUser(TLRPC.User user);
        void showNotRegisteredDialog(String firstName, String lastName, String phone, String inviteText);
        void showError(TLRPC.TL_error error, TLRPC.TL_contacts_importContacts req);
    }

    private final ContactRepository contactRepository;
    private NewContactView view;
    private boolean donePressed;

    public NewContactPresenter(int currentAccount) {
        this(new ContactRepositoryImpl(currentAccount));
    }

    public NewContactPresenter(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    public void attachView(NewContactView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public boolean isDonePressed() {
        return donePressed;
    }

    public TLRPC.User getCurrentUser() {
        return contactRepository.getCurrentUser();
    }

    public void addContact(String firstName, String lastName, String code, String phone) {
        if (donePressed || view == null) {
            return;
        }
        if (TextUtils.isEmpty(firstName)) {
            view.shakeFirstName();
            return;
        }
        if (TextUtils.isEmpty(code)) {
            view.shakeCode();
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            view.shakePhone();
            return;
        }

        donePressed = true;
        view.showEditDoneProgress(true, true);
        String fullPhone = "+" + code + phone;

        contactRepository.importContact(firstName, lastName, fullPhone, new ContactRepository.ImportContactCallback() {
            @Override
            public void onSuccess(TLRPC.TL_contacts_importedContacts res) {
                donePressed = false;
                if (view == null) {
                    return;
                }
                if (!res.users.isEmpty()) {
                    contactRepository.putUsers(res.users, false);
                    view.openChatWithUser(res.users.get(0));
                } else {
                    view.showEditDoneProgress(false, true);
                    String inviteText = contactRepository.getInviteText(1);
                    view.showNotRegisteredDialog(firstName, lastName, fullPhone, inviteText);
                }
            }

            @Override
            public void onError(TLRPC.TL_error error, TLRPC.TL_contacts_importContacts req) {
                donePressed = false;
                if (view != null) {
                    view.showEditDoneProgress(false, true);
                    view.showError(error, req);
                }
            }
        });
    }
}
