package org.telegram.core.data.repository;

public interface PermissionRepository {

    void checkMediaPaths();

    void forceImportContacts(int account);

    void initCamera();

    void postLocationPermission(boolean granted);

    void enableGroupCallCamera();
}
