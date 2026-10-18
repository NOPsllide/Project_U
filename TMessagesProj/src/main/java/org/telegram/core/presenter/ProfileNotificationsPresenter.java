package org.telegram.core.presenter;

import org.telegram.core.data.repository.NotificationsRepository;
import org.telegram.core.data.repository.NotificationsRepositoryImpl;

public class ProfileNotificationsPresenter {

    public interface ProfileNotificationsView {
        void onSettingsLoaded(boolean notificationsEnabled);
        void onSettingsSaved();
        void onCustomSettingsReset();
        void onRingtoneSaved(int requestCode);
    }

    private ProfileNotificationsView view;
    private final NotificationsRepository repository;

    public ProfileNotificationsPresenter(int currentAccount) {
        this(new NotificationsRepositoryImpl(currentAccount));
    }

    public ProfileNotificationsPresenter(NotificationsRepository repository) {
        this.repository = repository;
    }

    public void attachView(ProfileNotificationsView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void loadSettings(long dialogId, int topicId, boolean addingException) {
        boolean enabled = repository.isNotificationsEnabled(dialogId, topicId, addingException);
        if (view != null) {
            view.onSettingsLoaded(enabled);
        }
    }

    public void saveSettings(long dialogId, int topicId, boolean enabled, boolean addingException) {
        repository.saveDialogNotifications(dialogId, topicId, enabled, addingException, () -> {
            if (view != null) {
                view.onSettingsSaved();
            }
        });
    }

    public void resetCustomSettings(long dialogId, int topicId) {
        repository.resetCustomNotifications(dialogId, topicId, () -> {
            if (view != null) {
                view.onCustomSettingsReset();
            }
        });
    }

    public void saveRingtoneOrSound(long dialogId, int topicId, int requestCode, String name, String ringtonePath) {
        repository.saveRingtoneOrSound(dialogId, topicId, requestCode, name, ringtonePath);
        if (view != null) {
            view.onRingtoneSaved(requestCode);
        }
    }

    public void handleBackPressed(long dialogId, int topicId, boolean addingException, boolean notificationsEnabled) {
        if (!addingException && notificationsEnabled) {
            repository.resetNotify2Key(dialogId, topicId);
        }
    }

    public void handleFragmentDestroy(long dialogId, int topicId, boolean needReset) {
        if (!needReset) {
            repository.applyCustomKey(dialogId, topicId);
        }
    }
}
