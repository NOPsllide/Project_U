package org.telegram.core.data.repository;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;

import org.telegram.messenger.MediaController;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.tgnet.TLRPC;

public interface MediaRepository {

    Bitmap loadBitmap(String path, Uri uri, float maxWidth, float maxHeight, boolean useCache);

    boolean releaseBitmapCache(String bitmapKey);

    void recycleBitmap(Bitmap bitmap);

    ArrayList<MediaController.AlbumEntry> getInitialAlbums(int selectPhotoType, boolean allowSearchImages);

    void loadGalleryPhotosAlbums(int classGuid);

    ArrayList<MediaController.AlbumEntry> parseLoadedAlbums(int selectPhotoType, boolean allowSearchImages, Object[] args);

    ArrayList<SendMessagesHelper.SendingMediaInfo> buildSendingMedia(HashMap<Object, Object> photos, ArrayList<Object> order);

    ArrayList<String> loadRecentSearches();

    void saveRecentSearches(ArrayList<String> searches);

    void cancelRequest(int reqId);

    void resolveSearchBot(boolean gif, BotUserCallback callback);

    int searchBotResults(boolean gif, String query, String offset, long dialogId, int classGuid, BotResultsCallback callback);

    int getMaxCaptionLength();

    interface BotUserCallback {
        void onBotResolved(TLRPC.User botUser);
    }

    interface BotResultsCallback {
        void onResultsLoaded(TLRPC.messages_BotResults results, String botUsername);
    }
}
