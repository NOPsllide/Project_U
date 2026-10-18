package org.telegram.core.data.repository;

import android.os.SystemClock;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.SharedConfig;

public class BubbleRepositoryImpl implements BubbleRepository {

    @Override
    public void notifyCloseOtherActivities(Object caller) {
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.closeOtherAppActivities, caller);
    }

    @Override
    public void onBubbleChatOpened(int account, long dialogId) {
        if (account < 0) {
            return;
        }
        NotificationCenter.getInstance(account).postNotificationName(NotificationCenter.closeChats, dialogId);
        AccountInstance.getInstance(account).getNotificationsController().setOpenedInBubble(dialogId, true);
        AccountInstance.getInstance(account).getConnectionsManager().setAppPaused(false, false);
    }

    @Override
    public void onBubbleChatClosed(int account, long dialogId) {
        if (account < 0) {
            return;
        }
        AccountInstance.getInstance(account).getNotificationsController().setOpenedInBubble(dialogId, false);
        AccountInstance.getInstance(account).getConnectionsManager().setAppPaused(false, false);
    }

    @Override
    public boolean isAppLocked() {
        return SharedConfig.appLocked;
    }

    @Override
    public boolean needShowPasscode() {
        return AndroidUtilities.needShowPasscode(true);
    }

    @Override
    public void setAppLocked(boolean locked) {
        SharedConfig.appLocked = locked;
    }

    @Override
    public void onPausePasscode() {
        if (SharedConfig.passcodeHash.length() != 0) {
            SharedConfig.lastPauseTime = (int) (SystemClock.elapsedRealtime() / 1000);
        } else {
            SharedConfig.lastPauseTime = 0;
        }
        SharedConfig.saveConfig();
    }

    @Override
    public void onResumePasscode() {
        if (SharedConfig.lastPauseTime != 0) {
            SharedConfig.lastPauseTime = 0;
            SharedConfig.saveConfig();
        }
    }
}
