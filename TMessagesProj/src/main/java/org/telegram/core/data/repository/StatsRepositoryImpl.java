package org.telegram.core.data.repository;

import org.telegram.messenger.StatsController;

public class StatsRepositoryImpl implements StatsRepository {

    private final int currentAccount;

    public StatsRepositoryImpl(int currentAccount) {
        this.currentAccount = currentAccount;
    }

    @Override
    public void resetStats(int networkType) {
        StatsController.getInstance(currentAccount).resetStats(networkType);
    }

    @Override
    public int getSentItemsCount(int networkType, int dataType) {
        return StatsController.getInstance(currentAccount).getSentItemsCount(networkType, dataType);
    }

    @Override
    public int getReceivedItemsCount(int networkType, int dataType) {
        return StatsController.getInstance(currentAccount).getRecivedItemsCount(networkType, dataType);
    }

    @Override
    public int getCallsTotalTime(int networkType) {
        return StatsController.getInstance(currentAccount).getCallsTotalTime(networkType);
    }

    @Override
    public long getSentBytesCount(int networkType, int dataType) {
        return StatsController.getInstance(currentAccount).getSentBytesCount(networkType, dataType);
    }

    @Override
    public long getReceivedBytesCount(int networkType, int dataType) {
        return StatsController.getInstance(currentAccount).getReceivedBytesCount(networkType, dataType);
    }

    @Override
    public long getResetStatsDate(int networkType) {
        return StatsController.getInstance(currentAccount).getResetStatsDate(networkType);
    }
}
