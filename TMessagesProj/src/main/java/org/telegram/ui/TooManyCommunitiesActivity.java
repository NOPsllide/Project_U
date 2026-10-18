package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.core.presenter.TooManyCommunitiesPresenter;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.TooManyCommunitiesHintCell;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.GroupCreateUserCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;

public class TooManyCommunitiesActivity extends BaseFragment implements TooManyCommunitiesPresenter.TooManyCommunitiesView {

    public static final int TYPE_JOIN = 0;
    public static final int TYPE_EDIT = 1;
    public static final int TYPE_CREATE = 2;

    private TooManyCommunitiesPresenter presenter;

    private RecyclerListView listView;
    private RecyclerListView searchListView;
    private TextView buttonTextView;
    private Adapter adapter;
    private SearchAdapter searchAdapter;
    private FrameLayout buttonLayout;
    private EmptyTextProgressView emptyView;
    private FrameLayout searchViewContainer;

    private int buttonAnimation;

    private TooManyCommunitiesHintCell hintCell;

    private ValueAnimator enterAnimator;
    private float enterProgress;
    protected RadialProgressView progressBar;

    int type;

    private int buttonHeight = AndroidUtilities.dp(64);

    Runnable showProgressRunnable = new Runnable() {
        @Override
        public void run() {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setAlpha(0);
            progressBar.animate().alpha(1f).start();
        }
    };

    RecyclerListView.OnItemClickListener onItemClickListener = (view, position) -> {
        if (view instanceof GroupCreateUserCell) {
            TLRPC.Chat chat = (TLRPC.Chat) ((GroupCreateUserCell) view).getObject();
            if (presenter != null) {
                presenter.toggleChatSelection(chat.id);
                ((GroupCreateUserCell) view).setChecked(presenter.isChatSelected(chat.id), true);
            }
            if (presenter != null && !presenter.getSelectedIds().isEmpty()) {
                RecyclerListView list = searchViewContainer.getVisibility() == View.VISIBLE ? searchListView : listView;
                int bottom = list.getHeight() - view.getBottom();
                if (bottom < buttonHeight) {
                    list.smoothScrollBy(0, buttonHeight - bottom);
                }
            }
        }
    };

    RecyclerListView.OnItemLongClickListener onItemLongClickListener = (view, position) -> {
        onItemClickListener.onItemClick(view, position);
        return true;
    };

    public TooManyCommunitiesActivity(int type) {
        super();
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        arguments = bundle;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (presenter != null) {
            presenter.detachView();
        }
    }

