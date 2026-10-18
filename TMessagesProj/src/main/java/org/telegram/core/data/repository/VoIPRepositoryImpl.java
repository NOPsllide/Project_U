package org.telegram.core.data.repository;

import android.app.Activity;

import org.telegram.messenger.voip.VoIPService;
import org.telegram.ui.Components.voip.VoIPHelper;

public class VoIPRepositoryImpl implements VoIPRepository {

    @Override
    public boolean isVideoCall() {
        VoIPService service = VoIPService.getSharedInstance();
        return service != null && service.privateCall != null && service.privateCall.video;
    }

    @Override
    public void acceptIncomingCall() {
        VoIPService service = VoIPService.getSharedInstance();
        if (service != null) {
            service.acceptIncomingCall();
        }
    }

    @Override
    public void declineIncomingCall() {
        VoIPService service = VoIPService.getSharedInstance();
        if (service != null) {
            service.declineIncomingCall();
        }
    }

    @Override
    public void showRateAlert(Activity activity, Runnable onDismiss, boolean callVideo, long callId, long callAccessHash, int account) {
        VoIPHelper.showRateAlert(activity, onDismiss, callVideo, callId, callAccessHash, account, false);
    }

    @Override
    public void showPermissionDenied(Activity activity, Runnable onDismiss, int requestCode) {
        VoIPHelper.permissionDenied(activity, onDismiss, requestCode);
    }
}
