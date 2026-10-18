package org.telegram.core.presenter;

import android.Manifest;
import android.content.pm.PackageManager;

import org.telegram.core.data.repository.PermissionRepository;
import org.telegram.core.data.repository.PermissionRepositoryImpl;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

public class PermissionPresenter {

    public interface PermissionView {
        void showPermissionErrorAlert(int animationId, String message);
    }

    public final static int REQUEST_CODE_GEOLOCATION = 2,
            REQUEST_CODE_EXTERNAL_STORAGE = 4,
            REQUEST_CODE_ATTACH_CONTACT = 5,
            REQUEST_CODE_CALLS = 7,
            REQUEST_CODE_OPEN_CAMERA = 20,
            REQUEST_CODE_VIDEO_MESSAGE = 150,
            REQUEST_CODE_EXTERNAL_STORAGE_FOR_AVATAR = 151,
            REQUEST_CODE_SIGN_IN_WITH_GOOGLE = 200,
            REQUEST_CODE_PAYMENT_FORM = 210;

    private final PermissionRepository repository;
    private PermissionView view;

    public PermissionPresenter() {
        this(new PermissionRepositoryImpl());
    }

    public PermissionPresenter(PermissionRepository repository) {
        this.repository = repository;
    }

    public void attachView(PermissionView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public boolean handlePermissionsResult(int currentAccount, int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults == null) {
            grantResults = new int[0];
        }
        if (permissions == null) {
            permissions = new String[0];
        }

        boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;

        if (requestCode == 104) {
            if (granted) {
                repository.enableGroupCallCamera();
            } else if (view != null) {
                view.showPermissionErrorAlert(R.raw.permission_request_camera, LocaleController.getString("VoipNeedCameraPermission", R.string.VoipNeedCameraPermission));
            }
        } else if (requestCode == REQUEST_CODE_EXTERNAL_STORAGE || requestCode == REQUEST_CODE_EXTERNAL_STORAGE_FOR_AVATAR) {
            if (!granted) {
                if (view != null) {
                    view.showPermissionErrorAlert(R.raw.permission_request_folder, requestCode == REQUEST_CODE_EXTERNAL_STORAGE_FOR_AVATAR ? LocaleController.getString("PermissionNoStorageAvatar", R.string.PermissionNoStorageAvatar) :
                            LocaleController.getString("PermissionStorageWithHint", R.string.PermissionStorageWithHint));
                }
            } else {
                repository.checkMediaPaths();
            }
        } else if (requestCode == REQUEST_CODE_ATTACH_CONTACT) {
            if (!granted) {
                if (view != null) {
                    view.showPermissionErrorAlert(R.raw.permission_request_contacts, LocaleController.getString("PermissionNoContactsSharing", R.string.PermissionNoContactsSharing));
                }
                return false;
            } else {
                repository.forceImportContacts(currentAccount);
            }
        } else if (requestCode == 3 || requestCode == REQUEST_CODE_VIDEO_MESSAGE) {
            boolean audioGranted = true;
            boolean cameraGranted = true;
            for (int i = 0, size = Math.min(permissions.length, grantResults.length); i < size; i++) {
                if (Manifest.permission.RECORD_AUDIO.equals(permissions[i])) {
                    audioGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                } else if (Manifest.permission.CAMERA.equals(permissions[i])) {
                    cameraGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                }
            }
            if (requestCode == REQUEST_CODE_VIDEO_MESSAGE && (!audioGranted || !cameraGranted)) {
                if (view != null) {
                    view.showPermissionErrorAlert(R.raw.permission_request_camera, LocaleController.getString("PermissionNoCameraMicVideo", R.string.PermissionNoCameraMicVideo));
                }
            } else if (!audioGranted) {
                if (view != null) {
                    view.showPermissionErrorAlert(R.raw.permission_request_microphone, LocaleController.getString("PermissionNoAudioWithHint", R.string.PermissionNoAudioWithHint));
                }
            } else if (!cameraGranted) {
                if (view != null) {
                    view.showPermissionErrorAlert(R.raw.permission_request_camera, LocaleController.getString("PermissionNoCameraWithHint", R.string.PermissionNoCameraWithHint));
                }
            } else {
                repository.initCamera();
                return false;
            }
        } else if (requestCode == 18 || requestCode == 19 || requestCode == REQUEST_CODE_OPEN_CAMERA || requestCode == 22) {
            if (!granted && view != null) {
                view.showPermissionErrorAlert(R.raw.permission_request_camera, LocaleController.getString("PermissionNoCameraWithHint", R.string.PermissionNoCameraWithHint));
            }
        } else if (requestCode == REQUEST_CODE_GEOLOCATION) {
            repository.postLocationPermission(granted);
        }
        return true;
    }
}
