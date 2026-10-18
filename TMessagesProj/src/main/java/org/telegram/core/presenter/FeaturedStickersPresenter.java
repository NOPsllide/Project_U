package org.telegram.core.presenter;

import android.util.LongSparseArray;
import org.telegram.core.data.repository.StickerRepository;
import org.telegram.tgnet.TLRPC;
import java.util.ArrayList;

public class FeaturedStickersPresenter {

    public interface View {
        void updateRows();
        void notifyDataSetChanged();
        void notifyItemRangeChanged(int first, int count);
    }

    private final StickerRepository stickerRepository;
    private final int accountNum;
    private View view;
    private ArrayList<Long> unreadStickers = null;
    private LongSparseArray<TLRPC.StickerSetCovered> installingStickerSets = new LongSparseArray<>();

    public FeaturedStickersPresenter(StickerRepository stickerRepository, int accountNum) {
        this.stickerRepository = stickerRepository;
        this.accountNum = accountNum;
    }

    public void attachView(View view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void onCreate() {
        stickerRepository.checkFeaturedStickers();
        ArrayList<Long> arrayList = stickerRepository.getUnreadStickerSets();
        if (arrayList != null) {
            unreadStickers = new ArrayList<>(arrayList);
        }
        if (view != null) {
            view.updateRows();
        }
    }

    public void onFeaturedStickersLoaded() {
        if (unreadStickers == null) {
            unreadStickers = stickerRepository.getUnreadStickerSets();
        }
        if (view != null) {
            view.updateRows();
        }
    }

    public void onStickersLoaded() {
        if (view != null) {
            view.notifyDataSetChanged();
        }
    }

    public void updateVisibleRange(int first, int last) {
        if (view != null && first != -1 && last != -1) {
            view.notifyItemRangeChanged(first, last - first + 1);
        }
    }

    public void markAsRead() {
        stickerRepository.markFeaturedStickersAsRead(false, true);
    }

    public ArrayList<Long> getUnreadStickers() {
        return unreadStickers;
    }

    public LongSparseArray<TLRPC.StickerSetCovered> getInstallingStickerSets() {
        return installingStickerSets;
    }

    public void addInstallingStickerSet(long id, TLRPC.StickerSetCovered pack) {
        installingStickerSets.put(id, pack);
    }

    public void removeInstallingStickerSet(long id) {
        installingStickerSets.remove(id);
    }

    public void toggleStickerSet(Object parentActivity, TLRPC.StickerSetCovered pack, Object baseFragment) {
        if (installingStickerSets.indexOfKey(pack.set.id) >= 0) {
            return;
        }
        installingStickerSets.put(pack.set.id, pack);
        stickerRepository.toggleStickerSet(parentActivity, pack, 2, baseFragment, false, false);
    }

    public ArrayList<TLRPC.StickerSetCovered> getFeaturedStickerSets() {
        return stickerRepository.getFeaturedStickerSets();
    }
}