    @Override
    public View createView(Context context) {
        type = arguments.getInt("type", TYPE_JOIN);

        presenter = new TooManyCommunitiesPresenter(currentAccount);
        presenter.attachView(this);

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("LimitReached", R.string.LimitReached));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        ActionBarMenuItem searchItem = menu.addItem(0, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {

            boolean expanded = false;

            @Override
            public void onSearchCollapse() {
                super.onSearchCollapse();
                if (listView.getVisibility() != View.VISIBLE) {
                    listView.setVisibility(View.VISIBLE);
                    listView.setAlpha(0);
                }
                emptyView.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
                listView.animate().alpha(1f).setDuration(150).setListener(null).start();

                searchViewContainer.animate().alpha(0f).setDuration(150).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        searchViewContainer.setVisibility(View.GONE);
                    }
                }).start();
                expanded = false;
            }

            @Override
            public void onTextChanged(EditText editText) {
                String query = editText.getText().toString();
                if (searchAdapter != null) {
                    searchAdapter.search(query);
                }
                if (!expanded && !TextUtils.isEmpty(query)) {
                    if (searchViewContainer.getVisibility() != View.VISIBLE) {
                        searchViewContainer.setVisibility(View.VISIBLE);
                        searchViewContainer.setAlpha(0);
                    }
                    listView.animate().alpha(0).setDuration(150).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            listView.setVisibility(View.GONE);
                        }
                    }).start();
                    searchAdapter.searchResultsSignatures.clear();
                    searchAdapter.searchResults.clear();
                    searchAdapter.notifyDataSetChanged();
                    searchViewContainer.animate().setListener(null).alpha(1f).setDuration(150).start();
                    expanded = true;
                } else if (expanded && TextUtils.isEmpty(query)) {
                    onSearchCollapse();
                }
            }
        });
        searchItem.setContentDescription(LocaleController.getString("Search", R.string.Search));
        searchItem.setSearchFieldHint(LocaleController.getString("Search", R.string.Search));

        FrameLayout contentView = new FrameLayout(context);
        fragmentView = contentView;
        listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context));
        listView.setAdapter(adapter = new Adapter());
        listView.setClipToPadding(false);
        listView.setOnItemClickListener(onItemClickListener);
        listView.setOnItemLongClickListener(onItemLongClickListener);

        searchListView = new RecyclerListView(context);
        searchListView.setLayoutManager(new LinearLayoutManager(context));
        searchListView.setAdapter(searchAdapter = new SearchAdapter());
        searchListView.setOnItemClickListener(onItemClickListener);
        searchListView.setOnItemLongClickListener(onItemLongClickListener);
        searchListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                }
            }
        });
        emptyView = new EmptyTextProgressView(context);
        emptyView.setShowAtCenter(true);
        emptyView.setText(LocaleController.getString("NoResult", R.string.NoResult));
        emptyView.showTextView();

        progressBar = new RadialProgressView(context);
        contentView.addView(progressBar, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
        adapter.updateRows();
        progressBar.setVisibility(View.GONE);

        contentView.addView(listView);
        searchViewContainer = new FrameLayout(context);
        searchViewContainer.addView(searchListView);
        searchViewContainer.addView(emptyView);
        searchViewContainer.setVisibility(View.GONE);
        contentView.addView(searchViewContainer);

        presenter.loadInactiveChannels();

        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        buttonLayout = new FrameLayout(context) {
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                canvas.drawRect(0, 0, getMeasuredWidth(), 1, Theme.dividerPaint);
            }
        };
        buttonLayout.setWillNotDraw(false);
        buttonTextView = new TextView(context);
        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        buttonTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        buttonTextView.setBackground(Theme.AdaptiveRipple.filledRect(Theme.key_featuredStickers_addButton, 4));
        contentView.addView(buttonLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 64, Gravity.BOTTOM));
        buttonLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        buttonLayout.addView(buttonTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, 0, 16, 12, 16, 12));
        buttonLayout.setVisibility(View.GONE);
        buttonTextView.setOnClickListener(v -> {
            if (presenter != null) {
                presenter.leaveSelectedChats();
            }
        });
        return fragmentView;
    }

    private void onSelectedCountChange(int count) {
        if (count == 0 && buttonAnimation != -1 && buttonLayout.getVisibility() == View.VISIBLE) {
            buttonAnimation = -1;
            buttonLayout.animate().setListener(null).cancel();
            buttonLayout.animate().translationY(buttonHeight).setDuration(200).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    buttonAnimation = 0;
                    buttonLayout.setVisibility(View.GONE);
                }
            }).start();

            RecyclerListView list = searchViewContainer.getVisibility() == View.VISIBLE ? searchListView : listView;
            list.hideSelector(false);

            int last = ((LinearLayoutManager) list.getLayoutManager()).findLastVisibleItemPosition();
            if (last == list.getAdapter().getItemCount() - 1 || (last == list.getAdapter().getItemCount() - 2 && list == listView)) {
                RecyclerView.ViewHolder holder = list.findViewHolderForAdapterPosition(last);
                if (holder != null) {
                    int bottom = holder.itemView.getBottom();
                    if (last == adapter.getItemCount() - 2) {
                        bottom += AndroidUtilities.dp(12);
                    }
                    if (list.getMeasuredHeight() - bottom <= buttonHeight) {
                        int dy = -(list.getMeasuredHeight() - bottom);
                        list.setTranslationY(dy);
                        list.animate().translationY(0).setDuration(200).start();
                    }
                }
            }
            listView.setPadding(0, 0, 0, 0);
            searchListView.setPadding(0, 0, 0, 0);
        }
        if (count > 0 && buttonLayout.getVisibility() == View.GONE && buttonAnimation != 1) {
            buttonAnimation = 1;
            buttonLayout.setVisibility(View.VISIBLE);
            buttonLayout.setTranslationY(buttonHeight);
            buttonLayout.animate().setListener(null).cancel();
            buttonLayout.animate().translationY(0).setDuration(200).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    buttonAnimation = 0;
                }
            }).start();

            listView.setPadding(0, 0, 0, buttonHeight - AndroidUtilities.dp(12));
            searchListView.setPadding(0, 0, 0, buttonHeight);
        }

        if (count > 0) {
            buttonTextView.setText(LocaleController.formatString("LeaveChats", R.string.LeaveChats, LocaleController.formatPluralString("Chats", count)));
        }
    }

    @Override
    public void showProgress(boolean show) {
        if (show) {
            enterProgress = 0f;
            AndroidUtilities.runOnUIThread(showProgressRunnable, 500);
        } else {
            AndroidUtilities.cancelRunOnUIThread(showProgressRunnable);
            if (progressBar != null && progressBar.getVisibility() == View.VISIBLE) {
                progressBar.animate().alpha(0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        progressBar.setVisibility(View.GONE);
                    }
                }).start();
            }
        }
    }

    @Override
    public void onInactiveChatsLoaded(ArrayList<TLRPC.Chat> chats, ArrayList<String> signatures) {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        if (listView != null && listView.getMeasuredHeight() > 0) {
            enterAnimator = ValueAnimator.ofFloat(0, 1f);
            enterAnimator.addUpdateListener(animation -> {
                enterProgress = (float) animation.getAnimatedValue();
                int n = listView.getChildCount();
                for (int i = 0; i < n; i++) {
                    if (listView.getChildAdapterPosition(listView.getChildAt(i)) >= adapter.headerPosition && adapter.headerPosition > 0) {
                        listView.getChildAt(i).setAlpha(enterProgress);
                    } else {
                        listView.getChildAt(i).setAlpha(1f);
                    }
                }
            });
            enterAnimator.setDuration(100);
            enterAnimator.start();
        } else {
            enterProgress = 1f;
        }
    }

    @Override
    public void onSearchResults(ArrayList<TLRPC.Chat> chats, ArrayList<String> signatures, int searchId) {
        if (searchAdapter != null) {
            searchAdapter.updateSearchResults(chats, signatures);
        }
    }

    @Override
    public void onSelectedCountChanged(int count) {
        onSelectedCountChange(count);
    }

    @Override
    public void finishScreen() {
        finishFragment();
    }

    class Adapter extends RecyclerListView.SelectionAdapter {

        int rowCount;
        int hintPosition;
        int shadowPosition;
        int headerPosition;
        int inactiveChatsStartRow;
        int inactiveChatsEndRow;
        int endPaddingPosition;

        @Override
        public void notifyDataSetChanged() {
            updateRows();
            super.notifyDataSetChanged();
        }

        public void updateRows() {
            hintPosition = -1;
            shadowPosition = -1;
            headerPosition = -1;
            inactiveChatsStartRow = -1;
            inactiveChatsEndRow = -1;
            endPaddingPosition = -1;

            rowCount = 0;
            hintPosition = rowCount++;
            shadowPosition = rowCount++;

            int count = presenter != null ? presenter.getInactiveChats().size() : 0;
            if (count > 0) {
                headerPosition = rowCount++;
                inactiveChatsStartRow = rowCount++;
                rowCount += count - 1;
                inactiveChatsEndRow = rowCount;
                endPaddingPosition = rowCount++;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 1:
                    hintCell = new TooManyCommunitiesHintCell(parent.getContext());
                    view = hintCell;
                    String message;
                    if (type == TYPE_JOIN) {
                        message = LocaleController.getString("TooManyCommunitiesHintJoin", R.string.TooManyCommunitiesHintJoin);
                    } else if (type == TYPE_EDIT) {
                        message = LocaleController.getString("TooManyCommunitiesHintEdit", R.string.TooManyCommunitiesHintEdit);
                    } else {
                        message = LocaleController.getString("TooManyCommunitiesHintCreate", R.string.TooManyCommunitiesHintCreate);
                    }
                    hintCell.setMessageText(message);
                    RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lp.bottomMargin = AndroidUtilities.dp(16);
                    lp.topMargin = AndroidUtilities.dp(23);

                    hintCell.setLayoutParams(lp);
                    break;
                case 2:
                    view = new ShadowSectionCell(parent.getContext());
                    Drawable drawable = Theme.getThemedDrawable(parent.getContext(), R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow);
                    CombinedDrawable combinedDrawable = new CombinedDrawable(new ColorDrawable(Theme.getColor(Theme.key_windowBackgroundGray)), drawable);
                    combinedDrawable.setFullsize(true);
                    view.setBackground(combinedDrawable);
                    break;
                case 3:
                    HeaderCell header = new HeaderCell(parent.getContext(), Theme.key_windowBackgroundWhiteBlueHeader, 21, 8, false);
                    view = header;
                    header.setHeight(54);
                    header.setText(LocaleController.getString("InactiveChats", R.string.InactiveChats));
                    break;
                case 5:
                    view = new EmptyCell(parent.getContext(), AndroidUtilities.dp(12));
                    break;
                case 4:
                default:
                    view = new GroupCreateUserCell(parent.getContext(), 1, 0, false);
                    break;

            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (position >= headerPosition && headerPosition > 0) {
                holder.itemView.setAlpha(enterProgress);
            } else {
                holder.itemView.setAlpha(1f);
            }
            if (getItemViewType(position) == 4 && presenter != null) {
                GroupCreateUserCell cell = (GroupCreateUserCell) holder.itemView;
                int index = position - inactiveChatsStartRow;
                TLRPC.Chat chat = presenter.getInactiveChats().get(index);
                String signature = presenter.getInactiveChatsSignatures().get(index);
                cell.setObject(chat, chat.title, signature, position != inactiveChatsEndRow - 1);
                cell.setChecked(presenter.isChatSelected(chat.id), false);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == hintPosition) {
                return 1;
            } else if (position == shadowPosition) {
                return 2;
            } else if (position == headerPosition) {
                return 3;
            } else if (position == endPaddingPosition) {
                return 5;
            }
            return 4;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            if (holder.getAdapterPosition() >= inactiveChatsStartRow && holder.getAdapterPosition() < inactiveChatsEndRow) {
                return true;
            }
            return false;
        }
    }

    class SearchAdapter extends RecyclerListView.SelectionAdapter {

        ArrayList<TLRPC.Chat> searchResults = new ArrayList<>();
        ArrayList<String> searchResultsSignatures = new ArrayList<>();

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RecyclerListView.Holder(new GroupCreateUserCell(parent.getContext(), 1, 0, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            TLRPC.Chat chat = searchResults.get(position);
            String signature = searchResultsSignatures.get(position);
            GroupCreateUserCell cell = ((GroupCreateUserCell) holder.itemView);
            cell.setObject(chat, chat.title, signature, position != searchResults.size() - 1);
            cell.setChecked(presenter != null && presenter.isChatSelected(chat.id), false);
        }

        @Override
        public int getItemCount() {
            return searchResults.size();
        }

        public void search(final String query) {
            if (presenter != null) {
                presenter.search(query);
            }
        }

        public void updateSearchResults(ArrayList<TLRPC.Chat> chats, ArrayList<String> signatures) {
            searchResults.clear();
            searchResultsSignatures.clear();
            if (chats != null) {
                searchResults.addAll(chats);
                searchResultsSignatures.addAll(signatures);
            }
            notifyDataSetChanged();
            if (searchResults.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
            } else {
                emptyView.setVisibility(View.GONE);
            }
        }
    }

    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();
        ThemeDescription.ThemeDescriptionDelegate cellDelegate = () -> {
            if (listView != null) {
                int count = listView.getChildCount();
                for (int a = 0; a < count; a++) {
                    View child = listView.getChildAt(a);
                    if (child instanceof GroupCreateUserCell) {
                        ((GroupCreateUserCell) child).update(0);
                    }
                }
            }

            if (searchListView != null) {
                int count = searchListView.getChildCount();
                for (int a = 0; a < count; a++) {
                    View child = searchListView.getChildAt(a);
                    if (child instanceof GroupCreateUserCell) {
                        ((GroupCreateUserCell) child).update(0);
                    }
                }
            }

            buttonTextView.setBackground(Theme.AdaptiveRipple.filledRect(Theme.key_featuredStickers_addButton, 4));
            progressBar.setProgressColor(Theme.getColor(Theme.key_progressCircle));
        };

        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCH, null, null, null, null, Theme.key_actionBarDefaultSearch));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, null, null, null, null, Theme.key_actionBarDefaultSearchPlaceholder));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle));

        themeDescriptions.add(new ThemeDescription(hintCell, 0, new Class[]{TooManyCommunitiesHintCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_chats_nameMessage_threeLines));
        themeDescriptions.add(new ThemeDescription(hintCell, 0, new Class[]{TooManyCommunitiesHintCell.class}, new String[]{"headerTextView"}, null, null, null, Theme.key_chats_nameMessage_threeLines));
        themeDescriptions.add(new ThemeDescription(hintCell, 0, new Class[]{TooManyCommunitiesHintCell.class}, new String[]{"messageTextView"}, null, null, null, Theme.key_chats_message));
        themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite));
        themeDescriptions.add(new ThemeDescription(buttonLayout, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGray));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{GroupCreateUserCell.class}, new String[]{"textView"}, null, null, null, Theme.key_groupcreate_sectionText));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{GroupCreateUserCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_checkbox));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{GroupCreateUserCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_checkboxDisabled));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{GroupCreateUserCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_checkboxCheck));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{GroupCreateUserCell.class}, new String[]{"nameTextView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{GroupCreateUserCell.class}, new String[]{"statusTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{GroupCreateUserCell.class}, null, Theme.avatarDrawables, null, Theme.key_avatar_text));

        themeDescriptions.add(new ThemeDescription(searchListView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{GroupCreateUserCell.class}, new String[]{"textView"}, null, null, null, Theme.key_groupcreate_sectionText));
        themeDescriptions.add(new ThemeDescription(searchListView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{GroupCreateUserCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_checkbox));
        themeDescriptions.add(new ThemeDescription(searchListView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{GroupCreateUserCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_checkboxDisabled));
        themeDescriptions.add(new ThemeDescription(searchListView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{GroupCreateUserCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_checkboxCheck));
        themeDescriptions.add(new ThemeDescription(searchListView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{GroupCreateUserCell.class}, new String[]{"nameTextView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(searchListView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{GroupCreateUserCell.class}, new String[]{"statusTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText));
        themeDescriptions.add(new ThemeDescription(searchListView, 0, new Class[]{GroupCreateUserCell.class}, null, Theme.avatarDrawables, null, Theme.key_avatar_text));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundRed));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundOrange));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundViolet));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundGreen));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundCyan));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundBlue));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundPink));
        themeDescriptions.add(new ThemeDescription(emptyView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_emptyListPlaceholder));

        themeDescriptions.add(new ThemeDescription(buttonTextView, 0, null, null, null, cellDelegate, Theme.key_featuredStickers_addButton));
        themeDescriptions.add(new ThemeDescription(buttonTextView, 0, null, null, null, cellDelegate, Theme.key_featuredStickers_addButtonPressed));
        themeDescriptions.add(new ThemeDescription(progressBar, 0, null, null, null, cellDelegate, Theme.key_featuredStickers_addButtonPressed));
        themeDescriptions.add(new ThemeDescription(hintCell, 0, new Class[]{TooManyCommunitiesHintCell.class}, new String[]{"imageLayout"}, null, null, null, Theme.key_dialogRedIcon));

        return themeDescriptions;
    }
}
