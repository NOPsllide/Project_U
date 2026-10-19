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

    void toggleStickerSet(Object parentActivity, TLRPC.TL_messages_stickerSet pack, int installType, Object baseFragment, boolean showSettings, boolean showTooltip);

    void toggleStickerSets(ArrayList<TLRPC.StickerSet> sets, int currentType, int action, Object fragment, boolean showProgress);

    void getArchivedStickers(int type, long offsetId, int limit, ArchivedStickersCallback callback);

    boolean isStickerPackInstalled(long setId);

    boolean isStickerPackInstalled(long setId, boolean countForced);

    void checkImageStickers();

    void checkStickers(int type);

    void checkFeaturedEmoji();

    ArrayList<TLRPC.TL_messages_stickerSet> getImageStickerSets();

    ArrayList<TLRPC.TL_messages_stickerSet> getStickerSets(int type);

    ArrayList<TLRPC.TL_messages_stickerSet> getFilteredStickerSets(int type);

    ArrayList<TLRPC.StickerSetCovered> getFeaturedEmojiSets();

    List<TLRPC.StickerSetCovered> getFeaturedSets(int currentType);

    int getArchivedStickersCount(int type);

    void calcNewHash(int type);

    void reorderStickerSets(int type, ArrayList<Long> order, Runnable onComplete);

    void sortStickerSets(int type, List<TLRPC.TL_messages_stickerSet> orderList);

    String getDoubleTapReaction();

    TLRPC.TL_availableReaction getReaction(String reaction);

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
