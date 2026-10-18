package org.telegram.core.data.repository;

import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.Utilities;

import java.io.File;
import java.util.ArrayList;

public class CacheRepositoryImpl implements CacheRepository {

    private final int accountNum;
    private volatile boolean canceled;

    public CacheRepositoryImpl(int accountNum) {
        this.accountNum = accountNum;
    }

    @Override
    public long getDatabaseSize() {
        return MessagesStorage.getInstance(accountNum).getDatabaseSize();
    }

    @Override
    public void clearLocalDatabase() {
        MessagesController.getInstance(accountNum).clearQueryTime();
        MessagesStorage.getInstance(accountNum).clearLocalDatabase();
    }

    @Override
    public int getKeepMedia() {
        return SharedConfig.keepMedia;
    }

    @Override
    public void setKeepMedia(int value) {
        SharedConfig.setKeepMedia(value);
    }

    @Override
    public void cancelCalculations() {
        canceled = true;
    }

    private long getDirectorySize(File dir, int documentsMusicType) {
        if (dir == null || canceled) {
            return 0;
        }
        long size = 0;
        if (dir.isDirectory()) {
            size = Utilities.getDirSize(dir.getAbsolutePath(), documentsMusicType, false);
        } else if (dir.isFile()) {
            size += dir.length();
        }
        return size;
    }

