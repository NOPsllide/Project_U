package org.telegram.core.data.repository;

import android.app.Activity;

public interface VoIPRepository {

    boolean isVideoCall();

    void acceptIncomingCall();

    void declineIncomingCall();

    void showRateAlert(Activity activity, Runnable onDismiss, boolean callVideo, long callId, long callAccessHash, int account);

    void showPermissionDenied(Activity activity, Runnable onDismiss, int requestCode);
}
