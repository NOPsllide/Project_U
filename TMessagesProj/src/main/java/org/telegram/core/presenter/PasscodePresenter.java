package org.telegram.core.presenter;

import org.telegram.core.data.repository.PasscodeRepository;
import org.telegram.core.data.repository.PasscodeRepositoryImpl;
import org.telegram.messenger.NotificationCenter;

public class PasscodePresenter {

    public interface PasscodeView {
        void onPasscodeChanged();
    }

    private PasscodeView view;
    private final PasscodeRepository repository;

    public PasscodePresenter() {
        this(new PasscodeRepositoryImpl());
    }

    public PasscodePresenter(PasscodeRepository repository) {
        this.repository = repository;
    }

    public void attachView(PasscodeView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public boolean hasPasscode() {
        return repository.hasPasscode();
    }

    public int getPasscodeType() {
        return repository.getPasscodeType();
    }

    public int getAutoLockIn() {
        return repository.getAutoLockIn();
    }

    public void setAutoLockIn(int seconds) {
        repository.setAutoLockIn(seconds);
    }

    public boolean isUseFingerprint() {
        return repository.isUseFingerprint();
    }

    public void setUseFingerprint(boolean use) {
        repository.setUseFingerprint(use);
    }

    public boolean isAllowScreenCapture() {
        return repository.isAllowScreenCapture();
    }

    public void setAllowScreenCapture(boolean allow) {
        repository.setAllowScreenCapture(allow);
    }

    public boolean checkPasscode(String passcode) {
        return repository.checkPasscode(passcode);
    }

    public void increaseBadPasscodeTries() {
        repository.increaseBadPasscodeTries();
    }

    public void resetBadPasscodeTries() {
        repository.resetBadPasscodeTries();
    }

    public long getPasscodeRetryInMs() {
        return repository.getPasscodeRetryInMs();
    }

    public boolean setPasscode(String passcode, int passcodeType) {
        return repository.setPasscode(passcode, passcodeType);
    }

    public void clearPasscode() {
        repository.clearPasscode();
    }

    public void postPasscodeNotification(boolean reload) {
        repository.postPasscodeNotification(reload);
    }

    public void handleNotification(int id, Object... args) {
        if (view == null) {
            return;
        }
        if (id == NotificationCenter.didSetPasscode && (args.length == 0 || (Boolean) args[0])) {
            view.onPasscodeChanged();
        }
    }
}
