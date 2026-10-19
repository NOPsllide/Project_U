package org.telegram.core.presenter;

import org.telegram.core.data.repository.NotificationsRepository;
import org.telegram.core.data.repository.NotificationsRepositoryImpl;
import org.telegram.messenger.NotificationCenter;
import org.telegram.ui.NotificationsSettingsActivity;
import java.util.ArrayList;

public class NotificationsCustomSettingsPresenter {

    public interface NotificationsCustomSettingsView {
        void onExceptionsLoaded(ArrayList<NotificationsSettingsActivity.NotificationException> usersResult,
                                ArrayList<NotificationsSettingsActivity.NotificationException> chatsResult,
                                ArrayList<NotificationsSettingsActivity.NotificationException> channelsResult);
        void onNotificationSettingsUpdated();
    }

    private NotificationsCustomSettingsView view;
    private final NotificationsRepository repository;
    private final int currentAccount;

    public NotificationsCustomSettingsPresenter(int currentAccount) {
        this(currentAccount, new NotificationsRepositoryImpl(currentAccount));
    }

    public NotificationsCustomSettingsPresenter(int currentAccount, NotificationsRepository repository) {
        this.currentAccount = currentAccount;
        this.repository = repository;
    }

    public void attachView(NotificationsCustomSettingsView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void loadExceptions() {
        repository.loadExceptions(currentAccount, (usersResult, chatsResult, channelsResult) -> {
            if (view != null) {
                view.onExceptionsLoaded(usersResult, chatsResult, channelsResult);
            }
        });
    }

    public boolean isGlobalNotificationsEnabled(int currentType) {
        return repository.isGlobalNotificationsEnabled(currentType);
    }

    public void setGlobalNotificationsEnabled(int currentType, int value) {
        repository.setGlobalNotificationsEnabled(currentType, value);
    }

    public void updateServerNotificationsSettings(int currentType) {
        repository.updateServerNotificationsSettings(currentType);
    }

    public void updateServerNotificationsSettings(long dialogId, int topicId, boolean resetSound) {
        repository.updateServerNotificationsSettings(dialogId, topicId, resetSound);
    }

    public void muteDialog(long did, int topicId, boolean mute) {
        repository.muteDialog(did, topicId, mute);
    }

    public void muteUntil(long did, int topicId, int timeInSeconds) {
        repository.muteUntil(did, topicId, timeInSeconds);
    }

    public void deleteAllExceptions(int currentType, ArrayList<NotificationsSettingsActivity.NotificationException> exceptions, int topicId) {
        repository.deleteAllExceptions(currentType, exceptions, topicId);
    }

    public void handleNotification(int id, Object... args) {
        if (view == null) {
            return;
        }
        if (id == NotificationCenter.notificationsSettingsUpdated) {
            view.onNotificationSettingsUpdated();
        }
    }
}
