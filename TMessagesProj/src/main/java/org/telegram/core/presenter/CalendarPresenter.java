package org.telegram.core.presenter;

import org.telegram.core.data.repository.CalendarRepository;
import org.telegram.core.data.repository.CalendarRepositoryImpl;
import org.telegram.tgnet.TLRPC;

public class CalendarPresenter {

    public interface CalendarView {
        void onSearchResultsCalendarLoaded(TLRPC.TL_messages_searchResultsCalendar result);
        void onSearchResultsCalendarError(TLRPC.TL_error error);
    }

    private CalendarView view;
    private final CalendarRepository repository;

    public CalendarPresenter(int currentAccount) {
        this(new CalendarRepositoryImpl(currentAccount));
    }

    public CalendarPresenter(CalendarRepository repository) {
        this.repository = repository;
    }

    public void attachView(CalendarView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void loadSearchResultsCalendar(long dialogId, int offsetId, int filterType) {
        repository.loadSearchResultsCalendar(dialogId, offsetId, filterType, new CalendarRepository.CalendarCallback() {
            @Override
            public void onSuccess(TLRPC.TL_messages_searchResultsCalendar result) {
                if (view != null) {
                    view.onSearchResultsCalendarLoaded(result);
                }
            }

            @Override
            public void onError(TLRPC.TL_error error) {
                if (view != null) {
                    view.onSearchResultsCalendarError(error);
                }
            }
        });
    }
}
