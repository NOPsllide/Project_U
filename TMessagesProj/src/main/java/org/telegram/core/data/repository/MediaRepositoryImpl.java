package org.telegram.core.data.repository;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;

import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.ui.PhotoAlbumPickerActivity;

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

    @Override
    public ArrayList<MediaController.AlbumEntry> getInitialAlbums(int selectPhotoType, boolean allowSearchImages) {
        if (selectPhotoType == PhotoAlbumPickerActivity.SELECT_TYPE_AVATAR || selectPhotoType == PhotoAlbumPickerActivity.SELECT_TYPE_WALLPAPER || selectPhotoType == PhotoAlbumPickerActivity.SELECT_TYPE_QR || !allowSearchImages) {
            return MediaController.allPhotoAlbums;
        } else {
            return MediaController.allMediaAlbums;
        }
    }

    @Override
    public void loadGalleryPhotosAlbums(int classGuid) {
        MediaController.loadGalleryPhotosAlbums(classGuid);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<MediaController.AlbumEntry> parseLoadedAlbums(int selectPhotoType, boolean allowSearchImages, Object[] args) {
        if (selectPhotoType == PhotoAlbumPickerActivity.SELECT_TYPE_AVATAR || selectPhotoType == PhotoAlbumPickerActivity.SELECT_TYPE_WALLPAPER || selectPhotoType == PhotoAlbumPickerActivity.SELECT_TYPE_QR || !allowSearchImages) {
            return (ArrayList<MediaController.AlbumEntry>) args[2];
        } else {
            return (ArrayList<MediaController.AlbumEntry>) args[1];
        }
    }

    @Override
    public ArrayList<SendMessagesHelper.SendingMediaInfo> buildSendingMedia(HashMap<Object, Object> photos, ArrayList<Object> order) {
        ArrayList<SendMessagesHelper.SendingMediaInfo> media = new ArrayList<>();
        for (int a = 0; a < order.size(); a++) {
            Object object = photos.get(order.get(a));
            SendMessagesHelper.SendingMediaInfo info = new SendMessagesHelper.SendingMediaInfo();
            media.add(info);
            if (object instanceof MediaController.PhotoEntry) {
                MediaController.PhotoEntry photoEntry = (MediaController.PhotoEntry) object;
                if (photoEntry.imagePath != null) {
                    info.path = photoEntry.imagePath;
                } else {
                    info.path = photoEntry.path;
                }
                info.thumbPath = photoEntry.thumbPath;
                info.videoEditedInfo = photoEntry.editedInfo;
                info.isVideo = photoEntry.isVideo;
                info.caption = photoEntry.caption != null ? photoEntry.caption.toString() : null;
                info.entities = photoEntry.entities;
                info.masks = photoEntry.stickers;
                info.ttl = photoEntry.ttl;
            } else if (object instanceof MediaController.SearchImage) {
                MediaController.SearchImage searchImage = (MediaController.SearchImage) object;
                if (searchImage.imagePath != null) {
                    info.path = searchImage.imagePath;
                } else {
                    info.searchImage = searchImage;
                }
                info.thumbPath = searchImage.thumbPath;
                info.videoEditedInfo = searchImage.editedInfo;
                info.caption = searchImage.caption != null ? searchImage.caption.toString() : null;
                info.entities = searchImage.entities;
                info.masks = searchImage.stickers;
                info.ttl = searchImage.ttl;
                if (searchImage.inlineResult != null && searchImage.type == 1) {
                    info.inlineResult = searchImage.inlineResult;
                    info.params = searchImage.params;
                }
                searchImage.date = (int) (System.currentTimeMillis() / 1000);
            }
        }
        return media;
    }
}
