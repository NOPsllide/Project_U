package org.telegram.core.data.repository;

import org.telegram.tgnet.TLRPC;
import java.util.ArrayList;

public interface StickerRepository {

    ArrayList<TLRPC.StickerSetCovered> getFeaturedStickerSets();

    ArrayList<Long> getUnreadStickerSets();

    void checkFeaturedStickers();

    void markFeaturedStickersAsRead(boolean query, boolean server);

    void toggleStickerSet(Object parentActivity, TLRPC.StickerSetCovered pack, int installType, Object baseFragment, boolean showSettings, boolean showTooltip);

    void getArchivedStickers(int type, long offsetId, int limit, ArchivedStickersCallback callback);

    boolean isStickerPackInstalled(long setId);

    interface ArchivedStickersCallback {
        void onSuccess(TLRPC.TL_messages_archivedStickers response);
        void onError();
    }
}
