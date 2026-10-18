package org.telegram.core.presenter;

import org.telegram.core.data.repository.PrivacyRepository;
import org.telegram.core.data.repository.PrivacyRepositoryImpl;
import org.telegram.messenger.DialogObject;
import org.telegram.tgnet.TLRPC;

public class DialogOrContactPickerPresenter {

    public interface DialogOrContactPickerView {
        void showBlockConfirmation(TLRPC.User user);
        void showBlockError();
        void showBlockSuccess();
        void finishScreen();
    }

    private final PrivacyRepository privacyRepository;
    private DialogOrContactPickerView view;

    public DialogOrContactPickerPresenter(int currentAccount) {
        this(new PrivacyRepositoryImpl(currentAccount));
    }

    public DialogOrContactPickerPresenter(PrivacyRepository privacyRepository) {
        this.privacyRepository = privacyRepository;
    }

    public void attachView(DialogOrContactPickerView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void onDialogSelected(long did) {
        if (!DialogObject.isUserDialog(did)) {
            return;
        }
        TLRPC.User user = privacyRepository.getUser(did);
        if (user != null && view != null) {
            view.showBlockConfirmation(user);
        }
    }

    public void onContactSelected(TLRPC.User user) {
        if (user != null && view != null) {
            view.showBlockConfirmation(user);
        }
    }

    public void confirmBlockUser(TLRPC.User user) {
        if (user == null || view == null) {
            return;
        }
        if (privacyRepository.isSupportUser(user)) {
            view.showBlockError();
        } else {
            privacyRepository.blockPeer(user.id);
            view.showBlockSuccess();
            view.finishScreen();
        }
    }
}
