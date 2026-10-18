package org.telegram.core.data.repository;

public interface AuthRepository {

    boolean canAddAccount();

    boolean hasPasscode();

    Integer getAvailableAccountForLogin();

    boolean hasPremiumOnAccounts();

    void performLogout();

    void checkAutodownloadSettings();
}
