package org.telegram.core.data.repository;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MediaDataController;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import java.util.ArrayList;

public class StickerRepositoryImpl implements StickerRepository {

    private final int accountNum;

    public StickerRepositoryImpl(int accountNum) {
        this.accountNum = accountNum;
    }

    @Override
    public ArrayList<TLRPC.StickerSetCovered> getFeaturedStickerSets() {
        return MediaDataController.getInstance(accountNum).getFeaturedStickerSets();
    }

    @Override
    public ArrayList<Long> getUnreadStickerSets() {
        return MediaDataController.getInstance(accountNum).getUnreadStickerSets();
    }

    @Override
    public void checkFeaturedStickers() {
        MediaDataController.getInstance(accountNum).checkFeaturedStickers();
    }

    @Override
    public void markFeaturedStickersAsRead(boolean query, boolean server) {
        MediaDataController.getInstance(accountNum).markFeaturedStickersAsRead(query, server);
    }

    @Override
    public void toggleStickerSet(Object parentActivity, TLRPC.StickerSetCovered pack, int installType, Object baseFragment, boolean showSettings, boolean showTooltip) {
        MediaDataController.getInstance(accountNum).toggleStickerSet(parentActivity, pack, installType, baseFragment, showSettings, showTooltip);
    }

    @Override
    public void getArchivedStickers(int type, long offsetId, int limit, ArchivedStickersCallback callback) {
        TLRPC.TL_messages_getArchivedStickers req = new TLRPC.TL_messages_getArchivedStickers();
        req.offset_id = offsetId;
        req.limit = limit;
        req.masks = type == MediaDataController.TYPE_MASK;
        req.emojis = type == MediaDataController.TYPE_EMOJIPACKS;

        ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> 
            AndroidUtilities.runOnUIThread(() -> {
                if (error == null && callback != null) {
                    callback.onSuccess((TLRPC.TL_messages_archivedStickers) response);
                } else if (callback != null) {
                    callback.onError();
                }
            })
        );
    }

    @Override
    public boolean isStickerPackInstalled(long setId) {
        return MediaDataController.getInstance(accountNum).isStickerPackInstalled(setId);
    }
}
