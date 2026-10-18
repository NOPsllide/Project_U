package org.telegram.core.presenter;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import org.telegram.core.data.repository.ActionIntroRepository;
import org.telegram.core.data.repository.ActionIntroRepositoryImpl;
import org.telegram.messenger.LocationController;
import org.telegram.ui.ActionIntroActivity;

public class ActionIntroPresenter {

    public interface ActionIntroView {
        void navigateToChannelCreate(Bundle args);
        void navigateToPasscode();
        void navigateToPeopleNearby();
        void navigateToGroupCreate(Bundle args);
        void showChangePhoneAlert();
        void openQrReader();
        void requestCameraPermission();
        void requestLocationPermission();
        void openLocationSettings();
        void showLocationRequiredAlert();
        void showCameraPermissionAlert();
        void updateAddress(String address, String displayAddress, Location location);
    }

    private ActionIntroView view;
    private final ActionIntroRepository repository;

    public ActionIntroPresenter() {
        this(new ActionIntroRepositoryImpl());
    }

    public ActionIntroPresenter(ActionIntroRepository repository) {
        this.repository = repository;
    }

    public void attachView(ActionIntroView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void checkLocationEnabledOnResume() {
        if (repository.isLocationEnabled() && view != null) {
            view.navigateToPeopleNearby();
        }
    }

    public void fetchAddress(Location location, LocationController.LocationFetchCallback callback) {
        repository.fetchLocationAddress(location, callback);
    }

    public String getCurrentUserPhone(int currentAccount) {
        return repository.getCurrentUserPhone(currentAccount);
    }

    public void onActionButtonClick(int currentType, Activity activity, long currentUserId, String currentGroupCreateAddress, Location currentGroupCreateLocation) {
        if (view == null || activity == null) {
            return;
        }
        switch (currentType) {
            case ActionIntroActivity.ACTION_TYPE_QR_LOGIN:
                if (Build.VERSION.SDK_INT >= 23 && activity.checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    view.requestCameraPermission();
                } else {
                    view.openQrReader();
                }
                break;
            case ActionIntroActivity.ACTION_TYPE_CHANNEL_CREATE:
                view.navigateToChannelCreate(repository.buildChannelCreateArgs());
                break;
            case ActionIntroActivity.ACTION_TYPE_SET_PASSCODE:
                view.navigateToPasscode();
                break;
            case ActionIntroActivity.ACTION_TYPE_NEARBY_LOCATION_ACCESS:
                view.requestLocationPermission();
                break;
            case ActionIntroActivity.ACTION_TYPE_NEARBY_LOCATION_ENABLED:
                view.openLocationSettings();
                break;
            case ActionIntroActivity.ACTION_TYPE_NEARBY_GROUP_CREATE:
                if (currentGroupCreateAddress != null && currentGroupCreateLocation != null) {
                    view.navigateToGroupCreate(repository.buildGroupCreateArgs(currentUserId, currentGroupCreateAddress, currentGroupCreateLocation));
                }
                break;
            case ActionIntroActivity.ACTION_TYPE_CHANGE_PHONE_NUMBER:
                view.showChangePhoneAlert();
                break;
        }
    }

    public void handleLocationPermissionResult(int[] grantResults) {
        if (view == null) {
            return;
        }
        if (grantResults != null && grantResults.length != 0) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                view.showLocationRequiredAlert();
            } else {
                view.navigateToPeopleNearby();
            }
        }
    }

    public void handleCameraPermissionResult(int[] grantResults) {
        if (view == null) {
            return;
        }
        if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            view.openQrReader();
        } else {
            view.showCameraPermissionAlert();
        }
    }
}
