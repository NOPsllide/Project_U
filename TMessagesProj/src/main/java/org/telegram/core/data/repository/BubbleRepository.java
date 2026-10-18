package org.telegram.core.data.repository;

public interface BubbleRepository {

    void notifyCloseOtherActivities(Object caller);

    void onBubbleChatOpened(int account, long dialogId);

    void onBubbleChatClosed(int account, long dialogId);

    boolean isAppLocked();

    boolean needShowPasscode();

    void setAppLocked(boolean locked);

    void onPausePasscode();

    void onResumePasscode();
}
