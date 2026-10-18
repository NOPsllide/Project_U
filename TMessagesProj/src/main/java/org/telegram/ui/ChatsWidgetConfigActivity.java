package org.telegram.ui;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.telegram.core.presenter.WidgetConfigPresenter;
import org.telegram.messenger.AndroidUtilities;

public class ChatsWidgetConfigActivity extends ExternalActionActivity implements WidgetConfigPresenter.WidgetConfigView {

    private WidgetConfigPresenter presenter;
    private int creatingAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        presenter = new WidgetConfigPresenter();
        presenter.attachView(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.detachView();
        }
    }

    @Override
    public void onWidgetConfigured(int appWidgetId) {
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    @Override
    public void onConfigurationCancelled() {
        finish();
    }

    @Override
    protected boolean handleIntent(Intent intent, boolean isNew, boolean restore, boolean fromPassword, int intentAccount, int state) {
        if (!checkPasscode(intent, isNew, restore, fromPassword, intentAccount, state)) {
            return false;
        }
        creatingAppWidgetId = presenter != null ? presenter.getAppWidgetId(intent) : AppWidgetManager.INVALID_APPWIDGET_ID;
        if (creatingAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            Bundle args = new Bundle();
            args.putBoolean("onlySelect", true);
            args.putInt("dialogsType", 10);
            args.putBoolean("allowSwitchAccount", true);
            EditWidgetActivity fragment = new EditWidgetActivity(EditWidgetActivity.TYPE_CHATS, creatingAppWidgetId);
            fragment.setDelegate(dialogs -> {
                if (presenter != null) {
                    presenter.onWidgetFinished(creatingAppWidgetId);
                } else {
                    onWidgetConfigured(creatingAppWidgetId);
                }
            });

            if (AndroidUtilities.isTablet()) {
                if (layersActionBarLayout.getFragmentStack().isEmpty()) {
                    layersActionBarLayout.addFragmentToStack(fragment);
                }
            } else {
                if (actionBarLayout.getFragmentStack().isEmpty()) {
                    actionBarLayout.addFragmentToStack(fragment);
                }
            }
            if (!AndroidUtilities.isTablet()) {
                backgroundTablet.setVisibility(View.GONE);
            }
            actionBarLayout.showLastFragment();
            if (AndroidUtilities.isTablet()) {
                layersActionBarLayout.showLastFragment();
            }
            intent.setAction(null);
        } else {
            if (presenter != null) {
                presenter.cancel();
            } else {
                finish();
            }
        }
        return true;
    }
}
