package org.telegram.core.presenter;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

public class PhotoPickerSearchPresenter {

    public interface PhotoPickerSearchView {
        void onSearchHintChanged(String hint);
        void onSearchTextChanged(String text);
        void onRecentSearchCleared();
    }

    private PhotoPickerSearchView view;
    private int currentTab = 0;

    public void attachView(PhotoPickerSearchView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public int getCurrentTab() {
        return currentTab;
    }

    public void selectTab(int id) {
        currentTab = id;
        if (view != null) {
            String hint = id == 0
                    ? LocaleController.getString("SearchImagesTitle", R.string.SearchImagesTitle)
                    : LocaleController.getString("SearchGifsTitle", R.string.SearchGifsTitle);
            view.onSearchHintChanged(hint);
        }
    }

    public void onSearchText(String text) {
        if (view != null) {
            view.onSearchTextChanged(text);
        }
    }

    public void clearRecentSearch() {
        if (view != null) {
            view.onRecentSearchCleared();
        }
    }
}
