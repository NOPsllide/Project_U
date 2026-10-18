package org.telegram.core.presenter;

import java.util.ArrayList;
import java.util.HashMap;

import org.telegram.core.data.repository.MediaRepository;
import org.telegram.core.data.repository.MediaRepositoryImpl;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.ui.PhotoAlbumPickerActivity;

public class PhotoAlbumPickerPresenter {

    public interface PhotoAlbumPickerView {
        void showAlbums(ArrayList<MediaController.AlbumEntry> albums);
        void hideLoading();
        void closeChats();
    }

    private PhotoAlbumPickerView view;
    private final MediaRepository mediaRepository;

    public PhotoAlbumPickerPresenter() {
        this(new MediaRepositoryImpl());
    }

    public PhotoAlbumPickerPresenter(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    public void attachView(PhotoAlbumPickerView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public ArrayList<MediaController.AlbumEntry> loadInitialAlbums(int selectPhotoType, boolean allowSearchImages, int classGuid) {
        ArrayList<MediaController.AlbumEntry> albums = mediaRepository.getInitialAlbums(selectPhotoType, allowSearchImages);
        mediaRepository.loadGalleryPhotosAlbums(classGuid);
        return albums;
    }

    public void onAlbumsLoaded(int classGuid, int eventClassGuid, int selectPhotoType, boolean allowSearchImages, Object[] args) {
        if (classGuid == eventClassGuid && view != null) {
            ArrayList<MediaController.AlbumEntry> albums = mediaRepository.parseLoadedAlbums(selectPhotoType, allowSearchImages, args);
            view.showAlbums(albums);
            view.hideLoading();
        }
    }

    public void sendPhotos(HashMap<Object, Object> photos, ArrayList<Object> order, boolean notify, int scheduleDate, PhotoAlbumPickerActivity.PhotoAlbumPickerActivityDelegate delegate) {
        if (photos.isEmpty() || delegate == null) {
            return;
        }
        ArrayList<SendMessagesHelper.SendingMediaInfo> media = mediaRepository.buildSendingMedia(photos, order);
        delegate.didSelectPhotos(media, notify, scheduleDate);
    }
}
