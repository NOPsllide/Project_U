package org.telegram.core.data.repository;

import android.graphics.Bitmap;
import android.net.Uri;

import org.telegram.messenger.ImageLoader;

public class MediaRepositoryImpl implements MediaRepository {

    @Override
    public Bitmap loadBitmap(String path, Uri uri, float maxWidth, float maxHeight, boolean useCache) {
        return ImageLoader.loadBitmap(path, uri, maxWidth, maxHeight, useCache);
    }

    @Override
    public boolean releaseBitmapCache(String bitmapKey) {
        if (bitmapKey == null) {
            return false;
        }
        if (ImageLoader.getInstance().decrementUseCount(bitmapKey) && !ImageLoader.getInstance().isInMemCache(bitmapKey, false)) {
            return true;
        }
        return false;
    }

    @Override
    public void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }
}
