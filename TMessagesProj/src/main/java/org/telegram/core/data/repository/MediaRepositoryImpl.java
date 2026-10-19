package org.telegram.core.data.repository;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.PhotoAlbumPickerActivity;

public class MediaRepositoryImpl implements MediaRepository {

    private int currentAccount;

    public MediaRepositoryImpl() {
        this(UserConfig.selectedAccount);
    }

    public MediaRepositoryImpl(int currentAccount) {
        this.currentAccount = currentAccount;
    }

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

    @Override
    public ArrayList<String> loadRecentSearches() {
        ArrayList<String> recentSearches = new ArrayList<>();
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("web_recent_search", Activity.MODE_PRIVATE);
        int count = preferences.getInt("count", 0);
        for (int a = 0; a < count; a++) {
            String str = preferences.getString("recent" + a, null);
            if (str == null) {
                break;
            }
            recentSearches.add(str);
        }
        return recentSearches;
    }

    @Override
    public void saveRecentSearches(ArrayList<String> searches) {
        SharedPreferences.Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("web_recent_search", Activity.MODE_PRIVATE).edit();
        editor.clear();
        editor.putInt("count", searches.size());
        for (int a = 0, N = searches.size(); a < N; a++) {
            editor.putString("recent" + a, searches.get(a));
        }
        editor.commit();
    }

    @Override
    public void cancelRequest(int reqId) {
        if (reqId != 0) {
            ConnectionsManager.getInstance(currentAccount).cancelRequest(reqId, true);
        }
    }

    @Override
    public void resolveSearchBot(boolean gif, BotUserCallback callback) {
        TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
        req.username = gif ? MessagesController.getInstance(currentAccount).gifSearchBot : MessagesController.getInstance(currentAccount).imageSearchBot;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> {
            if (response != null) {
                AndroidUtilities.runOnUIThread(() -> {
                    TLRPC.TL_contacts_resolvedPeer res = (TLRPC.TL_contacts_resolvedPeer) response;
                    MessagesController.getInstance(currentAccount).putUsers(res.users, false);
                    MessagesController.getInstance(currentAccount).putChats(res.chats, false);
                    MessagesStorage.getInstance(currentAccount).putUsersAndChats(res.users, res.chats, true, true);
                    TLObject object = MessagesController.getInstance(currentAccount).getUserOrChat(gif ? MessagesController.getInstance(currentAccount).gifSearchBot : MessagesController.getInstance(currentAccount).imageSearchBot);
                    if (object instanceof TLRPC.User && callback != null) {
                        callback.onBotResolved((TLRPC.User) object);
                    }
                });
            } else if (callback != null) {
                AndroidUtilities.runOnUIThread(() -> callback.onBotResolved(null));
            }
        });
    }

    @Override
    public int searchBotResults(boolean gif, String query, String offset, long dialogId, int classGuid, BotResultsCallback callback) {
        TLObject object = MessagesController.getInstance(currentAccount).getUserOrChat(gif ? MessagesController.getInstance(currentAccount).gifSearchBot : MessagesController.getInstance(currentAccount).imageSearchBot);
        if (!(object instanceof TLRPC.User)) {
            return 0;
        }
        TLRPC.User user = (TLRPC.User) object;

        TLRPC.TL_messages_getInlineBotResults req = new TLRPC.TL_messages_getInlineBotResults();
        req.query = query == null ? "" : query;
        req.bot = MessagesController.getInstance(currentAccount).getInputUser(user);
        req.offset = offset != null ? offset : "";

        if (dialogId != 0) {
            if (DialogObject.isEncryptedDialog(dialogId)) {
                req.peer = new TLRPC.TL_inputPeerEmpty();
            } else {
                req.peer = MessagesController.getInstance(currentAccount).getInputPeer(dialogId);
            }
        } else {
            req.peer = new TLRPC.TL_inputPeerEmpty();
        }

        int reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (callback != null) {
                callback.onResultsLoaded(response instanceof TLRPC.messages_BotResults ? (TLRPC.messages_BotResults) response : null, user.username);
            }
        }));
        if (classGuid != 0) {
            ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(reqId, classGuid);
        }
        return reqId;
    }

    @Override
    public int getMaxCaptionLength() {
        return MessagesController.getInstance(currentAccount).maxCaptionLength;
    }
}