    @Override
    public void calculateCacheSize(CacheSizeCallback callback) {
        canceled = false;
        Utilities.globalQueue.postRunnable(() -> {
            long cacheSize = getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_CACHE), 0);
            if (canceled) {
                return;
            }
            long photoSize = getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_IMAGE), 0);
            photoSize += getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_IMAGE_PUBLIC), 0);
            if (canceled) {
                return;
            }
            long videoSize = getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_VIDEO), 0);
            videoSize += getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_VIDEO_PUBLIC), 0);
            if (canceled) {
                return;
            }
            long documentsSize = getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_DOCUMENT), 1);
            documentsSize += getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_FILES), 1);
            if (canceled) {
                return;
            }
            long musicSize = getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_DOCUMENT), 2);
            musicSize += getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_FILES), 2);
            if (canceled) {
                return;
            }
            long stickersSize = getDirectorySize(new File(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_CACHE), "acache"), 0);
            if (canceled) {
                return;
            }
            long audioSize = getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_AUDIO), 0);
            long totalSize = cacheSize + videoSize + audioSize + photoSize + documentsSize + musicSize + stickersSize;

            long totalDeviceSize = 0;
            long totalDeviceFreeSize = 0;
            File path = null;
            if (Build.VERSION.SDK_INT >= 19) {
                ArrayList<File> storageDirs = AndroidUtilities.getRootDirs();
                if (storageDirs != null && !storageDirs.isEmpty()) {
                    path = storageDirs.get(0);
                    if (!TextUtils.isEmpty(SharedConfig.storageCacheDir)) {
                        for (int a = 0, N = storageDirs.size(); a < N; a++) {
                            File file = storageDirs.get(a);
                            if (file.getAbsolutePath().startsWith(SharedConfig.storageCacheDir)) {
                                path = file;
                                break;
                            }
                        }
                    }
                }
            } else if (!TextUtils.isEmpty(SharedConfig.storageCacheDir)) {
                path = new File(SharedConfig.storageCacheDir);
            }
            if (path != null) {
                try {
                    StatFs stat = new StatFs(path.getPath());
                    long blockSize;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        blockSize = stat.getBlockSizeLong();
                    } else {
                        blockSize = stat.getBlockSize();
                    }
                    long availableBlocks;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        availableBlocks = stat.getAvailableBlocksLong();
                    } else {
                        availableBlocks = stat.getAvailableBlocks();
                    }
                    long blocksTotal;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        blocksTotal = stat.getBlockCountLong();
                    } else {
                        blocksTotal = stat.getBlockCount();
                    }
                    totalDeviceSize = blocksTotal * blockSize;
                    totalDeviceFreeSize = availableBlocks * blockSize;
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }

            final long finalCacheSize = cacheSize;
            final long finalPhotoSize = photoSize;
            final long finalVideoSize = videoSize;
            final long finalDocumentsSize = documentsSize;
            final long finalMusicSize = musicSize;
            final long finalStickersSize = stickersSize;
            final long finalAudioSize = audioSize;
            final long finalTotalSize = totalSize;
            final long finalTotalDeviceSize = totalDeviceSize;
            final long finalTotalDeviceFreeSize = totalDeviceFreeSize;

            AndroidUtilities.runOnUIThread(() -> {
                if (callback != null && !canceled) {
                    callback.onCalculated(finalCacheSize, finalPhotoSize, finalVideoSize, finalDocumentsSize, finalMusicSize, finalStickersSize, finalAudioSize, finalTotalSize, finalTotalDeviceSize, finalTotalDeviceFreeSize);
                }
            });
        });
    }

    @Override
    public void cleanupFolders(boolean[] clearTypes, long[] currentSizes, CacheCleanupCallback callback) {
        Utilities.globalQueue.postRunnable(() -> {
            boolean imagesCleared = false;
            long clearedSize = 0;
            boolean allItemsClear = true;

            for (int a = 0; a < 7; a++) {
                if (clearTypes == null || a >= clearTypes.length || !clearTypes[a]) {
                    if (clearTypes != null && a < clearTypes.length) {
                        allItemsClear = false;
                    }
                    continue;
                }
                int type = -1;
                int documentsMusicType = 0;
                if (a == 0) {
                    type = FileLoader.MEDIA_DIR_IMAGE;
                    if (currentSizes != null && a < currentSizes.length) {
                        clearedSize += currentSizes[a];
                    }
                } else if (a == 1) {
                    type = FileLoader.MEDIA_DIR_VIDEO;
                    if (currentSizes != null && a < currentSizes.length) {
                        clearedSize += currentSizes[a];
                    }
                } else if (a == 2) {
                    type = FileLoader.MEDIA_DIR_DOCUMENT;
                    documentsMusicType = 1;
                    if (currentSizes != null && a < currentSizes.length) {
                        clearedSize += currentSizes[a];
                    }
                } else if (a == 3) {
                    type = FileLoader.MEDIA_DIR_DOCUMENT;
                    documentsMusicType = 2;
                    if (currentSizes != null && a < currentSizes.length) {
                        clearedSize += currentSizes[a];
                    }
                } else if (a == 4) {
                    type = FileLoader.MEDIA_DIR_AUDIO;
                    if (currentSizes != null && a < currentSizes.length) {
                        clearedSize += currentSizes[a];
                    }
                } else if (a == 5) {
                    type = 100;
                    if (currentSizes != null && a < currentSizes.length) {
                        clearedSize += currentSizes[a];
                    }
                } else if (a == 6) {
                    type = FileLoader.MEDIA_DIR_CACHE;
                    if (currentSizes != null && a < currentSizes.length) {
                        clearedSize += currentSizes[a];
                    }
                }
                if (type == -1) {
                    continue;
                }
                File file;
                if (type == 100) {
                    file = new File(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_CACHE), "acache");
                } else {
                    file = FileLoader.checkDirectory(type);
                }
                if (file != null) {
                    Utilities.clearDir(file.getAbsolutePath(), documentsMusicType, Long.MAX_VALUE, false);
                }
                if (type == FileLoader.MEDIA_DIR_IMAGE || type == FileLoader.MEDIA_DIR_VIDEO) {
                    int publicDirectoryType = type == FileLoader.MEDIA_DIR_IMAGE ? FileLoader.MEDIA_DIR_IMAGE_PUBLIC : FileLoader.MEDIA_DIR_VIDEO_PUBLIC;
                    file = FileLoader.checkDirectory(publicDirectoryType);
                    if (file != null) {
                        Utilities.clearDir(file.getAbsolutePath(), documentsMusicType, Long.MAX_VALUE, false);
                    }
                }
                if (type == FileLoader.MEDIA_DIR_DOCUMENT) {
                    file = FileLoader.checkDirectory(FileLoader.MEDIA_DIR_FILES);
                    if (file != null) {
                        Utilities.clearDir(file.getAbsolutePath(), documentsMusicType, Long.MAX_VALUE, false);
                    }
                }

                if (type == FileLoader.MEDIA_DIR_CACHE || type == FileLoader.MEDIA_DIR_IMAGE || type == 100) {
                    imagesCleared = true;
                }
            }

            long cacheSize = getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_CACHE), 0);
            long photoSize = getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_IMAGE), 0) + getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_IMAGE_PUBLIC), 0);
            long videoSize = getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_VIDEO), 0) + getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_VIDEO_PUBLIC), 0);
            long documentsSize = getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_DOCUMENT), 1) + getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_FILES), 1);
            long musicSize = getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_DOCUMENT), 2) + getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_FILES), 2);
            long stickersSize = getDirectorySize(new File(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_CACHE), "acache"), 0);
            long audioSize = getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_AUDIO), 0);
            long totalSize = cacheSize + videoSize + audioSize + photoSize + documentsSize + musicSize + stickersSize;

            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = stat.getBlockSizeLong();
            } else {
                blockSize = stat.getBlockSize();
            }
            long availableBlocks;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                availableBlocks = stat.getAvailableBlocksLong();
            } else {
                availableBlocks = stat.getAvailableBlocks();
            }
            long blocksTotal;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blocksTotal = stat.getBlockCountLong();
            } else {
                blocksTotal = stat.getBlockCount();
            }

            long totalDeviceSize = blocksTotal * blockSize;
            long totalDeviceFreeSize = availableBlocks * blockSize;

            if (allItemsClear) {
                FileLoader.getInstance(accountNum).clearFilePaths();
            }
            FileLoader.getInstance(accountNum).checkCurrentDownloadsFiles();

            final boolean finalImagesCleared = imagesCleared;
            final long finalClearedSize = clearedSize;
            final long finalCacheSize = cacheSize;
            final long finalPhotoSize = photoSize;
            final long finalVideoSize = videoSize;
            final long finalDocumentsSize = documentsSize;
            final long finalMusicSize = musicSize;
            final long finalStickersSize = stickersSize;
            final long finalAudioSize = audioSize;
            final long finalTotalSize = totalSize;
            final long finalTotalDeviceSize = totalDeviceSize;
            final long finalTotalDeviceFreeSize = totalDeviceFreeSize;

            AndroidUtilities.runOnUIThread(() -> {
                if (finalImagesCleared) {
                    ImageLoader.getInstance().clearMemory();
                }
                MediaDataController.getInstance(accountNum).ringtoneDataStore.checkRingtoneSoundsLoaded();
                MediaDataController.getInstance(accountNum).chekAllMedia(true);
                if (callback != null) {
                    callback.onCleaned(finalClearedSize, finalCacheSize, finalPhotoSize, finalVideoSize, finalDocumentsSize, finalMusicSize, finalStickersSize, finalAudioSize, finalTotalSize, finalTotalDeviceSize, finalTotalDeviceFreeSize, finalImagesCleared);
                }
            });
        });
    }
}
