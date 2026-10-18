package org.telegram.core.data.repository;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;

import org.telegram.messenger.MediaController;
import org.telegram.messenger.SendMessagesHelper;

public interface MediaRepository {

    Bitmap loadBitmap(String path, Uri uri, float maxWidth, float maxHeight, boolean useCache);

    boolean releaseBitmapCache(String bitmapKey);

    void recycleBitmap(Bitmap bitmap);

    ArrayList<MediaController.AlbumEntry> getInitialAlbums(int selectPhotoType, boolean allowSearchImages);

    void loadGalleryPhotosAlbums(int classGuid);

    ArrayList<MediaController.AlbumEntry> parseLoadedAlbums(int selectPhotoType, boolean allowSearchImages, Object[] args);

    ArrayList<SendMessagesHelper.SendingMediaInfo> buildSendingMedia(HashMap<Object, Object> photos, ArrayList<Object> order);
}
