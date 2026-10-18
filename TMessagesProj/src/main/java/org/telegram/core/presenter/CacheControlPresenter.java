package org.telegram.core.presenter;

import org.telegram.core.data.repository.CacheRepository;
import org.telegram.core.data.repository.CacheRepositoryImpl;
import org.telegram.messenger.NotificationCenter;

public class CacheControlPresenter implements NotificationCenter.NotificationCenterDelegate {

    public interface CacheControlView {
        void onCacheCalculated(long cacheSize, long photoSize, long videoSize, long documentsSize, long musicSize, long stickersSize, long audioSize, long totalSize, long totalDeviceSize, long totalDeviceFreeSize);
        void onCacheCleaned(long clearedSize, long cacheSize, long photoSize, long videoSize, long documentsSize, long musicSize, long stickersSize, long audioSize, long totalSize, long totalDeviceSize, long totalDeviceFreeSize, boolean imagesCleared);
        void onDatabaseCleared(long newDatabaseSize);
    }

    private final int currentAccount;
    private final CacheRepository cacheRepository;
    private CacheControlView view;

    public CacheControlPresenter(int currentAccount) {
        this(currentAccount, new CacheRepositoryImpl(currentAccount));
    }

    public CacheControlPresenter(int currentAccount, CacheRepository cacheRepository) {
        this.currentAccount = currentAccount;
        this.cacheRepository = cacheRepository;
    }

    public void attachView(CacheControlView view) {
        this.view = view;
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.didClearDatabase);
    }

    public void detachView() {
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.didClearDatabase);
        this.view = null;
        cacheRepository.cancelCalculations();
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.didClearDatabase) {
            long newDatabaseSize = cacheRepository.getDatabaseSize();
            if (view != null) {
                view.onDatabaseCleared(newDatabaseSize);
            }
        }
    }

    public long getDatabaseSize() {
        return cacheRepository.getDatabaseSize();
    }

    public void clearLocalDatabase() {
        cacheRepository.clearLocalDatabase();
    }

    public void calculateCacheSize() {
        cacheRepository.calculateCacheSize((cacheSize, photoSize, videoSize, documentsSize, musicSize, stickersSize, audioSize, totalSize, totalDeviceSize, totalDeviceFreeSize) -> {
            if (view != null) {
                view.onCacheCalculated(cacheSize, photoSize, videoSize, documentsSize, musicSize, stickersSize, audioSize, totalSize, totalDeviceSize, totalDeviceFreeSize);
            }
        });
    }

    public void cleanupFolders(boolean[] clearTypes, long[] currentSizes) {
        cacheRepository.cleanupFolders(clearTypes, currentSizes, (clearedSize, cacheSize, photoSize, videoSize, documentsSize, musicSize, stickersSize, audioSize, totalSize, totalDeviceSize, totalDeviceFreeSize, imagesCleared) -> {
            if (view != null) {
                view.onCacheCleaned(clearedSize, cacheSize, photoSize, videoSize, documentsSize, musicSize, stickersSize, audioSize, totalSize, totalDeviceSize, totalDeviceFreeSize, imagesCleared);
            }
        });
    }

    public int getKeepMedia() {
        return cacheRepository.getKeepMedia();
    }

    public void setKeepMedia(int value) {
        cacheRepository.setKeepMedia(value);
    }

    public void cancel() {
        cacheRepository.cancelCalculations();
    }
}
