package org.telegram.core.presenter;

import org.telegram.core.data.repository.FilterRepository;
import org.telegram.core.data.repository.FilterRepositoryImpl;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;

public class FiltersSetupPresenter {

    public interface FiltersSetupView {
        void onFilterDeleted(MessagesController.DialogFilter filter);
        void onSuggestedFilterAdded(TLRPC.TL_dialogFilterSuggested suggested);
    }

    private FiltersSetupView view;
    private final FilterRepository repository;

    public FiltersSetupPresenter(int currentAccount) {
        this(new FilterRepositoryImpl(currentAccount));
    }

    public FiltersSetupPresenter(FilterRepository repository) {
        this.repository = repository;
    }

    public void attachView(FiltersSetupView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void loadFilters(boolean force) {
        repository.loadFilters(force);
    }

    public void saveOrder() {
        repository.saveDialogFiltersOrder();
    }

    public void deleteFilter(MessagesController.DialogFilter filter) {
        repository.deleteDialogFilter(filter, () -> {
            if (view != null) {
                view.onFilterDeleted(filter);
            }
        });
    }

    public void addSuggestedFilter(TLRPC.TL_dialogFilterSuggested suggested, BaseFragment fragment) {
        repository.addSuggestedFilter(suggested, fragment, () -> {
            if (view != null) {
                view.onSuggestedFilterAdded(suggested);
            }
        });
    }

    public ArrayList<MessagesController.DialogFilter> getDialogFilters() {
        return repository.getDialogFilters();
    }

    public ArrayList<TLRPC.TL_dialogFilterSuggested> getSuggestedFilters() {
        return repository.getSuggestedFilters();
    }
}
