package org.telegram.core.presenter;

import org.telegram.core.data.repository.StickerRepository;
import org.telegram.messenger.MediaDataController;
import org.telegram.tgnet.TLRPC;
import java.util.ArrayList;
import java.util.List;

public class StickersPresenter {

    public interface StickersView {
        void onStickersLoaded(boolean updateEmojiPacks);
        void onFeaturedStickersLoaded();
        void onArchivedCountLoaded();
        void updateMasksRow();
    }

    private final StickerRepository stickerRepository;
    private final int accountNum;
    private final int currentType;
    private StickersView view;

    public StickersPresenter(StickerRepository stickerRepository, int accountNum, int currentType) {
        this.stickerRepository = stickerRepository;
        this.accountNum = accountNum;
        this.currentType = currentType;
    }

    public void attachView(StickersView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void loadInitialData() {
        stickerRepository.checkStickers(currentType);
        if (currentType == MediaDataController.TYPE_IMAGE) {
            stickerRepository.checkFeaturedStickers();
            stickerRepository.checkStickers(MediaDataController.TYPE_MASK);
            stickerRepository.checkStickers(MediaDataController.TYPE_EMOJIPACKS);
        } else if (currentType == MediaDataController.TYPE_FEATURED_EMOJIPACKS) {
            stickerRepository.checkFeaturedEmoji();
        }
    }

    public List<TLRPC.StickerSetCovered> getFeaturedSets() {
        return stickerRepository.getFeaturedSets(currentType);
    }

    public ArrayList<TLRPC.TL_messages_stickerSet> getStickerSets(int type) {
        return stickerRepository.getStickerSets(type);
    }

    public ArrayList<TLRPC.TL_messages_stickerSet> getFilteredStickerSets(int type) {
        return stickerRepository.getFilteredStickerSets(type);
    }

    public ArrayList<TLRPC.StickerSetCovered> getFeaturedEmojiSets() {
        return stickerRepository.getFeaturedEmojiSets();
    }

    public int getArchivedStickersCount(int type) {
        return stickerRepository.getArchivedStickersCount(type);
    }

    public boolean isStickerPackInstalled(long id, boolean unarchive) {
        return stickerRepository.isStickerPackInstalled(id, unarchive);
    }

    public boolean isStickerPackInstalled(long id) {
        return stickerRepository.isStickerPackInstalled(id);
    }

    public void markFeaturedStickersAsRead(boolean query, boolean server) {
        stickerRepository.markFeaturedStickersAsRead(query, server);
    }

    public void sendReorder(ArrayList<Long> order, Runnable onComplete) {
        stickerRepository.reorderStickerSets(currentType, order, onComplete);
    }

    public void sortStickerSets(int type, List<TLRPC.TL_messages_stickerSet> orderList) {
        stickerRepository.sortStickerSets(type, orderList);
    }

    public void toggleStickerSet(Object parentActivity, TLRPC.StickerSetCovered pack, int installType, Object baseFragment, boolean showSettings, boolean showTooltip) {
        stickerRepository.toggleStickerSet(parentActivity, pack, installType, baseFragment, showSettings, showTooltip);
    }

    public void toggleStickerSet(Object parentActivity, TLRPC.TL_messages_stickerSet pack, int installType, Object baseFragment, boolean showSettings, boolean showTooltip) {
        stickerRepository.toggleStickerSet(parentActivity, pack, installType, baseFragment, showSettings, showTooltip);
    }

    public void toggleStickerSets(ArrayList<TLRPC.StickerSet> sets, int currentType, int action, Object fragment, boolean showProgress) {
        stickerRepository.toggleStickerSets(sets, currentType, action, fragment, showProgress);
    }

    public String getDoubleTapReaction() {
        return stickerRepository.getDoubleTapReaction();
    }

    public TLRPC.TL_availableReaction getReaction(String reaction) {
        return stickerRepository.getReaction(reaction);
    }

    public void onStickersDidLoad(int type, boolean updateEmojiPacks) {
        if (view == null) {
            return;
        }
        if (type == currentType) {
            view.onStickersLoaded(updateEmojiPacks);
        } else if (currentType == MediaDataController.TYPE_IMAGE && type == MediaDataController.TYPE_MASK) {
            view.updateMasksRow();
        }
    }

    public void onFeaturedStickersDidLoad() {
        if (view != null) {
            view.onFeaturedStickersLoaded();
        }
    }

    public void onFeaturedEmojiDidLoad() {
        if (view != null) {
            view.onFeaturedStickersLoaded();
        }
    }

    public void onArchivedStickersCountDidLoad(int type) {
        if (view != null && type == currentType) {
            view.onArchivedCountLoaded();
        }
    }
}
