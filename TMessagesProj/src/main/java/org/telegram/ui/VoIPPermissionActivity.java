package org.telegram.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import org.telegram.core.data.repository.VoIPRepositoryImpl;
import org.telegram.core.presenter.VoIPPermissionPresenter;
import org.telegram.messenger.FileLog;

import java.util.ArrayList;

@TargetApi(23)
public class VoIPPermissionActivity extends Activity implements VoIPPermissionPresenter.VoIPPermissionView {

    private VoIPPermissionPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = new VoIPPermissionPresenter();
        presenter.attachView(this);

        boolean isVideoCall = presenter.isVideoCall();

        ArrayList<String> permissions = new ArrayList<>();
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO);
        }
        if (isVideoCall && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }
        if (!permissions.isEmpty()) {
            try {
                requestPermissions(permissions.toArray(new String[0]), isVideoCall ? 102 : 101);
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.detachView();
        }
    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void startVoIPActivity() {
        startActivity(new Intent(this, LaunchActivity.class).setAction("voip"));
    }

    @Override
    public void showPermissionDeniedAlert(int requestCode) {
        new VoIPRepositoryImpl().showPermissionDenied(this, this::finish, requestCode);
    }

    @Override
    public boolean shouldShowRationale(String permission) {
        return shouldShowRequestPermissionRationale(permission);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (presenter != null) {
            presenter.onPermissionsResult(requestCode, grantResults, Manifest.permission.RECORD_AUDIO);
        }
    }
}
