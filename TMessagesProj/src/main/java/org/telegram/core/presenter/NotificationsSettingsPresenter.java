package org.telegram.core.presenter;

import org.telegram.core.data.repository.NotificationsRepository;
import org.telegram.core.data.repository.NotificationsRepositoryImpl;
import org.telegram.ui.NotificationsSettingsActivity;

import java.util.ArrayList;

public class NotificationsSettingsPresenter {

    public interface NotificationsSettingsView {
        void showLoading();
        void hideLoading();
        void onExceptionsLoaded(ArrayList<NotificationsSettingsActivity.NotificationException> usersResult,
                                ArrayList<NotificationsSettingsActivity.NotificationException> chatsResult,
                                ArrayList<NotificationsSettingsActivity.NotificationException> channelsResult);
        void onSettingsReset();
    }

    private NotificationsSettingsView view;
    private final NotificationsRepository repository;
    private final int currentAccount;

    public NotificationsSettingsPresenter(int currentAccount) {
        this(currentAccount, new NotificationsRepositoryImpl(currentAccount));
    }

    public NotificationsSettingsPresenter(int currentAccount, NotificationsRepository repository) {
        this.currentAccount = currentAccount;
        this.repository = repository;
    }

    public void attachView(NotificationsSettingsView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void loadExceptions() {
        if (view != null) {
            view.showLoading();
        }
        repository.loadExceptions(currentAccount, (usersResult, chatsResult, channelsResult) -> {
            if (view != null) {
                view.hideLoading();
                view.onExceptionsLoaded(usersResult, chatsResult, channelsResult);
            }
        });
    }

    public void resetSettings() {
        if (view != null) {
            view.showLoading();
        }
        repository.resetNotificationsSettings(currentAccount, () -> {
            if (view != null) {
                view.hideLoading();
                view.onSettingsReset();
            }
        });
    }
}
