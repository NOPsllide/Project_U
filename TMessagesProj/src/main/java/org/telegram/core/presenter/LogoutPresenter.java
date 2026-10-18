package org.telegram.core.presenter;

import org.telegram.core.data.repository.AuthRepository;

public class LogoutPresenter {

    public interface View {
        void openLoginActivity(int account);
        void showAccountLimitBottomSheet();
        void openPasscodeActivity();
        void openCacheControlActivity();
        void openChangePhoneNumber();
        void showSupportAlert();
        void showLogoutConfirmationDialog();
    }

    private final AuthRepository authRepository;
    private View view;

    public LogoutPresenter(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void attachView(View view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public boolean canAddAccount() {
        return authRepository.canAddAccount();
    }

    public boolean hasPasscode() {
        return authRepository.hasPasscode();
    }

    public void onAddAccountClicked() {
        if (view == null) return;
        Integer availableAccount = authRepository.getAvailableAccountForLogin();
        if (availableAccount != null) {
            view.openLoginActivity(availableAccount);
        } else if (!authRepository.hasPremiumOnAccounts()) {
            view.showAccountLimitBottomSheet();
        }
    }

    public void onPasscodeClicked() {
        if (view != null) {
            view.openPasscodeActivity();
        }
    }

    public void onCacheClicked() {
        if (view != null) {
            view.openCacheControlActivity();
        }
    }

    public void onPhoneClicked() {
        if (view != null) {
            view.openChangePhoneNumber();
        }
    }

    public void onSupportClicked() {
        if (view != null) {
            view.showSupportAlert();
        }
    }

    public void onLogoutClicked() {
        if (view != null) {
            view.showLogoutConfirmationDialog();
        }
    }

    public void confirmLogout() {
        authRepository.performLogout();
    }

    public void onDialogDismissed() {
        authRepository.checkAutodownloadSettings();
    }
}
