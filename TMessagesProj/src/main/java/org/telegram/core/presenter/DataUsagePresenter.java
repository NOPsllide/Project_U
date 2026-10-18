package org.telegram.core.presenter;

import org.telegram.core.data.repository.StatsRepository;
import org.telegram.core.data.repository.StatsRepositoryImpl;

public class DataUsagePresenter {

    public interface DataUsageView {
        void onStatsReset(int networkType);
    }

    private final int currentAccount;
    private final StatsRepository statsRepository;
    private DataUsageView view;

    public DataUsagePresenter(int currentAccount) {
        this(currentAccount, new StatsRepositoryImpl(currentAccount));
    }

    public DataUsagePresenter(int currentAccount, StatsRepository statsRepository) {
        this.currentAccount = currentAccount;
        this.statsRepository = statsRepository;
    }

    public void attachView(DataUsageView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void resetStats(int networkType) {
        statsRepository.resetStats(networkType);
        if (view != null) {
            view.onStatsReset(networkType);
        }
    }

    public int getSentItemsCount(int networkType, int dataType) {
        return statsRepository.getSentItemsCount(networkType, dataType);
    }

    public int getReceivedItemsCount(int networkType, int dataType) {
        return statsRepository.getReceivedItemsCount(networkType, dataType);
    }

    public int getCallsTotalTime(int networkType) {
        return statsRepository.getCallsTotalTime(networkType);
    }

    public long getSentBytesCount(int networkType, int dataType) {
        return statsRepository.getSentBytesCount(networkType, dataType);
    }

    public long getReceivedBytesCount(int networkType, int dataType) {
        return statsRepository.getReceivedBytesCount(networkType, dataType);
    }

    public long getResetStatsDate(int networkType) {
        return statsRepository.getResetStatsDate(networkType);
    }
}
