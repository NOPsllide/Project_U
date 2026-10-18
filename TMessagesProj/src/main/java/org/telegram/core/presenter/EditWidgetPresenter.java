package org.telegram.core.presenter;

import android.content.Context;
import org.telegram.core.data.repository.WidgetRepository;
import org.telegram.core.data.repository.WidgetRepositoryImpl;

import java.util.ArrayList;

public class EditWidgetPresenter {

    public interface EditWidgetView {
        void onWidgetSaved();
    }

    private EditWidgetView view;
    private final WidgetRepository repository;

    public EditWidgetPresenter() {
        this(new WidgetRepositoryImpl());
    }

    public EditWidgetPresenter(WidgetRepository repository) {
        this.repository = repository;
    }

    public void attachView(EditWidgetView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void loadWidgetDialogs(int currentAccount, int currentWidgetId, int widgetType, ArrayList<Long> selectedDialogs) {
        repository.loadWidgetDialogs(currentAccount, currentWidgetId, widgetType, selectedDialogs);
    }

    public void saveWidget(Context context, int currentAccount, int currentWidgetId, int widgetType, ArrayList<Long> selectedDialogs) {
        repository.saveWidgetConfig(context, currentAccount, currentWidgetId, widgetType, selectedDialogs);
        if (view != null) {
            view.onWidgetSaved();
        }
    }
}
