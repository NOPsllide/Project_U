package org.telegram.core.data.repository;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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
        MediaDataController.getInstance(accountNum).toggleStickerSet((android.content.Context) parentActivity, pack, installType, (BaseFragment) baseFragment, showSettings, showTooltip);
    }

    @Override
    public void toggleStickerSet(Object parentActivity, TLRPC.TL_messages_stickerSet pack, int installType, Object baseFragment, boolean showSettings, boolean showTooltip) {
        MediaDataController.getInstance(accountNum).toggleStickerSet((android.content.Context) parentActivity, pack, installType, (BaseFragment) baseFragment, showSettings, showTooltip);
    }

    @Override
    public void toggleStickerSets(ArrayList<TLRPC.StickerSet> sets, int currentType, int action, Object fragment, boolean showProgress) {
        MediaDataController.getInstance(accountNum).toggleStickerSets(sets, currentType, action, (BaseFragment) fragment, showProgress);
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

    @Override
    public boolean isStickerPackInstalled(long setId, boolean countForced) {
        return MediaDataController.getInstance(accountNum).isStickerPackInstalled(setId, countForced);
    }

    @Override
    public void checkImageStickers() {
        MediaDataController.getInstance(accountNum).checkStickers(MediaDataController.TYPE_IMAGE);
    }

    @Override
    public void checkStickers(int type) {
        MediaDataController.getInstance(accountNum).checkStickers(type);
    }

    @Override
    public void checkFeaturedEmoji() {
        MediaDataController.getInstance(accountNum).checkFeaturedEmoji();
    }

    @Override
    public ArrayList<TLRPC.TL_messages_stickerSet> getImageStickerSets() {
        return MediaDataController.getInstance(accountNum).getStickerSets(MediaDataController.TYPE_IMAGE);
    }

    @Override
    public ArrayList<TLRPC.TL_messages_stickerSet> getStickerSets(int type) {
        return MediaDataController.getInstance(accountNum).getStickerSets(type);
    }

    @Override
    public ArrayList<TLRPC.TL_messages_stickerSet> getFilteredStickerSets(int type) {
        return new ArrayList<>(MessagesController.getInstance(accountNum).filterPremiumStickers(MediaDataController.getInstance(accountNum).getStickerSets(type)));
    }

    @Override
    public ArrayList<TLRPC.StickerSetCovered> getFeaturedEmojiSets() {
        return MediaDataController.getInstance(accountNum).getFeaturedEmojiSets();
    }

    @Override
    public List<TLRPC.StickerSetCovered> getFeaturedSets(int currentType) {
        MediaDataController mediaDataController = MediaDataController.getInstance(accountNum);
        List<TLRPC.StickerSetCovered> featuredStickerSets;
        if (currentType == MediaDataController.TYPE_EMOJIPACKS) {
            featuredStickerSets = new ArrayList<>(mediaDataController.getFeaturedEmojiSets());
            for (int i = 0; i < featuredStickerSets.size(); ++i) {
                if (featuredStickerSets.get(i) == null || mediaDataController.isStickerPackInstalled(featuredStickerSets.get(i).set.id, false)) {
                    featuredStickerSets.remove(i--);
                }
            }
        } else {
            featuredStickerSets = mediaDataController.getFeaturedStickerSets();
        }
        return featuredStickerSets;
    }

    @Override
    public int getArchivedStickersCount(int type) {
        return MediaDataController.getInstance(accountNum).getArchivedStickersCount(type);
    }

    @Override
    public void calcNewHash(int type) {
        MediaDataController.getInstance(accountNum).calcNewHash(type);
    }

    @Override
    public void reorderStickerSets(int type, ArrayList<Long> order, Runnable onComplete) {
        MediaDataController.getInstance(accountNum).calcNewHash(type);
        TLRPC.TL_messages_reorderStickerSets req = new TLRPC.TL_messages_reorderStickerSets();
        req.masks = type == MediaDataController.TYPE_MASK;
        req.emojis = type == MediaDataController.TYPE_EMOJIPACKS;
        req.order.addAll(order);
        ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (onComplete != null) {
                onComplete.run();
            }
        }));
        NotificationCenter.getInstance(accountNum).postNotificationName(NotificationCenter.stickersDidLoad, type, true);
    }

    @Override
    public void sortStickerSets(int type, List<TLRPC.TL_messages_stickerSet> orderList) {
        ArrayList<TLRPC.TL_messages_stickerSet> sets = MediaDataController.getInstance(accountNum).getStickerSets(type);
        Collections.sort(sets, (o1, o2) -> {
            int i1 = orderList.indexOf(o1);
            int i2 = orderList.indexOf(o2);
            if (i1 >= 0 && i2 >= 0) {
                return i1 - i2;
            }
            return 0;
        });
    }

    @Override
    public String getDoubleTapReaction() {
        return MediaDataController.getInstance(accountNum).getDoubleTapReaction();
    }

    @Override
    public TLRPC.TL_availableReaction getReaction(String reaction) {
        return MediaDataController.getInstance(accountNum).getReactionsMap().get(reaction);
    }

    @Override
    public TLRPC.TL_messages_stickerSet getGroupStickerSetById(TLRPC.StickerSet stickerset) {
        return MediaDataController.getInstance(accountNum).getGroupStickerSetById(stickerset);
    }

    @Override
    public void putGroupStickerSet(TLRPC.TL_messages_stickerSet stickerSet) {
        MediaDataController.getInstance(accountNum).putGroupStickerSet(stickerSet);
    }

    @Override
    public int searchStickerSets(String query, SearchStickersCallback callback) {
        TLRPC.TL_messages_searchStickerSets searchStickerSets = new TLRPC.TL_messages_searchStickerSets();
        searchStickerSets.q = query;
        return ConnectionsManager.getInstance(accountNum).sendRequest(searchStickerSets, (response, error) -> {
            if (error == null && response instanceof TLRPC.TL_messages_foundStickerSets) {
                List<TLRPC.TL_messages_stickerSet> newSearchEntries = new ArrayList<>();
                TLRPC.TL_messages_foundStickerSets foundStickerSets = (TLRPC.TL_messages_foundStickerSets) response;
                for (TLRPC.StickerSetCovered stickerSetCovered : foundStickerSets.sets) {
                    TLRPC.TL_messages_stickerSet set = new TLRPC.TL_messages_stickerSet();
                    set.set = stickerSetCovered.set;
                    set.documents = stickerSetCovered.covers;
                    newSearchEntries.add(set);
                }
                String lowQuery = query.toLowerCase(Locale.ROOT).trim();
                List<TLRPC.TL_messages_stickerSet> newLocalEntries = new ArrayList<>();
                for (TLRPC.TL_messages_stickerSet localSet : MediaDataController.getInstance(accountNum).getStickerSets(MediaDataController.TYPE_IMAGE)) {
                    if (localSet.set.short_name.toLowerCase(Locale.ROOT).contains(lowQuery) || localSet.set.title.toLowerCase(Locale.ROOT).contains(lowQuery)) {
                        newLocalEntries.add(localSet);
                    }
                }
                AndroidUtilities.runOnUIThread(() -> {
                    if (callback != null) {
                        callback.onSuccess(newSearchEntries, newLocalEntries);
                    }
                });
            } else {
                AndroidUtilities.runOnUIThread(() -> {
                    if (callback != null) {
                        callback.onError();
                    }
                });
            }
        }, ConnectionsManager.RequestFlagInvokeAfter | ConnectionsManager.RequestFlagFailOnServerErrors);
    }

    @Override
    public void cancelRequest(int reqId) {
        if (reqId != 0) {
            ConnectionsManager.getInstance(accountNum).cancelRequest(reqId, true);
        }
    }

    @Override
    public void setGroupStickers(long chatId, TLRPC.ChatFull info, TLRPC.TL_messages_stickerSet selectedStickerSet, boolean removeStickerSet, SetGroupStickersCallback callback) {
        if (info == null || info.stickerset != null && selectedStickerSet != null && selectedStickerSet.set.id == info.stickerset.id || info.stickerset == null && selectedStickerSet == null) {
            return;
        }
        TLRPC.TL_channels_setStickers req = new TLRPC.TL_channels_setStickers();
        req.channel = MessagesController.getInstance(accountNum).getInputChannel(chatId);
        if (removeStickerSet) {
            req.stickerset = new TLRPC.TL_inputStickerSetEmpty();
        } else {
            MessagesController.getEmojiSettings(accountNum).edit().remove("group_hide_stickers_" + info.id).apply();
            req.stickerset = new TLRPC.TL_inputStickerSetID();
            req.stickerset.id = selectedStickerSet.set.id;
            req.stickerset.access_hash = selectedStickerSet.set.access_hash;
        }
        ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (error == null) {
                if (selectedStickerSet == null) {
                    info.stickerset = null;
                } else {
                    info.stickerset = selectedStickerSet.set;
                    MediaDataController.getInstance(accountNum).putGroupStickerSet(selectedStickerSet);
                }
                if (info.stickerset == null) {
                    info.flags |= 256;
                } else {
                    info.flags = info.flags &~ 256;
                }
                MessagesStorage.getInstance(accountNum).updateChatInfo(info, false);
                NotificationCenter.getInstance(accountNum).postNotificationName(NotificationCenter.chatInfoDidLoad, info, 0, true, false);
                if (callback != null) {
                    callback.onSuccess();
                }
            } else {
                if (callback != null) {
                    callback.onError(error.text);
                }
            }
        }));
    }

    @Override
    public void openStickersBot(BaseFragment fragment) {
        MessagesController.getInstance(accountNum).openByUserName("stickers", fragment, 1);
    }
}
