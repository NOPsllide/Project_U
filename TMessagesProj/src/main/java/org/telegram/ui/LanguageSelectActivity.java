/*
 * This is the source code of Telegram for Android v. 1.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.core.data.repository.LanguageRepository;
import org.telegram.core.data.repository.LanguageRepositoryImpl;
import org.telegram.core.presenter.LanguageSelectPresenter;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LanguageDetector;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.LanguageCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextRadioCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;

public class LanguageSelectActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, LanguageSelectPresenter.View {

    private ListAdapter listAdapter;
    private RecyclerListView listView;
    private ListAdapter searchListViewAdapter;
    private EmptyTextProgressView emptyView;

    private boolean searchWas;
    private boolean searching;

    private LanguageSelectPresenter presenter;

    @Override
    public boolean onFragmentCreate() {
        if (presenter == null) {
            LanguageRepository repository = new LanguageRepositoryImpl();
            presenter = new LanguageSelectPresenter(repository, currentAccount);
        }
        presenter.attachView(this);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.suggestedLangpack);
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.suggestedLangpack);
        if (presenter != null) {
            presenter.detachView();
        }
    }

    @Override
    public View createView(Context context) {
        if (presenter == null) {
            LanguageRepository repository = new LanguageRepositoryImpl();
            presenter = new LanguageSelectPresenter(repository, currentAccount);
            presenter.attachView(this);
        }

        searching = false;
        searchWas = false;

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("Language", R.string.Language));

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        ActionBarMenuItem item = menu.addItem(0, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {
            @Override
            public void onSearchExpand() {
                searching = true;
            }

            @Override
            public void onSearchCollapse() {
                if (presenter != null) {
                    presenter.search(null);
                }
                searching = false;
                searchWas = false;
                if (listView != null) {
                    emptyView.setVisibility(View.GONE);
                    listView.setAdapter(listAdapter);
                }
            }

            @Override
            public void onTextChanged(EditText editText) {
                String text = editText.getText().toString();
                if (presenter != null) {
                    presenter.search(text);
                }
                if (text.length() != 0) {
                    searchWas = true;
                    if (listView != null) {
                        listView.setAdapter(searchListViewAdapter);
                    }
                } else {
                    searching = false;
                    searchWas = false;
                    if (listView != null) {
                        emptyView.setVisibility(View.GONE);
                        listView.setAdapter(listAdapter);
                    }
                }
            }
        });
        item.setSearchFieldHint(LocaleController.getString("Search", R.string.Search));

        listAdapter = new ListAdapter(context, false);
        searchListViewAdapter = new ListAdapter(context, true);

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        emptyView = new EmptyTextProgressView(context);
        emptyView.setText(LocaleController.getString("NoResult", R.string.NoResult));
        emptyView.showTextView();
        emptyView.setShowAtCenter(true);
        frameLayout.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView = new RecyclerListView(context);
        listView.setEmptyView(emptyView);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(listAdapter);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView.setOnItemClickListener((view, position) -> {
            if (getParentActivity() == null || parentLayout == null || !(view instanceof TextRadioCell) || presenter == null) {
                return;
            }
            boolean search = listView.getAdapter() == searchListViewAdapter;
            presenter.onItemClick(position, search);
        });

        listView.setOnItemLongClickListener((view, position) -> {
            if (getParentActivity() == null || parentLayout == null || !(view instanceof TextRadioCell) || presenter == null) {
                return false;
            }
            boolean search = listView.getAdapter() == searchListViewAdapter;
            return presenter.onItemLongClick(position, search);
        });

        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                }
            }
        });

        return fragmentView;
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (presenter != null) {
            presenter.onNotificationReceived(id);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
        if (searchListViewAdapter != null) {
            searchListViewAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void showSearchResults(ArrayList<LocaleController.LocaleInfo> results) {
        if (results == null) {
            if (listView != null) {
                emptyView.setVisibility(View.GONE);
                listView.setAdapter(listAdapter);
            }
        } else {
            if (searchListViewAdapter != null) {
                searchListViewAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void showDeleteLanguageDialog(LocaleController.LocaleInfo localeInfo) {
        if (getParentActivity() == null || localeInfo == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(LocaleController.getString("DeleteLocalizationTitle", R.string.DeleteLocalizationTitle));
        builder.setMessage(AndroidUtilities.replaceTags(LocaleController.formatString("DeleteLocalizationText", R.string.DeleteLocalizationText, localeInfo.name)));
        builder.setPositiveButton(LocaleController.getString("Delete", R.string.Delete), (dialogInterface, i) -> {
            if (presenter != null) {
                presenter.deleteLanguage(localeInfo);
            }
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        AlertDialog alertDialog = builder.create();
        showDialog(alertDialog);
        TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (button != null) {
            button.setTextColor(Theme.getColor(Theme.key_dialogTextRed2));
        }
    }

    @Override
    public void onLanguageApplied() {
        if (parentLayout != null) {
            parentLayout.rebuildAllFragmentViews(false, false);
        }
    }

    private class TranslateSettings extends LinearLayout {
        private HeaderCell header;
        private TextCheckCell showButtonCheck;
        private TextSettingsCell doNotTranslateCell;
        private TextInfoPrivacyCell info;
        private TextInfoPrivacyCell info2;
        private ValueAnimator doNotTranslateCellAnimation = null;
        private SharedPreferences.OnSharedPreferenceChangeListener listener;

        public TranslateSettings(Context context) {
            super(context);
            setFocusable(false);
            setOrientation(VERTICAL);

            header = new HeaderCell(context);
            header.setFocusable(true);
            header.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            header.setText(LocaleController.getString("TranslateMessages", R.string.TranslateMessages));
            header.setContentDescription(LocaleController.getString("TranslateMessages", R.string.TranslateMessages));
            addView(header, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            boolean value = getValue();
            showButtonCheck = new TextCheckCell(context);
            showButtonCheck.setBackground(Theme.AdaptiveRipple.filledRect(Theme.getColor(Theme.key_windowBackgroundWhite)));
            showButtonCheck.setTextAndCheck(
                LocaleController.getString("ShowTranslateButton", R.string.ShowTranslateButton),
                value,
                value
            );
            showButtonCheck.setOnClickListener(e -> {
                if (presenter != null) {
                    presenter.setTranslateButtonEnabled(!getValue());
                }
            });
            addView(showButtonCheck, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            doNotTranslateCell = new TextSettingsCell(context);
            doNotTranslateCell.setBackground(Theme.AdaptiveRipple.filledRect(Theme.getColor(Theme.key_windowBackgroundWhite)));
            doNotTranslateCell.setOnClickListener(e -> {
                presentFragment(new RestrictedLanguagesSelectActivity());
                update();
            });
            doNotTranslateCell.setClickable(value && LanguageDetector.hasSupport());
            doNotTranslateCell.setAlpha(value && LanguageDetector.hasSupport() ? 1f : 0f);
            addView(doNotTranslateCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            info = new TextInfoPrivacyCell(context);
            info.setTopPadding(11);
            info.setBottomPadding(16);
            info.setFocusable(true);
            info.setText(LocaleController.getString("TranslateMessagesInfo1", R.string.TranslateMessagesInfo1));
            info.setContentDescription(LocaleController.getString("TranslateMessagesInfo1", R.string.TranslateMessagesInfo1));
            addView(info, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            info2 = new TextInfoPrivacyCell(context);
            info2.setTopPadding(0);
            info2.setBottomPadding(16);
            info2.setFocusable(true);
            info2.setText(LocaleController.getString("TranslateMessagesInfo2", R.string.TranslateMessagesInfo2));
            info2.setContentDescription(LocaleController.getString("TranslateMessagesInfo2", R.string.TranslateMessagesInfo2));
            info2.setAlpha(value ? 0f : 1f);
            addView(info2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            updateHeight();
            update();
        }

        private boolean getValue() {
            return presenter != null && presenter.isTranslateButtonEnabled();
        }

        public void update() {
            boolean value = getValue() && LanguageDetector.hasSupport();

            showButtonCheck.setChecked(getValue());

            if (doNotTranslateCellAnimation != null) {
                doNotTranslateCellAnimation.cancel();
            }

            showButtonCheck.setDivider(value);
            ArrayList<String> langCodes = presenter != null ? presenter.getRestrictedLanguages() : new ArrayList<>();
            String doNotTranslateCellValue = null;
            if (langCodes.size() == 1 && presenter != null) {
                try {
                    LocaleController.LocaleInfo info = presenter.getLanguageFromDict(langCodes.get(0));
                    if (info != null) {
                        doNotTranslateCellValue = info.name;
                    }
                } catch (Exception e) {}
            }
            if (doNotTranslateCellValue == null) {
                doNotTranslateCellValue = String.format(LocaleController.getPluralString("Languages", langCodes.size()), langCodes.size());
            }
            doNotTranslateCell.setTextAndValue(LocaleController.getString("DoNotTranslate", R.string.DoNotTranslate), doNotTranslateCellValue, false);
            doNotTranslateCell.setClickable(value);

            info2.setVisibility(View.VISIBLE);
            doNotTranslateCellAnimation = ValueAnimator.ofFloat(doNotTranslateCell.getAlpha(), value ? 1f : 0f);
            doNotTranslateCellAnimation.setInterpolator(CubicBezierInterpolator.DEFAULT);
            doNotTranslateCellAnimation.addUpdateListener(a -> {
                float t = (float) a.getAnimatedValue();
                doNotTranslateCell.setAlpha(t);
                doNotTranslateCell.setTranslationY(-AndroidUtilities.dp(8) * (1f - t));
                info.setTranslationY(-doNotTranslateCell.getHeight() * (1f - t));
                info2.setAlpha(1f - t);
                info2.setTranslationY(-doNotTranslateCell.getHeight() * (1f - t));
            });
            doNotTranslateCellAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (doNotTranslateCell.getAlpha() > .5) {
                        info2.setVisibility(View.GONE);
                    } else {
                        info2.setVisibility(View.VISIBLE);
                    }
                }
            });
            doNotTranslateCellAnimation.setDuration((long) (Math.abs(doNotTranslateCell.getAlpha() - (value ? 1f : 0f)) * 200));
            doNotTranslateCellAnimation.start();
        }

        @Override
        protected void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            updateHeight();
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            updateHeight();
            super.onLayout(changed, l, t, r, b);
        }

        void updateHeight() {
            header.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.displaySize.x, MeasureSpec.EXACTLY), MeasureSpec.UNSPECIFIED);
            showButtonCheck.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.displaySize.x, MeasureSpec.EXACTLY), MeasureSpec.UNSPECIFIED);
            doNotTranslateCell.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.displaySize.x, MeasureSpec.EXACTLY), MeasureSpec.UNSPECIFIED);
            info.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.displaySize.x, MeasureSpec.EXACTLY), MeasureSpec.UNSPECIFIED);
            info2.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.displaySize.x, MeasureSpec.EXACTLY), MeasureSpec.UNSPECIFIED);

            int newHeight = searching ? 0 : height();
            if (getLayoutParams() == null) {
                setLayoutParams(new RecyclerView.LayoutParams(LayoutHelper.MATCH_PARENT, newHeight));
            } else if (getLayoutParams().height != newHeight) {
                RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) getLayoutParams();
                lp.height = newHeight;
                setLayoutParams(lp);
            }
        }

        int height() {
            return Math.max(AndroidUtilities.dp(40), header.getMeasuredHeight()) +
                   Math.max(AndroidUtilities.dp(50), showButtonCheck.getMeasuredHeight()) +
                   Math.max(Math.max(AndroidUtilities.dp(50), doNotTranslateCell.getMeasuredHeight()), (info2.getMeasuredHeight() <= 0 ? AndroidUtilities.dp(51) : info2.getMeasuredHeight())) +
                   (info.getMeasuredHeight() <= 0 ? AndroidUtilities.dp(62) : info.getMeasuredHeight());
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            updateHeight();
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            update();
            if (presenter != null) {
                listener = (sharedPreferences, s) -> update();
                presenter.registerSettingsChangeListener(listener);
            }
            updateHeight();
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            if (presenter != null && listener != null) {
                presenter.unregisterSettingsChangeListener(listener);
            }
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;
        private boolean search;

        public ListAdapter(Context context, boolean isSearch) {
            mContext = context;
            search = isSearch;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return holder.getItemViewType() == 0;
        }

        @Override
        public int getItemCount() {
            return presenter != null ? presenter.getItemCount(search) : 0;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0: {
                    view = new TextRadioCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                }
                case 2:
                    view = new TranslateSettings(mContext);
                    break;
                case 3:
                    HeaderCell header = new HeaderCell(mContext);
                    header.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    header.setText(LocaleController.getString("Language", R.string.Language));
                    view = header;
                    break;
                case 1:
                default: {
                    view = new ShadowSectionCell(mContext);
                    break;
                }
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0: {
                    if (presenter == null) {
                        return;
                    }
                    TextRadioCell textSettingsCell = (TextRadioCell) holder.itemView;
                    LocaleController.LocaleInfo localeInfo = presenter.getItem(position, search);
                    if (localeInfo == null) {
                        return;
                    }
                    boolean last = presenter.isLastItem(position, search);
                    if (localeInfo.isLocal()) {
                        textSettingsCell.setTextAndValueAndCheck(String.format("%1$s (%2$s)", localeInfo.name, LocaleController.getString("LanguageCustom", R.string.LanguageCustom)), localeInfo.nameEnglish, false, false, !last);
                    } else {
                        textSettingsCell.setTextAndValueAndCheck(localeInfo.name, localeInfo.nameEnglish, false, false, !last);
                    }
                    textSettingsCell.setChecked(presenter.isCurrentLocale(localeInfo));
                    break;
                }
                case 1: {
                    ShadowSectionCell sectionCell = (ShadowSectionCell) holder.itemView;
                    if (presenter != null && presenter.isDividerMiddle(position)) {
                        sectionCell.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    } else {
                        sectionCell.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                    }
                    break;
                }
                case 2: {
                    TranslateSettings translateSettings = (TranslateSettings) holder.itemView;
                    translateSettings.setVisibility(searching ? View.GONE : View.VISIBLE);
                    translateSettings.updateHeight();
                    break;
                }
            }
        }

        @Override
        public int getItemViewType(int i) {
            if (presenter == null) {
                return 0;
            }
            return presenter.getItemViewType(i, search);
        }
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{LanguageCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray));

        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCH, null, null, null, null, Theme.key_actionBarDefaultSearch));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, null, null, null, null, Theme.key_actionBarDefaultSearchPlaceholder));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector));

        themeDescriptions.add(new ThemeDescription(emptyView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_emptyListPlaceholder));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{LanguageCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{LanguageCell.class}, new String[]{"textView2"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText3));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{LanguageCell.class}, new String[]{"checkImage"}, null, null, null, Theme.key_featuredStickers_addedIcon));

        return themeDescriptions;
    }
}
