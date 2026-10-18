package org.telegram.core.data.repository;

import org.telegram.messenger.DownloadController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;

public class AuthRepositoryImpl implements AuthRepository {

    private final int accountNum;

    public AuthRepositoryImpl(int accountNum) {
        this.accountNum = accountNum;
    }

    @Override
    public boolean canAddAccount() {
        return UserConfig.getActivatedAccountsCount() < UserConfig.MAX_ACCOUNT_COUNT;
    }

    @Override
    public boolean hasPasscode() {
        return SharedConfig.passcodeHash.length() > 0;
    }

    @Override
    public Integer getAvailableAccountForLogin() {
        int freeAccounts = 0;
        Integer availableAccount = null;
        for (int a = UserConfig.MAX_ACCOUNT_COUNT - 1; a >= 0; a--) {
            if (!UserConfig.getInstance(a).isClientActivated()) {
                freeAccounts++;
                if (availableAccount == null) {
                    availableAccount = a;
                }
            }
        }
        if (!UserConfig.hasPremiumOnAccounts()) {
            freeAccounts -= (UserConfig.MAX_ACCOUNT_COUNT - UserConfig.MAX_ACCOUNT_DEFAULT_COUNT);
        }
        if (freeAccounts > 0) {
            return availableAccount;
        }
        return null;
    }

    @Override
    public boolean hasPremiumOnAccounts() {
        return UserConfig.hasPremiumOnAccounts();
    }

    @Override
    public void performLogout() {
        MessagesController.getInstance(accountNum).performLogout(1);
    }

    @Override
    public void checkAutodownloadSettings() {
        DownloadController.getInstance(accountNum).checkAutodownloadSettings();
    }
}
