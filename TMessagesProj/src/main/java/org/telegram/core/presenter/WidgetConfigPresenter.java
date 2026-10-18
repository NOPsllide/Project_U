package org.telegram.core.presenter;

import android.content.Context;
import android.content.Intent;

import org.telegram.core.data.repository.WidgetRepository;
import org.telegram.core.data.repository.WidgetRepositoryImpl;
import org.telegram.messenger.MessagesStorage;

import java.util.ArrayList;

public class WidgetConfigPresenter {

    public interface WidgetConfigView {
        void onWidgetConfigured(int appWidgetId);
        void onConfigurationCancelled();
    }

    private final WidgetRepository repository;
    private WidgetConfigView view;

    public WidgetConfigPresenter() {
        this(new WidgetRepositoryImpl());
    }

    public WidgetConfigPresenter(WidgetRepository repository) {
        this.repository = repository;
    }

    public void attachView(WidgetConfigView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public int getAppWidgetId(Intent intent) {
        return repository.extractAppWidgetId(intent);
    }

    public void saveFeedWidget(Context context, int account, int appWidgetId, ArrayList<MessagesStorage.TopicKey> dids) {
        repository.saveFeedWidgetConfig(context, account, appWidgetId, dids);
        if (view != null) {
            view.onWidgetConfigured(appWidgetId);
        }
    }

    public void onWidgetFinished(int appWidgetId) {
        if (view != null) {
            view.onWidgetConfigured(appWidgetId);
        }
    }

    public void cancel() {
        if (view != null) {
            view.onConfigurationCancelled();
        }
    }
}
