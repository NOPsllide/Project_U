package org.telegram.core.presenter;

import org.telegram.core.data.repository.SettingsRepository;
import org.telegram.tgnet.TLRPC;

import java.util.List;

public class ReactionsDoubleTapPresenter {

    public interface View {
        void notifyDataSetChanged();
        void notifyItemRangeChanged(int position, int count);
        void updateRows();
        void showPremiumFeature();
    }

    private final SettingsRepository settingsRepository;
    private View view;

    private int previewRow;
    private int infoRow;
    private int reactionsStartRow = -1;
    private int premiumReactionRow = -1;
    private int rowCount;

    public ReactionsDoubleTapPresenter(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public void attachView(View view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void updateRows() {
        rowCount = 0;
        previewRow = rowCount++;
        infoRow = rowCount++;
        if (settingsRepository.isPremium()) {
            reactionsStartRow = -1;
            premiumReactionRow = rowCount++;
        } else {
            premiumReactionRow = -1;
            reactionsStartRow = rowCount;
        }
        if (view != null) {
            view.updateRows();
        }
    }

    public List<TLRPC.TL_availableReaction> getAvailableReactions() {
        return settingsRepository.getAvailableReactions();
    }

    public String getDoubleTapReaction() {
        return settingsRepository.getDoubleTapReaction();
    }

    public TLRPC.TL_availableReaction getReaction(String key) {
        return settingsRepository.getAvailableReaction(key);
    }

    public boolean isPremium() {
        return settingsRepository.isPremium();
    }

    public void onReactionClicked(TLRPC.TL_availableReaction react, boolean locked) {
        if (locked && !settingsRepository.isPremium()) {
            if (view != null) {
                view.showPremiumFeature();
            }
            return;
        }
        settingsRepository.setDoubleTapReaction(react.reaction);
        if (view != null) {
            view.notifyItemRangeChanged(0, getItemCount());
        }
    }

    public void selectDoubleTapReaction(String reaction) {
        settingsRepository.setDoubleTapReaction(reaction);
        if (view != null) {
            view.notifyItemRangeChanged(0, getItemCount());
        }
    }

    public int getItemCount() {
        List<TLRPC.TL_availableReaction> reactions = getAvailableReactions();
        int reactionsSize = reactions != null ? reactions.size() : 0;
        return rowCount + (premiumReactionRow < 0 ? reactionsSize : 0) + 1;
    }

    public void onReactionsDidLoad() {
        if (view != null) {
            view.notifyDataSetChanged();
        }
    }

    public void onPremiumStatusChanged() {
        updateRows();
        if (view != null) {
            view.notifyDataSetChanged();
        }
    }

    public int getPreviewRow() {
        return previewRow;
    }

    public int getInfoRow() {
        return infoRow;
    }

    public int getReactionsStartRow() {
        return reactionsStartRow;
    }

    public int getPremiumReactionRow() {
        return premiumReactionRow;
    }

    public int getRowCount() {
        return rowCount;
    }
}
