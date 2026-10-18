package org.telegram.core.data.repository;

import android.graphics.Bitmap;
import android.net.Uri;

public interface MediaRepository {

    Bitmap loadBitmap(String path, Uri uri, float maxWidth, float maxHeight, boolean useCache);

    boolean releaseBitmapCache(String bitmapKey);

    void recycleBitmap(Bitmap bitmap);
}
