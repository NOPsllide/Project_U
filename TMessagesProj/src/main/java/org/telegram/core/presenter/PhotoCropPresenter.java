package org.telegram.core.presenter;

import android.graphics.Bitmap;
import android.net.Uri;

import org.telegram.core.data.repository.MediaRepository;
import org.telegram.core.data.repository.MediaRepositoryImpl;

import java.io.File;

public class PhotoCropPresenter {

    public interface PhotoCropView {
        void onBitmapLoaded(Bitmap bitmap);
    }

    private final MediaRepository repository;
    private PhotoCropView view;

    public PhotoCropPresenter() {
        this(new MediaRepositoryImpl());
    }

    public PhotoCropPresenter(MediaRepository repository) {
        this.repository = repository;
    }

    public void attachView(PhotoCropView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public Bitmap loadBitmap(String photoPath, Uri photoUri, int size) {
        if (photoPath == null && photoUri == null) {
            return null;
        }
        if (photoPath != null) {
            File f = new File(photoPath);
            if (!f.exists()) {
                return null;
            }
        }
        Bitmap bitmap = repository.loadBitmap(photoPath, photoUri, size, size, true);
        if (bitmap != null && view != null) {
            view.onBitmapLoaded(bitmap);
        }
        return bitmap;
    }

    public void releaseImage(String bitmapKey, Bitmap imageToCrop, boolean sameBitmap) {
        if (bitmapKey != null) {
            if (repository.releaseBitmapCache(bitmapKey)) {
                bitmapKey = null;
            }
        }
        if (bitmapKey == null && imageToCrop != null && !sameBitmap) {
            repository.recycleBitmap(imageToCrop);
        }
    }
}
