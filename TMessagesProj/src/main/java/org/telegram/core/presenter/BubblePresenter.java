package org.telegram.core.presenter;

import android.content.Intent;
import android.os.Bundle;

import org.telegram.core.data.repository.BubbleRepository;
import org.telegram.core.data.repository.BubbleRepositoryImpl;
import org.telegram.messenger.UserConfig;

public class BubblePresenter {

    public static class BubbleChatIntentInfo {
        public final int account;
        public final long dialogId;
        public final Bundle args;

        public BubbleChatIntentInfo(int account, long dialogId, Bundle args) {
            this.account = account;
            this.dialogId = dialogId;
            this.args = args;
        }
    }

    public interface BubbleView {
        void showPasscode();
        void openChat(BubbleChatIntentInfo info);
        void finishActivity();
    }

    private final BubbleRepository repository;
    private BubbleView view;

    public BubblePresenter() {
        this(new BubbleRepositoryImpl());
    }

    public BubblePresenter(BubbleRepository repository) {
        this.repository = repository;
    }

    public void attachView(BubbleView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void init(Object caller) {
        repository.notifyCloseOtherActivities(caller);
    }

    public BubbleChatIntentInfo parseIntent(Intent intent) {
        if (intent == null) {
            return null;
        }
        int account = intent.getIntExtra("currentAccount", UserConfig.selectedAccount);
        if (!UserConfig.isValidAccount(account)) {
            return null;
        }
        if (intent.getAction() != null && intent.getAction().startsWith("com.tmessages.openchat")) {
            long chatId = intent.getLongExtra("chatId", 0);
            long userId = intent.getLongExtra("userId", 0);
            Bundle args = new Bundle();
            long dialogId;
            if (userId != 0) {
                dialogId = userId;
                args.putLong("user_id", userId);
            } else {
                dialogId = -chatId;
                args.putLong("chat_id", chatId);
            }
            return new BubbleChatIntentInfo(account, dialogId, args);
        }
        return null;
    }

    public void onBubbleChatOpened(int account, long dialogId) {
        repository.onBubbleChatOpened(account, dialogId);
    }

    public void onBubbleChatClosed(int account, long dialogId) {
        repository.onBubbleChatClosed(account, dialogId);
    }

    public void onPause() {
        repository.onPausePasscode();
    }

    public void onResume() {
        repository.onResumePasscode();
    }
}
