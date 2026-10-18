package org.telegram.core.data.repository;

import org.telegram.messenger.ContactsController;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.GroupCallActivity;

public class PermissionRepositoryImpl implements PermissionRepository {

    @Override
    public void checkMediaPaths() {
        ImageLoader.getInstance().checkMediaPaths();
    }

    @Override
    public void forceImportContacts(int account) {
        ContactsController.getInstance(account).forceImportContacts();
    }

    @Override
    public void initCamera() {
        if (SharedConfig.inappCamera) {
            org.telegram.messenger.camera.CameraController.getInstance().initCamera(null);
        }
    }

    @Override
    public void postLocationPermission(boolean granted) {
        NotificationCenter.getGlobalInstance().postNotificationName(granted ? NotificationCenter.locationPermissionGranted : NotificationCenter.locationPermissionDenied);
    }

    @Override
    public void enableGroupCallCamera() {
        if (GroupCallActivity.groupCallInstance != null) {
            GroupCallActivity.groupCallInstance.enableCamera();
        }
    }
}
