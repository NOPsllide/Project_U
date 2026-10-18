package org.telegram.core.presenter;

import org.telegram.core.data.repository.PrivacyRepository;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.support.LongSparseIntArray;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.PrivacyUsersActivity;

import java.util.ArrayList;

public class PrivacyUsersPresenter {

    public interface View {
        void updateRows();
        void showProgress();
        void showTextView();
        void updateVisibleRows(int mask);
        void finishFragment();
        void onUserListUpdated(ArrayList<Long> ids, boolean added);
    }

    private final PrivacyRepository privacyRepository;
    private final int currentType;
    private final boolean isGroup;
    private final boolean isAlwaysShare;
    private final boolean blockedUsersActivity;
    private final ArrayList<Long> uidArray;

    private View view;

    private int rowCount;
    private int blockUserRow = -1;
    private int blockUserDetailRow = -1;
    private int usersHeaderRow = -1;
    private int usersStartRow = -1;
    private int usersEndRow = -1;
    private int usersDetailRow = -1;

    public PrivacyUsersPresenter(PrivacyRepository privacyRepository, int currentType, ArrayList<Long> users, boolean group, boolean always) {
        this.privacyRepository = privacyRepository;
        this.currentType = currentType;
        this.isGroup = group;
        this.isAlwaysShare = always;
        this.blockedUsersActivity = (currentType == PrivacyUsersActivity.TYPE_BLOCKED);
        if (users != null) {
            this.uidArray = users;
        } else {
            this.uidArray = new ArrayList<>();
        }
    }

    public void attachView(View view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void updateRows() {
        rowCount = 0;
        if (!blockedUsersActivity || privacyRepository.getTotalBlockedCount() >= 0) {
            blockUserRow = rowCount++;
            blockUserDetailRow = rowCount++;

            int count;
            if (currentType == PrivacyUsersActivity.TYPE_BLOCKED) {
                LongSparseIntArray blocked = privacyRepository.getBlockedPeers();
                count = blocked != null ? blocked.size() : 0;
            } else {
                count = uidArray.size();
            }
            if (count != 0) {
                usersHeaderRow = rowCount++;
                usersStartRow = rowCount;
                rowCount += count;
                usersEndRow = rowCount;
                usersDetailRow = rowCount++;
            } else {
                usersHeaderRow = -1;
                usersStartRow = -1;
                usersEndRow = -1;
                usersDetailRow = -1;
            }
        } else {
            blockUserRow = -1;
            blockUserDetailRow = -1;
            usersHeaderRow = -1;
            usersStartRow = -1;
            usersEndRow = -1;
            usersDetailRow = -1;
        }
        if (view != null) {
            view.updateRows();
        }
    }

    public void checkInitialEmptyViewState() {
        if (view == null) {
            return;
        }
        if (currentType == PrivacyUsersActivity.TYPE_BLOCKED) {
            if (privacyRepository.getTotalBlockedCount() < 0) {
                view.showProgress();
            } else {
                view.showTextView();
            }
        }
    }

    public void onNotificationReceived(int id, Object... args) {
        if (id == NotificationCenter.updateInterfaces) {
            int mask = (Integer) args[0];
            if ((mask & MessagesController.UPDATE_MASK_AVATAR) != 0 || (mask & MessagesController.UPDATE_MASK_NAME) != 0) {
                if (view != null) {
                    view.updateVisibleRows(mask);
                }
            }
        } else if (id == NotificationCenter.blockedUsersDidLoad) {
            if (view != null) {
                view.showTextView();
            }
            updateRows();
        }
    }

    public void checkLoadMore(int lastVisibleItemPosition, int totalItemCount) {
        if (currentType != PrivacyUsersActivity.TYPE_BLOCKED) {
            return;
        }
        if (privacyRepository.isBlockedEndReached()) {
            return;
        }
        if (lastVisibleItemPosition >= totalItemCount - 10) {
            privacyRepository.loadBlockedPeers(false);
        }
    }

    public void addUsers(ArrayList<Long> ids) {
        if (ids == null) {
            return;
        }
        for (Long id1 : ids) {
            if (uidArray.contains(id1)) {
                continue;
            }
            uidArray.add(id1);
        }
        updateRows();
        if (view != null) {
            view.onUserListUpdated(uidArray, true);
        }
    }

    public void deleteUser(long uid) {
        if (currentType == PrivacyUsersActivity.TYPE_BLOCKED) {
            privacyRepository.unblockPeer(uid);
        } else {
            uidArray.remove(Long.valueOf(uid));
            updateRows();
            if (view != null) {
                view.onUserListUpdated(uidArray, false);
                if (uidArray.isEmpty()) {
                    view.finishFragment();
                }
            }
        }
    }

    public void blockPeer(long uid) {
        privacyRepository.blockPeer(uid);
    }

    public long getUid(int position) {
        if (currentType == PrivacyUsersActivity.TYPE_BLOCKED) {
            LongSparseIntArray blocked = privacyRepository.getBlockedPeers();
            if (blocked != null && position >= usersStartRow && position < usersEndRow) {
                return blocked.keyAt(position - usersStartRow);
            }
            return 0;
        } else {
            int index = position - usersStartRow;
            if (index >= 0 && index < uidArray.size()) {
                return uidArray.get(index);
            }
            return 0;
        }
    }

    public TLRPC.User getUser(long userId) {
        return privacyRepository.getUser(userId);
    }

    public TLRPC.Chat getChat(long chatId) {
        return privacyRepository.getChat(chatId);
    }

    public int getTotalBlockedCount() {
        return privacyRepository.getTotalBlockedCount();
    }

    public int getCurrentType() {
        return currentType;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public boolean isAlwaysShare() {
        return isAlwaysShare;
    }

    public ArrayList<Long> getUidArray() {
        return uidArray;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getBlockUserRow() {
        return blockUserRow;
    }

    public int getBlockUserDetailRow() {
        return blockUserDetailRow;
    }

    public int getUsersHeaderRow() {
        return usersHeaderRow;
    }

    public int getUsersStartRow() {
        return usersStartRow;
    }

    public int getUsersEndRow() {
        return usersEndRow;
    }

    public int getUsersDetailRow() {
        return usersDetailRow;
    }
}
