package org.telegram.core.presenter;

import java.util.ArrayList;
import java.util.HashMap;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.MediaController;
import org.telegram.tgnet.TLRPC;
import org.telegram.core.data.repository.MediaRepository;

public class PhotoPickerPresenter {

    public interface PhotoPickerView {
        void onRecentSearchesLoaded(ArrayList<String> searches);
        void onSearchResultsLoaded(ArrayList<MediaController.SearchImage> newResults, String nextOffset, boolean endReached, int token);
        void onSearchLoadingStateChanged(boolean loading);
    }

    private final MediaRepository mediaRepository;
    private final int currentAccount;
    private PhotoPickerView view;

    private boolean searching;
    private boolean searchingUser;
    private int imageReqId;
    private int lastSearchToken;
    private String lastSearchImageString;

    public PhotoPickerPresenter(MediaRepository mediaRepository, int currentAccount) {
        this.mediaRepository = mediaRepository;
        this.currentAccount = currentAccount;
    }

    public void attachView(PhotoPickerView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
        cancelSearch();
    }

    public int getMaxCaptionLength() {
        return mediaRepository.getMaxCaptionLength();
    }

    public ArrayList<String> loadRecentSearches() {
        return mediaRepository.loadRecentSearches();
    }

    public void saveRecentSearches(ArrayList<String> searches) {
        mediaRepository.saveRecentSearches(searches);
    }

    public void cancelSearch() {
        if (imageReqId != 0) {
            mediaRepository.cancelRequest(imageReqId);
            imageReqId = 0;
        }
        searching = false;
    }

    public boolean isSearching() {
        return searching;
    }

    public void searchImages(boolean gif, String query, String offset, long dialogId, int classGuid, int itemSize, HashMap<String, MediaController.SearchImage> existingKeys) {
        if (searching) {
            cancelSearch();
        }
        lastSearchImageString = query;
        searching = true;
        if (view != null) {
            view.onSearchLoadingStateChanged(true);
        }

        final int token = ++lastSearchToken;
        int reqId = mediaRepository.searchBotResults(gif, query, offset, dialogId, classGuid, (res, botUsername) -> {
            if (token != lastSearchToken) {
                return;
            }
            searching = false;
            ArrayList<MediaController.SearchImage> addedList = new ArrayList<>();
            String nextOffset = null;
            boolean endReached = true;

            if (res != null) {
                nextOffset = res.next_offset;
                for (int a = 0, count = res.results.size(); a < count; a++) {
                    TLRPC.BotInlineResult result = res.results.get(a);
                    if (!gif && !"photo".equals(result.type) || gif && !"gif".equals(result.type)) {
                        continue;
                    }
                    if (existingKeys != null && existingKeys.containsKey(result.id)) {
                        continue;
                    }

                    MediaController.SearchImage image = new MediaController.SearchImage();

                    if (gif && result.document != null) {
                        for (int b = 0; b < result.document.attributes.size(); b++) {
                            TLRPC.DocumentAttribute attribute = result.document.attributes.get(b);
                            if (attribute instanceof TLRPC.TL_documentAttributeImageSize || attribute instanceof TLRPC.TL_documentAttributeVideo) {
                                image.width = attribute.w;
                                image.height = attribute.h;
                                break;
                            }
                        }
                        image.document = result.document;
                        image.size = 0;
                        if (result.photo != null) {
                            TLRPC.PhotoSize size = FileLoader.getClosestPhotoSizeWithSize(result.photo.sizes, itemSize, true);
                            if (size != null) {
                                result.document.thumbs.add(size);
                                result.document.flags |= 1;
                            }
                        }
                    } else if (!gif && result.photo != null) {
                        TLRPC.PhotoSize size = FileLoader.getClosestPhotoSizeWithSize(result.photo.sizes, AndroidUtilities.getPhotoSize());
                        TLRPC.PhotoSize size2 = FileLoader.getClosestPhotoSizeWithSize(result.photo.sizes, 320);
                        if (size == null) {
                            continue;
                        }
                        image.width = size.w;
                        image.height = size.h;
                        image.photoSize = size;
                        image.photo = result.photo;
                        image.size = size.size;
                        image.thumbPhotoSize = size2;
                    } else {
                        if (result.content == null) {
                            continue;
                        }
                        for (int b = 0; b < result.content.attributes.size(); b++) {
                            TLRPC.DocumentAttribute attribute = result.content.attributes.get(b);
                            if (attribute instanceof TLRPC.TL_documentAttributeImageSize) {
                                image.width = attribute.w;
                                image.height = attribute.h;
                            }
                        }
                        if (result.thumb != null) {
                            image.thumbUrl = result.thumb.url;
                        } else {
                            image.thumbUrl = null;
                        }
                        image.imageUrl = result.content.url;
                        image.size = gif ? 0 : result.content.size;
                    }

                    image.id = result.id;
                    image.type = gif ? 1 : 0;
                    image.inlineResult = result;
                    image.params = new HashMap<>();
                    image.params.put("id", result.id);
                    image.params.put("query_id", "" + res.query_id);
                    image.params.put("bot_name", botUsername);

                    addedList.add(image);
                    if (existingKeys != null) {
                        existingKeys.put(image.id, image);
                    }
                }
                endReached = addedList.isEmpty() || nextOffset == null;
            }

            if (view != null) {
                view.onSearchResultsLoaded(addedList, nextOffset, endReached, token);
                view.onSearchLoadingStateChanged(false);
            }
        });

        if (reqId == 0) {
            resolveBotUserAndRetry(gif, dialogId, classGuid, itemSize, existingKeys);
        } else {
            imageReqId = reqId;
        }
    }

    private void resolveBotUserAndRetry(boolean gif, long dialogId, int classGuid, int itemSize, HashMap<String, MediaController.SearchImage> existingKeys) {
        if (searchingUser) {
            return;
        }
        searchingUser = true;
        mediaRepository.resolveSearchBot(gif, botUser -> {
            searchingUser = false;
            if (botUser != null) {
                String str = lastSearchImageString;
                lastSearchImageString = null;
                searchImages(gif, str, "", dialogId, classGuid, itemSize, existingKeys);
            } else {
                searching = false;
                if (view != null) {
                    view.onSearchLoadingStateChanged(false);
                }
            }
        });
    }
}
