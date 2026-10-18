package org.telegram.core.presenter;

import org.telegram.core.data.repository.UserRepository;
import org.telegram.tgnet.TLRPC;

public class ChangeBioPresenter {

    public interface View {
        void showCurrentBio(String bio);
        void showLoading();
        void hideLoading();
        void showError(int accountNum, TLRPC.TL_error error, TLRPC.TL_account_updateProfile req);
        void closeScreen();
    }

    private final UserRepository userRepository;
    private final int accountNum;
    private View view;
    private int activeRequestId = -1;

    public ChangeBioPresenter(UserRepository userRepository, int accountNum) {
        this.userRepository = userRepository;
        this.accountNum = accountNum;
    }

    public void attachView(View view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public int getAboutLimit() {
        return userRepository.getAboutLimit();
    }

    public void loadCurrentBio() {
        if (view == null) return;
        String bio = userRepository.getCurrentBio();
        view.showCurrentBio(bio);
    }

    public void saveBio(String newBio) {
        if (view == null) return;

        String currentBio = userRepository.getCurrentBio();
        String cleanBio = newBio.replace("\n", "");

        if (currentBio.equals(cleanBio)) {
            view.closeScreen();
            return;
        }

        view.showLoading();

        activeRequestId = userRepository.updateBio(cleanBio, new UserRepository.Callback<TLRPC.User>() {
            @Override
            public void onSuccess(TLRPC.User result) {
                if (view == null) return;
                view.hideLoading();
                view.closeScreen();
            }

            @Override
            public void onError(TLRPC.TL_error error) {
                if (view == null) return;
                view.hideLoading();
                TLRPC.TL_account_updateProfile req = new TLRPC.TL_account_updateProfile();
                req.about = cleanBio;
                req.flags |= 4;
                view.showError(accountNum, error, req);
            }
        });
    }

    public void cancelSave() {
        if (activeRequestId != -1) {
            userRepository.cancelRequest(activeRequestId);
            activeRequestId = -1;
        }
    }
}
