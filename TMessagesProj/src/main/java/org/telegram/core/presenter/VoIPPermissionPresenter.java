package org.telegram.core.presenter;

import android.content.pm.PackageManager;

import org.telegram.core.data.repository.VoIPRepository;
import org.telegram.core.data.repository.VoIPRepositoryImpl;

public class VoIPPermissionPresenter {

    public interface VoIPPermissionView {
        void finishActivity();
        void startVoIPActivity();
        void showPermissionDeniedAlert(int requestCode);
        boolean shouldShowRationale(String permission);
    }

    private final VoIPRepository repository;
    private VoIPPermissionView view;

    public VoIPPermissionPresenter() {
        this(new VoIPRepositoryImpl());
    }

    public VoIPPermissionPresenter(VoIPRepository repository) {
        this.repository = repository;
    }

    public void attachView(VoIPPermissionView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public boolean isVideoCall() {
        return repository.isVideoCall();
    }

    public void onPermissionsResult(int requestCode, int[] grantResults, String audioPermission) {
        if (requestCode != 101 && requestCode != 102) {
            return;
        }
        boolean allGranted = true;
        for (int a = 0; a < grantResults.length; a++) {
            if (grantResults[a] != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        if (grantResults.length > 0 && allGranted) {
            repository.acceptIncomingCall();
            if (view != null) {
                view.finishActivity();
                view.startVoIPActivity();
            }
        } else {
            if (view != null && !view.shouldShowRationale(audioPermission)) {
                repository.declineIncomingCall();
                view.showPermissionDeniedAlert(requestCode);
            } else if (view != null) {
                view.finishActivity();
            }
        }
    }
}
