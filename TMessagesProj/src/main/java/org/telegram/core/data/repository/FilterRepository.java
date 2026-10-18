package org.telegram.core.data.repository;

import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;

public interface FilterRepository {

    void loadFilters(boolean force);

    void saveDialogFiltersOrder();

    void deleteDialogFilter(MessagesController.DialogFilter filter, Runnable onComplete);

    void addSuggestedFilter(TLRPC.TL_dialogFilterSuggested suggested, BaseFragment fragment, Runnable onComplete);

    ArrayList<MessagesController.DialogFilter> getDialogFilters();

    ArrayList<TLRPC.TL_dialogFilterSuggested> getSuggestedFilters();
}
