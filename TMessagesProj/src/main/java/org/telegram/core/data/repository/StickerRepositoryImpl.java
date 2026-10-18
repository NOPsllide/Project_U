package org.telegram.core.data.repository;

import org.telegram.messenger.MediaDataController;
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
}
