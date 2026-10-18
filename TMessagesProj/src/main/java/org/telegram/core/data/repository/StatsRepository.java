package org.telegram.core.data.repository;

import org.telegram.messenger.StatsController;

public interface StatsRepository {

    int TYPE_CALLS = StatsController.TYPE_CALLS;
    int TYPE_MESSAGES = StatsController.TYPE_MESSAGES;
    int TYPE_PHOTOS = StatsController.TYPE_PHOTOS;
    int TYPE_AUDIOS = StatsController.TYPE_AUDIOS;
    int TYPE_VIDEOS = StatsController.TYPE_VIDEOS;
    int TYPE_FILES = StatsController.TYPE_FILES;
    int TYPE_TOTAL = StatsController.TYPE_TOTAL;

    void resetStats(int networkType);
    int getSentItemsCount(int networkType, int dataType);
    int getReceivedItemsCount(int networkType, int dataType);
    int getCallsTotalTime(int networkType);
    long getSentBytesCount(int networkType, int dataType);
    long getReceivedBytesCount(int networkType, int dataType);
    long getResetStatsDate(int networkType);
}
