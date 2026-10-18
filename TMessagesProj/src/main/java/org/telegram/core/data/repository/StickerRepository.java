package org.telegram.core.data.repository;

import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import java.util.ArrayList;
import java.util.List;

public interface StickerRepository {

    ArrayList<TLRPC.StickerSetCovered> getFeaturedStickerSets();

    ArrayList<Long> getUnreadStickerSets();

    void checkFeaturedStickers();

    void markFeaturedStickersAsRead(boolean query, boolean server);

    void toggleStickerSet(Object parentActivity, TLRPC.StickerSetCovered pack, int installType, Object baseFragment, boolean showSettings, boolean showTooltip);

    void getArchivedStickers(int type, long offsetId, int limit, ArchivedStickersCallback callback);

    boolean isStickerPackInstalled(long setId);

    void checkImageStickers();

    ArrayList<TLRPC.TL_messages_stickerSet> getImageStickerSets();

    TLRPC.TL_messages_stickerSet getGroupStickerSetById(TLRPC.StickerSet stickerset);

    void putGroupStickerSet(TLRPC.TL_messages_stickerSet stickerSet);

    int searchStickerSets(String query, SearchStickersCallback callback);

    void cancelRequest(int reqId);

    void setGroupStickers(long chatId, TLRPC.ChatFull info, TLRPC.TL_messages_stickerSet selectedStickerSet, boolean removeStickerSet, SetGroupStickersCallback callback);

    void openStickersBot(BaseFragment fragment);

    interface ArchivedStickersCallback {
        void onSuccess(TLRPC.TL_messages_archivedStickers response);
        void onError();
    }

    interface SearchStickersCallback {
        void onSuccess(List<TLRPC.TL_messages_stickerSet> searchEntries, List<TLRPC.TL_messages_stickerSet> localEntries);
        void onError();
    }

    interface SetGroupStickersCallback {
        void onSuccess();
        void onError(String error);
    }
}
