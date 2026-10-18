/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.core.data.repository.PrivacyRepository;
import org.telegram.core.data.repository.PrivacyRepositoryImpl;
import org.telegram.core.presenter.PrivacyUsersPresenter;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ManageChatTextCell;
import org.telegram.ui.Cells.ManageChatUserCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;

public class PrivacyUsersActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, ContactsActivity.ContactsActivityDelegate, PrivacyUsersPresenter.View {

    private RecyclerListView listView;
    private LinearLayoutManager layoutManager;
    private ListAdapter listViewAdapter;
    private EmptyTextProgressView emptyView;

    private PrivacyActivityDelegate delegate;
    private PrivacyUsersPresenter presenter;

    private int currentType;
    private boolean isGroup;
    private ArrayList<Long> uidArray;
    private boolean isAlwaysShare;

    public static final int TYPE_PRIVACY = 0;
    public static final int TYPE_BLOCKED = 1;
    public static final int TYPE_FILTER = 2;

    public interface PrivacyActivityDelegate {
        void didUpdateUserList(ArrayList<Long> ids, boolean added);
    }

    public PrivacyUsersActivity() {
        super();
        currentType = TYPE_BLOCKED;
    }

    public PrivacyUsersActivity(int type, ArrayList<Long> users, boolean group, boolean always) {
        super();
        uidArray = users;
        isAlwaysShare = always;
        isGroup = group;
        currentType = type;
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.updateInterfaces);
        if (currentType == TYPE_BLOCKED) {
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.blockedUsersDidLoad);
        }
        if (presenter == null) {
            PrivacyRepository repository = new PrivacyRepositoryImpl(currentAccount);
            presenter = new PrivacyUsersPresenter(repository, currentType, uidArray, isGroup, isAlwaysShare);
        }
        presenter.attachView(this);
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.updateInterfaces);
        if (currentType == TYPE_BLOCKED) {
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.blockedUsersDidLoad);
        }
        if (presenter != null) {
            presenter.detachView();
        }
    }

    @Override
    public View createView(Context context) {
        if (presenter == null) {
            PrivacyRepository repository = new PrivacyRepositoryImpl(currentAccount);
            presenter = new PrivacyUsersPresenter(repository, currentType, uidArray, isGroup, isAlwaysShare);
            presenter.attachView(this);
        }

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        if (currentType == TYPE_BLOCKED) {
            actionBar.setTitle(LocaleController.getString("BlockedUsers", R.string.BlockedUsers));
        } else if (currentType == TYPE_FILTER) {
            if (isAlwaysShare) {
                actionBar.setTitle(LocaleController.getString("FilterAlwaysShow", R.string.FilterAlwaysShow));
            } else {
                actionBar.setTitle(LocaleController.getString("FilterNeverShow", R.string.FilterNeverShow));
            }
        } else {
            if (isGroup) {
                if (isAlwaysShare) {
                    actionBar.setTitle(LocaleController.getString("AlwaysAllow", R.string.AlwaysAllow));
                } else {
                    actionBar.setTitle(LocaleController.getString("NeverAllow", R.string.NeverAllow));
                }
            } else {
                if (isAlwaysShare) {
                    actionBar.setTitle(LocaleController.getString("AlwaysShareWithTitle", R.string.AlwaysShareWithTitle));
                } else {
                    actionBar.setTitle(LocaleController.getString("NeverShareWithTitle", R.string.NeverShareWithTitle));
                }
            }
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        emptyView = new EmptyTextProgressView(context);
        if (currentType == TYPE_BLOCKED) {
            emptyView.setText(LocaleController.getString("NoBlocked", R.string.NoBlocked));
        } else {
            emptyView.setText(LocaleController.getString("NoContacts", R.string.NoContacts));
        }
        frameLayout.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView = new RecyclerListView(context);
        listView.setEmptyView(emptyView);
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(listViewAdapter = new ListAdapter(context));
        listView.setVerticalScrollbarPosition(LocaleController.isRTL ? RecyclerListView.SCROLLBAR_POSITION_LEFT : RecyclerListView.SCROLLBAR_POSITION_RIGHT);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView.setOnItemClickListener((view, position) -> {
            if (position == presenter.getBlockUserRow()) {
                if (currentType == TYPE_BLOCKED) {
                    presentFragment(new DialogOrContactPickerActivity());
                } else {
                    Bundle args = new Bundle();
                    args.putBoolean(isAlwaysShare ? "isAlwaysShare" : "isNeverShare", true);
                    if (isGroup) {
                        args.putInt("chatAddType", 1);
                    } else if (currentType == TYPE_FILTER) {
                        args.putInt("chatAddType", 2);
                    }
                    GroupCreateActivity fragment = new GroupCreateActivity(args);
                    fragment.setDelegate(ids -> {
                        presenter.addUsers(ids);
                    });
                    presentFragment(fragment);
                }
            } else if (position >= presenter.getUsersStartRow() && position < presenter.getUsersEndRow()) {
                long uid = presenter.getUid(position);
                Bundle args = new Bundle();
                if (currentType == TYPE_BLOCKED) {
                    args.putLong("user_id", uid);
                    presentFragment(new ProfileActivity(args));
                } else {
                    if (DialogObject.isUserDialog(uid)) {
                        args.putLong("user_id", uid);
                    } else {
                        args.putLong("chat_id", -uid);
                    }
                    presentFragment(new ProfileActivity(args));
                }
            }
        });

        listView.setOnItemLongClickListener((view, position) -> {
            if (position >= presenter.getUsersStartRow() && position < presenter.getUsersEndRow()) {
                showUnblockAlert(presenter.getUid(position));
                return true;
            }
            return false;
        });

        if (currentType == TYPE_BLOCKED) {
            listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                    int visibleItemCount = Math.abs(layoutManager.findLastVisibleItemPosition() - firstVisibleItem) + 1;
                    int totalItemCount = recyclerView.getAdapter().getItemCount();
                    if (visibleItemCount > 0) {
                        presenter.checkLoadMore(layoutManager.findLastVisibleItemPosition(), totalItemCount);
                    }
                }
            });
            presenter.checkInitialEmptyViewState();
        }

        presenter.updateRows();
        return fragmentView;
    }

    public void setDelegate(PrivacyActivityDelegate privacyActivityDelegate) {
        delegate = privacyActivityDelegate;
    }

    private void showUnblockAlert(Long uid) {
        if (getParentActivity() == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        CharSequence[] items;
        if (currentType == TYPE_BLOCKED) {
            items = new CharSequence[]{LocaleController.getString("Unblock", R.string.Unblock)};
        } else {
            items = new CharSequence[]{LocaleController.getString("Delete", R.string.Delete)};
        }
        builder.setItems(items, (dialogInterface, i) -> {
            if (i == 0) {
                presenter.deleteUser(uid);
            }
        });
        showDialog(builder.create());
    }

    @Override
    public void updateRows() {
        if (listViewAdapter != null) {
            listViewAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void showProgress() {
        if (emptyView != null) {
            emptyView.showProgress();
        }
    }

    @Override
    public void showTextView() {
        if (emptyView != null) {
            emptyView.showTextView();
        }
    }

    @Override
    public void onUserListUpdated(ArrayList<Long> ids, boolean added) {
        if (delegate != null) {
            delegate.didUpdateUserList(ids, added);
        }
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (presenter != null) {
            presenter.onNotificationReceived(id, args);
        }
    }

    @Override
    public void updateVisibleRows(int mask) {
        if (listView == null) {
            return;
        }
        int count = listView.getChildCount();
        for (int a = 0; a < count; a++) {
            View child = listView.getChildAt(a);
            if (child instanceof ManageChatUserCell) {
                ((ManageChatUserCell) child).update(mask);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listViewAdapter != null) {
            listViewAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void didSelectContact(final TLRPC.User user, String param, ContactsActivity activity) {
        if (user == null || presenter == null) {
            return;
        }
        presenter.blockPeer(user.id);
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return presenter != null ? presenter.getRowCount() : 0;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int viewType = holder.getItemViewType();
            return viewType == 0 || viewType == 2;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    view = new ManageChatUserCell(mContext, 7, 6, true);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    ((ManageChatUserCell) view).setDelegate((cell, click) -> {
                        if (click) {
                            showUnblockAlert((Long) cell.getTag());
                        }
                        return true;
                    });
                    break;
                case 1:
                    view = new TextInfoPrivacyCell(mContext);
                    break;
                case 2:
                    view = new ManageChatTextCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 3:
                default:
                    HeaderCell headerCell = new HeaderCell(mContext, Theme.key_windowBackgroundWhiteBlueHeader, 21, 11, false);
                    headerCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    headerCell.setHeight(43);
                    view = headerCell;
                    break;
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0:
                    ManageChatUserCell userCell = (ManageChatUserCell) holder.itemView;
                    long uid = presenter.getUid(position);
                    userCell.setTag(uid);
                    if (uid > 0) {
                        TLRPC.User user = presenter.getUser(uid);
                        if (user != null) {
                            String number;
                            if (user.bot) {
                                number = LocaleController.getString("Bot", R.string.Bot).substring(0, 1).toUpperCase() + LocaleController.getString("Bot", R.string.Bot).substring(1);
                            } else if (user.phone != null && user.phone.length() != 0) {
                                number = PhoneFormat.getInstance().format("+" + user.phone);
                            } else {
                                number = LocaleController.getString("NumberUnknown", R.string.NumberUnknown);
                            }
                            userCell.setData(user, null, number, position != presenter.getUsersEndRow() - 1);
                        }
                    } else {
                        TLRPC.Chat chat = presenter.getChat(-uid);
                        if (chat != null) {
                            String subtitle;
                            if (chat.participants_count != 0) {
                                subtitle = LocaleController.formatPluralString("Members", chat.participants_count);
                            } else if (chat.has_geo) {
                                subtitle = LocaleController.getString("MegaLocation", R.string.MegaLocation);
                            } else if (!ChatObject.isPublic(chat)) {
                                subtitle = LocaleController.getString("MegaPrivate", R.string.MegaPrivate);
                            } else {
                                subtitle = LocaleController.getString("MegaPublic", R.string.MegaPublic);
                            }
                            userCell.setData(chat, null, subtitle, position != presenter.getUsersEndRow() - 1);
                        }
                    }
                    break;
                case 1:
                    TextInfoPrivacyCell privacyCell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == presenter.getBlockUserDetailRow()) {
                        if (currentType == TYPE_BLOCKED) {
                            privacyCell.setText(LocaleController.getString("BlockedUsersInfo", R.string.BlockedUsersInfo));
                        } else {
                            privacyCell.setText(null);
                        }
                        if (presenter.getUsersStartRow() == -1) {
                            privacyCell.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                        } else {
                            privacyCell.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                        }
                    } else if (position == presenter.getUsersDetailRow()) {
                        privacyCell.setText("");
                        privacyCell.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                    }
                    break;
                case 2:
                    ManageChatTextCell actionCell = (ManageChatTextCell) holder.itemView;
                    actionCell.setColors(Theme.key_windowBackgroundWhiteBlueIcon, Theme.key_windowBackgroundWhiteBlueButton);
                    if (currentType == TYPE_BLOCKED) {
                        actionCell.setText(LocaleController.getString("BlockUser", R.string.BlockUser), null, R.drawable.msg_contact_add, false);
                    } else {
                        actionCell.setText(LocaleController.getString("PrivacyAddAnException", R.string.PrivacyAddAnException), null, R.drawable.msg_contact_add, false);
                    }
                    break;
                case 3:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == presenter.getUsersHeaderRow()) {
                        if (currentType == TYPE_BLOCKED) {
                            headerCell.setText(LocaleController.formatPluralString("BlockedUsersCount", presenter.getTotalBlockedCount()));
                        } else {
                            headerCell.setText(LocaleController.getString("PrivacyExceptions", R.string.PrivacyExceptions));
                        }
                    }
                    break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (presenter == null) {
                return 0;
            }
            if (position == presenter.getUsersHeaderRow()) {
                return 3;
            } else if (position == presenter.getBlockUserRow()) {
                return 2;
            } else if (position == presenter.getBlockUserDetailRow() || position == presenter.getUsersDetailRow()) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();

        ThemeDescription.ThemeDescriptionDelegate cellDelegate = () -> {
            if (listView != null) {
                int count = listView.getChildCount();
                for (int a = 0; a < count; a++) {
                    View child = listView.getChildAt(a);
                    if (child instanceof ManageChatUserCell) {
                        ((ManageChatUserCell) child).update(0);
                    }
                }
            }
        };

        themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{ManageChatUserCell.class, ManageChatTextCell.class, HeaderCell.class}, null, null, null, Theme.key_windowBackgroundWhite));

        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector));

        themeDescriptions.add(new ThemeDescription(emptyView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_emptyListPlaceholder));
        themeDescriptions.add(new ThemeDescription(emptyView, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, Theme.key_progressCircle));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{ManageChatUserCell.class}, new String[]{"nameTextView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{ManageChatUserCell.class}, new String[]{"statusColor"}, null, null, cellDelegate, Theme.key_windowBackgroundWhiteGrayText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{ManageChatUserCell.class}, new String[]{"statusOnlineColor"}, null, null, cellDelegate, Theme.key_windowBackgroundWhiteBlueText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{ManageChatUserCell.class}, null, Theme.avatarDrawables, null, Theme.key_avatar_text));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundRed));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundOrange));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundViolet));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundGreen));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundCyan));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundBlue));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundPink));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{ManageChatTextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{ManageChatTextCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{ManageChatTextCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueButton));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{ManageChatTextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueIcon));

        return themeDescriptions;
    }
}
