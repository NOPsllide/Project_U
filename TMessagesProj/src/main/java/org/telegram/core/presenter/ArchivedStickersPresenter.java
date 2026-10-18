package org.telegram.core.presenter;

import androidx.collection.LongSparseArray;
import org.telegram.core.data.repository.StickerRepository;
import org.telegram.tgnet.TLRPC;
import java.util.ArrayList;
import java.util.List;

public class ArchivedStickersPresenter {

    public interface View {
        void showProgress();
        void hideProgress();
        void updateRows();
        void notifyDataSetChanged();
        void notifyItemRangeInserted(int position, int count);
    }

    private final StickerRepository stickerRepository;
    private final int currentType;
    private View view;

    private final ArrayList<TLRPC.StickerSetCovered> sets = new ArrayList<>();
    private final LongSparseArray<TLRPC.StickerSetCovered> installingStickerSets = new LongSparseArray<>();
    private boolean loadingStickers;
    private boolean endReached;
    private boolean firstLoaded;

    public ArchivedStickersPresenter(StickerRepository stickerRepository, int type) {
        this.stickerRepository = stickerRepository;
        this.currentType = type;
    }

    public void attachView(View view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void onCreate() {
        loadStickers();
    }

    public void loadStickers() {
        if (loadingStickers || endReached) {
            return;
        }

        loadingStickers = true;

        if (view != null && !firstLoaded) {
            view.showProgress();
        }

        if (view != null) {
            view.notifyDataSetChanged();
        }

        long offsetId = sets.isEmpty() ? 0 : sets.get(sets.size() - 1).set.id;

        stickerRepository.getArchivedStickers(currentType, offsetId, 15, new StickerRepository.ArchivedStickersCallback() {
            @Override
            public void onSuccess(TLRPC.TL_messages_archivedStickers response) {
                processResponse(response);
            }

            @Override
            public void onError() {
                loadingStickers = false;
                if (view != null) {
                    view.hideProgress();
                }
            }
        });
    }

    private void processResponse(TLRPC.TL_messages_archivedStickers response) {
        sets.addAll(response.sets);
        endReached = response.sets.size() != 15;
        loadingStickers = false;
        firstLoaded = true;

        if (view != null) {
            view.hideProgress();
            view.updateRows();
            view.notifyDataSetChanged();
        }
    }

    public void addNewArchivedStickers(List<TLRPC.StickerSetCovered> newSets, int stickersStartRow) {
        List<TLRPC.StickerSetCovered> filtered = new ArrayList<>(newSets);
        
        for (int i = filtered.size() - 1; i >= 0; i--) {
            for (int j = 0; j < sets.size(); j++) {
                if (sets.get(j).set.id == filtered.get(i).set.id) {
                    filtered.remove(i);
                    break;
                }
            }
        }

        if (!filtered.isEmpty()) {
            sets.addAll(0, filtered);
            if (view != null) {
                view.updateRows();
                view.notifyItemRangeInserted(stickersStartRow, filtered.size());
            }
        }
    }

    public ArrayList<TLRPC.StickerSetCovered> getSets() {
        return sets;
    }

    public LongSparseArray<TLRPC.StickerSetCovered> getInstallingStickerSets() {
        return installingStickerSets;
    }

    public void addInstallingStickerSet(long id, TLRPC.StickerSetCovered set) {
        installingStickerSets.put(id, set);
    }

    public void removeInstallingStickerSet(long id) {
        installingStickerSets.remove(id);
    }

    public boolean isStickerPackInstalled(long setId) {
        return stickerRepository.isStickerPackInstalled(setId);
    }

    public void toggleStickerSet(Object parentActivity, TLRPC.StickerSetCovered pack, int installType, Object baseFragment) {
        stickerRepository.toggleStickerSet(parentActivity, pack, installType, baseFragment, false, false);
    }

    public boolean isLoadingStickers() {
        return loadingStickers;
    }

    public boolean isEndReached() {
        return endReached;
    }

    public int getCurrentType() {
        return currentType;
    }
}
