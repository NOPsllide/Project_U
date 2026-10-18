package org.telegram.core.presenter;

import android.text.TextUtils;

import org.telegram.core.data.repository.StickerRepository;
import org.telegram.core.data.repository.StickerRepositoryImpl;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupStickersPresenter implements NotificationCenter.NotificationCenterDelegate {

    public interface GroupStickersView {
        void updateStickersList(ArrayList<TLRPC.TL_messages_stickerSet> stickerSets, int selectedIndex);
        void updateSelection(int prevIndex, int newIndex);
        void onSearchResults(List<TLRPC.TL_messages_stickerSet> searchEntries, List<TLRPC.TL_messages_stickerSet> localSearchEntries, String query);
        void onSearchCleared();
        void showSearchProgress(boolean progress, boolean clear);
        void onStickersSaved();
        void onStickersSaveError(String error);
    }

    private final int currentAccount;
    private final StickerRepository stickerRepository;
    private GroupStickersView view;

    private long chatId;
    private TLRPC.ChatFull info;
    private TLRPC.TL_messages_stickerSet selectedStickerSet;
    private boolean removeStickerSet;
    private int selectedStickerSetIndex = -1;

    private int searchReqId;
    private Runnable searchRunnable;
    private String lastSearchQuery;

    public GroupStickersPresenter(int currentAccount) {
        this(currentAccount, new StickerRepositoryImpl(currentAccount));
    }

    public GroupStickersPresenter(int currentAccount, StickerRepository stickerRepository) {
        this.currentAccount = currentAccount;
        this.stickerRepository = stickerRepository;
    }

    public void attachView(GroupStickersView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
        if (searchReqId != 0) {
            stickerRepository.cancelRequest(searchReqId);
            searchReqId = 0;
        }
        if (searchRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(searchRunnable);
            searchRunnable = null;
        }
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public long getChatId() {
        return chatId;
    }

    public void setInfo(TLRPC.ChatFull chatFull) {
        this.info = chatFull;
        if (info != null && info.stickerset != null) {
            selectedStickerSet = stickerRepository.getGroupStickerSetById(info.stickerset);
        }
        updateSelectedStickerSetIndex();
    }

    public TLRPC.ChatFull getInfo() {
        return info;
    }

    public TLRPC.TL_messages_stickerSet getSelectedStickerSet() {
        return selectedStickerSet;
    }

    public boolean isRemoveStickerSet() {
        return removeStickerSet;
    }

    public int getSelectedStickerSetIndex() {
        return selectedStickerSetIndex;
    }

    public long getSelectedStickerSetId() {
        if (removeStickerSet) {
            return 0;
        } else if (selectedStickerSet != null) {
            return selectedStickerSet.set.id;
        } else if (info != null && info.stickerset != null) {
            return info.stickerset.id;
        }
        return 0;
    }

    public void onFragmentCreate() {
        stickerRepository.checkImageStickers();
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.stickersDidLoad);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.chatInfoDidLoad);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.groupStickersDidLoad);
        loadStickers();
    }

    public void onFragmentDestroy() {
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.stickersDidLoad);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.chatInfoDidLoad);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.groupStickersDidLoad);

        if (selectedStickerSet != null || removeStickerSet) {
            saveStickerSet();
        }
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.stickersDidLoad) {
            if ((Integer) args[0] == MediaDataController.TYPE_IMAGE) {
                loadStickers();
            }
        } else if (id == NotificationCenter.chatInfoDidLoad) {
            TLRPC.ChatFull chatFull = (TLRPC.ChatFull) args[0];
            if (chatFull.id == chatId) {
                if (info == null && chatFull.stickerset != null) {
                    selectedStickerSet = stickerRepository.getGroupStickerSetById(chatFull.stickerset);
                }
                info = chatFull;
                loadStickers();
            }
        } else if (id == NotificationCenter.groupStickersDidLoad) {
            long setId = (Long) args[0];
            if (info != null && info.stickerset != null && info.stickerset.id == setId) {
                loadStickers();
            }
        }
    }

    public void loadStickers() {
        updateSelectedStickerSetIndex();
        if (view != null) {
            view.updateStickersList(stickerRepository.getImageStickerSets(), selectedStickerSetIndex);
        }
    }

    public void updateSelectedStickerSetIndex() {
        ArrayList<TLRPC.TL_messages_stickerSet> stickerSets = stickerRepository.getImageStickerSets();
        selectedStickerSetIndex = -1;

        long selectedSet = getSelectedStickerSetId();
        if (selectedSet != 0 && stickerSets != null) {
            for (int i = 0; i < stickerSets.size(); i++) {
                TLRPC.TL_messages_stickerSet set = stickerSets.get(i);
                if (set.set.id == selectedSet) {
                    selectedStickerSetIndex = i;
                    break;
                }
            }
        }
    }

    public void selectStickerSet(TLRPC.TL_messages_stickerSet stickerSet, boolean isCurrentlySelected) {
        int prevIndex = selectedStickerSetIndex;
        if (isCurrentlySelected) {
            selectedStickerSet = null;
            removeStickerSet = true;
        } else {
            selectedStickerSet = stickerSet;
            removeStickerSet = false;
        }
        updateSelectedStickerSetIndex();
        if (view != null) {
            view.updateSelection(prevIndex, selectedStickerSetIndex);
        }
    }

    public void searchStickers(String query) {
        if (searchReqId != 0) {
            stickerRepository.cancelRequest(searchReqId);
            searchReqId = 0;
        }

        if (searchRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(searchRunnable);
            searchRunnable = null;
        }
        lastSearchQuery = null;

        if (TextUtils.isEmpty(query)) {
            if (view != null) {
                view.onSearchCleared();
            }
            return;
        }

        if (view != null) {
            view.showSearchProgress(true, false);
        }

        AndroidUtilities.runOnUIThread(searchRunnable = () -> {
            lastSearchQuery = query;
            searchReqId = stickerRepository.searchStickerSets(query, new StickerRepository.SearchStickersCallback() {
                @Override
                public void onSuccess(List<TLRPC.TL_messages_stickerSet> searchEntries, List<TLRPC.TL_messages_stickerSet> localEntries) {
                    if (!Objects.equals(lastSearchQuery, query)) {
                        return;
                    }
                    if (view != null) {
                        view.onSearchResults(searchEntries, localEntries, query);
                    }
                }

                @Override
                public void onError() {
                    if (view != null) {
                        view.showSearchProgress(false, true);
                    }
                }
            });
        }, 300);
    }

    public void saveStickerSet() {
        stickerRepository.setGroupStickers(chatId, info, selectedStickerSet, removeStickerSet, new StickerRepository.SetGroupStickersCallback() {
            @Override
            public void onSuccess() {
                updateSelectedStickerSetIndex();
                if (view != null) {
                    view.onStickersSaved();
                }
            }

            @Override
            public void onError(String error) {
                if (view != null) {
                    view.onStickersSaveError(error);
                }
            }
        });
    }

    public void openStickersBot(BaseFragment fragment) {
        stickerRepository.openStickersBot(fragment);
    }
}
