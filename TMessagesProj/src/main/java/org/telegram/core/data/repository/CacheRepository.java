package org.telegram.core.data.repository;

public interface CacheRepository {

    interface CacheSizeCallback {
        void onCalculated(long cacheSize, long photoSize, long videoSize, long documentsSize, long musicSize, long stickersSize, long audioSize, long totalSize, long totalDeviceSize, long totalDeviceFreeSize);
    }

    interface CacheCleanupCallback {
        void onCleaned(long clearedSize, long cacheSize, long photoSize, long videoSize, long documentsSize, long musicSize, long stickersSize, long audioSize, long totalSize, long totalDeviceSize, long totalDeviceFreeSize, boolean imagesCleared);
    }

    long getDatabaseSize();

    void clearLocalDatabase();

    void calculateCacheSize(CacheSizeCallback callback);

    void cleanupFolders(boolean[] clearTypes, long[] currentSizes, CacheCleanupCallback callback);

    int getKeepMedia();

    void setKeepMedia(int value);

    void cancelCalculations();
}
