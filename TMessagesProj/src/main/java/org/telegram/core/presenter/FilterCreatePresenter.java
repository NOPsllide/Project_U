package org.telegram.core.presenter;

import org.telegram.core.data.repository.FilterRepository;
import org.telegram.core.data.repository.FilterRepositoryImpl;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.support.LongSparseIntArray;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.FilterCreateActivity;

import java.util.ArrayList;

public class FilterCreatePresenter {

    public interface FilterCreateView {
        void onFilterSaved();
        void onFilterDeleted();
    }

    private FilterCreateView view;
    private final FilterRepository repository;

    public FilterCreatePresenter(int currentAccount) {
        this(new FilterRepositoryImpl(currentAccount));
    }

    public FilterCreatePresenter(FilterRepository repository) {
        this.repository = repository;
    }

    public void attachView(FilterCreateView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void deleteFilter(MessagesController.DialogFilter filter) {
        repository.deleteDialogFilter(filter, () -> {
            if (view != null) {
                view.onFilterDeleted();
            }
        });
    }

    public void saveFilter(MessagesController.DialogFilter filter, int newFilterFlags, String newFilterName, ArrayList<Long> newAlwaysShow, ArrayList<Long> newNeverShow, LongSparseIntArray newPinned, boolean creatingNew, boolean hasUserChanged, BaseFragment fragment) {
        FilterCreateActivity.saveFilterToServer(filter, newFilterFlags, newFilterName, newAlwaysShow, newNeverShow, newPinned, creatingNew, false, hasUserChanged, true, true, fragment, () -> {
            if (view != null) {
                view.onFilterSaved();
            }
        });
    }
}
