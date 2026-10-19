package org.telegram.core.data.repository;

import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.Utilities;

public class PasscodeRepositoryImpl implements PasscodeRepository {

    @Override
    public boolean hasPasscode() {
        return SharedConfig.passcodeHash.length() != 0;
    }

    @Override
    public int getPasscodeType() {
        return SharedConfig.passcodeType;
    }

    @Override
    public int getAutoLockIn() {
        return SharedConfig.autoLockIn;
    }

    @Override
    public void setAutoLockIn(int seconds) {
        SharedConfig.autoLockIn = seconds;
        SharedConfig.saveConfig();
    }

    @Override
    public boolean isUseFingerprint() {
        return SharedConfig.useFingerprint;
    }

    @Override
    public void setUseFingerprint(boolean use) {
        SharedConfig.useFingerprint = use;
        SharedConfig.saveConfig();
    }

    @Override
    public boolean isAllowScreenCapture() {
        return SharedConfig.allowScreenCapture;
    }

    @Override
    public void setAllowScreenCapture(boolean allow) {
        SharedConfig.allowScreenCapture = allow;
        SharedConfig.saveConfig();
    }

    @Override
    public boolean checkPasscode(String passcode) {
        return SharedConfig.checkPasscode(passcode);
    }

    @Override
    public void increaseBadPasscodeTries() {
        SharedConfig.increaseBadPasscodeTries();
    }

    @Override
    public void resetBadPasscodeTries() {
        SharedConfig.badPasscodeTries = 0;
        SharedConfig.saveConfig();
    }

    @Override
    public long getPasscodeRetryInMs() {
        return SharedConfig.passcodeRetryInMs;
    }

    @Override
    public boolean setPasscode(String passcode, int passcodeType) {
        try {
            SharedConfig.passcodeSalt = new byte[16];
            Utilities.random.nextBytes(SharedConfig.passcodeSalt);
            byte[] passcodeBytes = passcode.getBytes("UTF-8");
            byte[] bytes = new byte[32 + passcodeBytes.length];
            System.arraycopy(SharedConfig.passcodeSalt, 0, bytes, 0, 16);
            System.arraycopy(passcodeBytes, 0, bytes, 16, passcodeBytes.length);
            System.arraycopy(SharedConfig.passcodeSalt, 0, bytes, passcodeBytes.length + 16, 16);
            SharedConfig.passcodeHash = Utilities.bytesToHex(Utilities.computeSHA256(bytes, 0, bytes.length));
            SharedConfig.allowScreenCapture = true;
            SharedConfig.passcodeType = passcodeType;
            SharedConfig.saveConfig();
            return true;
        } catch (Exception ignore) {
            return false;
        }
    }

    @Override
    public void clearPasscode() {
        SharedConfig.passcodeHash = "";
        SharedConfig.appLocked = false;
        SharedConfig.saveConfig();
    }

    @Override
    public void postPasscodeNotification(boolean reload) {
        if (reload) {
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didSetPasscode);
        } else {
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didSetPasscode, false);
        }
    }
}
