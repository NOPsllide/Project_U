package org.telegram.core.presenter;

import android.app.Activity;

import org.telegram.core.data.repository.WebviewRepository;
import org.telegram.core.data.repository.WebviewRepositoryImpl;
import org.telegram.messenger.MessageObject;

public class WebviewPresenter {

    public interface WebviewView {
        void onStatsUrlLoaded(String url);
    }

    private final int currentAccount;
    private final WebviewRepository repository;
    private WebviewView view;

    public WebviewPresenter(int currentAccount) {
        this(currentAccount, new WebviewRepositoryImpl(currentAccount));
    }

    public WebviewPresenter(int currentAccount, WebviewRepository repository) {
        this.currentAccount = currentAccount;
        this.repository = repository;
    }

    public void attachView(WebviewView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public String getGameShareLink(String botName, String startParam) {
        return repository.getGameShareLink(botName, startParam);
    }

    public void sendGameTyping(long dialogId) {
        repository.sendGameTyping(dialogId);
    }

    public void reloadStats(long dialogId, String params, boolean isDark) {
        repository.loadStatsUrl(dialogId, params, isDark, url -> {
            if (view != null) {
                view.onStatsUrlLoaded(url);
            }
        });
    }

    public void openGameInBrowser(String urlStr, MessageObject messageObject, Activity parentActivity, String short_name, String username) {
        repository.openGameInBrowser(urlStr, messageObject, parentActivity, short_name, username);
    }
}
