package org.telegram.core.data.repository;

public interface PasscodeRepository {

    boolean hasPasscode();

    int getPasscodeType();

    int getAutoLockIn();

    void setAutoLockIn(int seconds);

    boolean isUseFingerprint();

    void setUseFingerprint(boolean use);

    boolean isAllowScreenCapture();

    void setAllowScreenCapture(boolean allow);

    boolean checkPasscode(String passcode);

    void increaseBadPasscodeTries();

    void resetBadPasscodeTries();

    long getPasscodeRetryInMs();

    boolean setPasscode(String passcode, int passcodeType);

    void clearPasscode();

    void postPasscodeNotification(boolean reload);
}
