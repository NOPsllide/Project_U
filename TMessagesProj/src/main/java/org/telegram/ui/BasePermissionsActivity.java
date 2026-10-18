package org.telegram.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.RawRes;

import org.telegram.core.presenter.PermissionPresenter;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AlertsCreator;

public class BasePermissionsActivity extends Activity implements PermissionPresenter.PermissionView {
    public final static int REQUEST_CODE_GEOLOCATION = PermissionPresenter.REQUEST_CODE_GEOLOCATION,
            REQUEST_CODE_EXTERNAL_STORAGE = PermissionPresenter.REQUEST_CODE_EXTERNAL_STORAGE,
            REQUEST_CODE_ATTACH_CONTACT = PermissionPresenter.REQUEST_CODE_ATTACH_CONTACT,
            REQUEST_CODE_CALLS = PermissionPresenter.REQUEST_CODE_CALLS,
            REQUEST_CODE_OPEN_CAMERA = PermissionPresenter.REQUEST_CODE_OPEN_CAMERA,
            REQUEST_CODE_VIDEO_MESSAGE = PermissionPresenter.REQUEST_CODE_VIDEO_MESSAGE,
            REQUEST_CODE_EXTERNAL_STORAGE_FOR_AVATAR = PermissionPresenter.REQUEST_CODE_EXTERNAL_STORAGE_FOR_AVATAR,
            REQUEST_CODE_SIGN_IN_WITH_GOOGLE = PermissionPresenter.REQUEST_CODE_SIGN_IN_WITH_GOOGLE,
            REQUEST_CODE_PAYMENT_FORM = PermissionPresenter.REQUEST_CODE_PAYMENT_FORM;

    protected int currentAccount = -1;
    protected PermissionPresenter permissionPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionPresenter = new PermissionPresenter();
        permissionPresenter.attachView(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (permissionPresenter != null) {
            permissionPresenter.detachView();
        }
    }

    protected boolean checkPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (permissionPresenter != null) {
            return permissionPresenter.handlePermissionsResult(currentAccount, requestCode, permissions, grantResults);
        }
        return true;
    }

    protected AlertDialog createPermissionErrorAlert(@RawRes int animationId, String message) {
        return new AlertDialog.Builder(this)
                .setTopAnimation(animationId, AlertsCreator.PERMISSIONS_REQUEST_TOP_ICON_SIZE, false, Theme.getColor(Theme.key_dialogTopBackground))
                .setMessage(AndroidUtilities.replaceTags(message))
                .setPositiveButton(LocaleController.getString("PermissionOpenSettings", R.string.PermissionOpenSettings), (dialogInterface, i) -> {
                    try {
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + ApplicationLoader.applicationContext.getPackageName()));
                        startActivity(intent);
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                })
                .setNegativeButton(LocaleController.getString("ContactsPermissionAlertNotNow", R.string.ContactsPermissionAlertNotNow), null)
                .create();
    }

    @Override
    public void showPermissionErrorAlert(@RawRes int animationId, String message) {
        createPermissionErrorAlert(animationId, message).show();
    }
}
