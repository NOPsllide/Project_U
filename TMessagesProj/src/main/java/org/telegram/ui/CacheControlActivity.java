/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.FilesMigrationService;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckBoxCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SlideChooseView;
import org.telegram.ui.Components.StorageDiagramView;
import org.telegram.ui.Components.StroageUsageView;
import org.telegram.core.presenter.CacheControlPresenter;
import org.telegram.ui.Components.UndoView;

import java.util.ArrayList;

public class CacheControlActivity extends BaseFragment implements CacheControlPresenter.CacheControlView {

    private CacheControlPresenter presenter;

    private ListAdapter listAdapter;
    private RecyclerListView listView;
    @SuppressWarnings("FieldCanBeLocal")
    private LinearLayoutManager layoutManager;
    AlertDialog progressDialog;

    private int databaseRow;
    private int databaseInfoRow;
    private int keepMediaHeaderRow;
    private int keepMediaInfoRow;
    private int cacheInfoRow;
    private int deviseStorageHeaderRow;
    private int storageUsageRow;
    private int keepMediaChooserRow;
    private int rowCount;

    private long databaseSize = -1;
    private long cacheSize = -1;
    private long documentsSize = -1;
    private long audioSize = -1;
    private long musicSize = -1;
    private long photoSize = -1;
    private long videoSize = -1;
    private long stickersSize = -1;
    private long totalSize = -1;
    private long totalDeviceSize = -1;
    private long totalDeviceFreeSize = -1;
    private long migrateOldFolderRow = -1;
    private StorageDiagramView.ClearViewData[] clearViewData = new StorageDiagramView.ClearViewData[7];
    private boolean calculating = true;

    private View bottomSheetView;
    private BottomSheet bottomSheet;
    private View actionTextView;

    private UndoView cacheRemovedTooltip;

    long fragmentCreateTime;

    private boolean updateDatabaseSize;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        presenter = new CacheControlPresenter(currentAccount);
        presenter.attachView(this);
        databaseSize = presenter.getDatabaseSize();
        presenter.calculateCacheSize();

        fragmentCreateTime = System.currentTimeMillis();
        updateRows();
        return true;
    }

    private void updateRows() {
        rowCount = 0;

        keepMediaHeaderRow = rowCount++;
        keepMediaChooserRow = rowCount++;
        keepMediaInfoRow = rowCount++;
        deviseStorageHeaderRow = rowCount++;
        storageUsageRow = rowCount++;

        cacheInfoRow = rowCount++;
        databaseRow = rowCount++;
        databaseInfoRow = rowCount++;
    }

    private void updateStorageUsageRow() {
        View view = layoutManager.findViewByPosition(storageUsageRow);
        if (view instanceof StroageUsageView) {
            StroageUsageView stroageUsageView = ((StroageUsageView) view);
            long currentTime = System.currentTimeMillis();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && currentTime - fragmentCreateTime > 250) {
                TransitionSet transition = new TransitionSet();
                ChangeBounds changeBounds = new ChangeBounds();
                changeBounds.setDuration(250);
                changeBounds.excludeTarget(stroageUsageView.legendLayout, true);
                Fade in = new Fade(Fade.IN);
                in.setDuration(290);
                transition
                        .addTransition(new Fade(Fade.OUT).setDuration(250))
                        .addTransition(changeBounds)
                        .addTransition(in);
                transition.setOrdering(TransitionSet.ORDERING_TOGETHER);
                transition.setInterpolator(CubicBezierInterpolator.EASE_OUT);
                TransitionManager.beginDelayedTransition(listView, transition);
            }
            stroageUsageView.setStorageUsage(calculating, databaseSize, totalSize, totalDeviceFreeSize, totalDeviceSize);
            RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(storageUsageRow);
            if (holder != null) {
                stroageUsageView.setEnabled(listAdapter.isEnabled(holder));
            }
        } else {
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (presenter != null) {
            presenter.detachView();
        }
        try {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {

        }
        progressDialog = null;
    }

    private void cleanupFolders() {
        progressDialog = new AlertDialog(getParentActivity(), 3);
        progressDialog.setCanCancel(false);
        progressDialog.showDelayed(500);

        boolean[] clearTypes = new boolean[7];
        long[] currentSizes = new long[]{photoSize, videoSize, documentsSize, musicSize, audioSize, stickersSize, cacheSize};
        for (int a = 0; a < 7; a++) {
            clearTypes[a] = clearViewData[a] != null && clearViewData[a].clear;
        }

        presenter.cleanupFolders(clearTypes, currentSizes);
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("StorageUsage", R.string.StorageUsage));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener((view, position) -> {
            if (getParentActivity() == null) {
                return;
            }
            if (position == migrateOldFolderRow) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    migrateOldFolder();
                }
            } else if (position == databaseRow) {
                clearDatabase();
            } else if (position == storageUsageRow) {
                if (totalSize <= 0 || getParentActivity() == null) {
                    return;
                }
                bottomSheet = new BottomSheet(getParentActivity(), false) {
                    @Override
                    protected boolean canDismissWithSwipe() {
                        return false;
                    }
                };
                bottomSheet.fixNavigationBar();
                bottomSheet.setAllowNestedScroll(true);
                bottomSheet.setApplyBottomPadding(false);
                LinearLayout linearLayout = new LinearLayout(getParentActivity());
                bottomSheetView = linearLayout;
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                StorageDiagramView circleDiagramView = new StorageDiagramView(context);
                linearLayout.addView(circleDiagramView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 16, 0, 16));
                CheckBoxCell lastCreatedCheckbox = null;
                for (int a = 0; a < 7; a++) {
                    long size;
                    String name;
                    String color;
                    if (a == 0) {
                        size = photoSize;
                        name = LocaleController.getString("LocalPhotoCache", R.string.LocalPhotoCache);
                        color = Theme.key_statisticChartLine_blue;
                    } else if (a == 1) {
                        size = videoSize;
                        name = LocaleController.getString("LocalVideoCache", R.string.LocalVideoCache);
                        color = Theme.key_statisticChartLine_golden;
                    } else if (a == 2) {
                        size = documentsSize;
                        name = LocaleController.getString("LocalDocumentCache", R.string.LocalDocumentCache);
                        color = Theme.key_statisticChartLine_green;
                    } else if (a == 3) {
                        size = musicSize;
                        name = LocaleController.getString("LocalMusicCache", R.string.LocalMusicCache);
                        color = Theme.key_statisticChartLine_indigo;
                    } else if (a == 4) {
                        size = audioSize;
                        name = LocaleController.getString("LocalAudioCache", R.string.LocalAudioCache);
                        color = Theme.key_statisticChartLine_red;
                    } else if (a == 5) {
                        size = stickersSize;
                        name = LocaleController.getString("AnimatedStickers", R.string.AnimatedStickers);
                        color = Theme.key_statisticChartLine_lightgreen;
                    } else {
                        size = cacheSize;
                        name = LocaleController.getString("LocalCache", R.string.LocalCache);
                        color = Theme.key_statisticChartLine_lightblue;
                    }
                    if (size > 0) {
                        clearViewData[a] = new StorageDiagramView.ClearViewData(circleDiagramView);
                        clearViewData[a].size = size;
                        clearViewData[a].color = color;
                        CheckBoxCell checkBoxCell = new CheckBoxCell(getParentActivity(), 4, 21, null);
                        lastCreatedCheckbox = checkBoxCell;
                        checkBoxCell.setTag(a);
                        checkBoxCell.setBackgroundDrawable(Theme.getSelectorDrawable(false));
                        linearLayout.addView(checkBoxCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 50));
                        checkBoxCell.setText(name, AndroidUtilities.formatFileSize(size), true, true);
                        checkBoxCell.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
                        checkBoxCell.setCheckBoxColor(color, Theme.key_windowBackgroundWhiteGrayIcon, Theme.key_checkboxCheck);
                        checkBoxCell.setOnClickListener(v -> {
                            int enabledCount = 0;
                            for (int i = 0; i < clearViewData.length; i++) {
                                if (clearViewData[i] != null && clearViewData[i].clear) {
                                    enabledCount++;
                                }
                            }
                            CheckBoxCell cell = (CheckBoxCell) v;
                            int num = (Integer) cell.getTag();
                            if (enabledCount == 1 && clearViewData[num].clear) {
                                AndroidUtilities.shakeView(((CheckBoxCell) v).getCheckBoxView());
                                return;
                            }


                            clearViewData[num].setClear(!clearViewData[num].clear);
                            cell.setChecked(clearViewData[num].clear, true);
                        });
                    } else {
                        clearViewData[a] = null;
                    }
                }
                if (lastCreatedCheckbox != null) {
                    lastCreatedCheckbox.setNeedDivider(false);
                }
                circleDiagramView.setData(clearViewData);
                BottomSheet.BottomSheetCell cell = new BottomSheet.BottomSheetCell(getParentActivity(), 2);
                cell.setTextAndIcon(LocaleController.getString("ClearMediaCache", R.string.ClearMediaCache), 0);
                actionTextView = cell.getTextView();
                cell.getTextView().setOnClickListener(v -> {
                    try {
                        if (visibleDialog != null) {
                            visibleDialog.dismiss();
                        }
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                    cleanupFolders();
                });
                linearLayout.addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 50));
                NestedScrollView scrollView = new NestedScrollView(context);
                scrollView.setVerticalScrollBarEnabled(false);
                scrollView.addView(linearLayout);
                bottomSheet.setCustomView(scrollView);
                showDialog(bottomSheet);
            }
        });

        cacheRemovedTooltip = new UndoView(context);
        frameLayout.addView(cacheRemovedTooltip, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.LEFT, 8, 0, 8, 8));

        return fragmentView;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void migrateOldFolder() {
        FilesMigrationService.checkBottomSheet(this);
    }

    private void clearDatabase() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(LocaleController.getString("LocalDatabaseClearTextTitle", R.string.LocalDatabaseClearTextTitle));
        builder.setMessage(LocaleController.getString("LocalDatabaseClearText", R.string.LocalDatabaseClearText));
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        builder.setPositiveButton(LocaleController.getString("CacheClear", R.string.CacheClear), (dialogInterface, i) -> {
            if (getParentActivity() == null) {
                return;
            }
            progressDialog = new AlertDialog(getParentActivity(), 3);
            progressDialog.setCanCancel(false);
            progressDialog.showDelayed(500);
            presenter.clearLocalDatabase();
        });
        AlertDialog alertDialog = builder.create();
        showDialog(alertDialog);
        TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (button != null) {
            button.setTextColor(Theme.getColor(Theme.key_dialogTextRed2));
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
    public void onCacheCalculated(long cacheSize, long photoSize, long videoSize, long documentsSize, long musicSize, long stickersSize, long audioSize, long totalSize, long totalDeviceSize, long totalDeviceFreeSize) {
        this.cacheSize = cacheSize;
        this.photoSize = photoSize;
        this.videoSize = videoSize;
        this.documentsSize = documentsSize;
        this.musicSize = musicSize;
        this.stickersSize = stickersSize;
        this.audioSize = audioSize;
        this.totalSize = totalSize;
        this.totalDeviceSize = totalDeviceSize;
        this.totalDeviceFreeSize = totalDeviceFreeSize;
        calculating = false;
        updateStorageUsageRow();
    }

    @Override
    public void onCacheCleaned(long clearedSize, long cacheSize, long photoSize, long videoSize, long documentsSize, long musicSize, long stickersSize, long audioSize, long totalSize, long totalDeviceSize, long totalDeviceFreeSize, boolean imagesCleared) {
        this.cacheSize = cacheSize;
        this.photoSize = photoSize;
        this.videoSize = videoSize;
        this.documentsSize = documentsSize;
        this.musicSize = musicSize;
        this.stickersSize = stickersSize;
        this.audioSize = audioSize;
        this.totalSize = totalSize;
        this.totalDeviceSize = totalDeviceSize;
        this.totalDeviceFreeSize = totalDeviceFreeSize;

        if (listAdapter != null) {
            updateStorageUsageRow();
        }
        try {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        progressDialog = null;

        cacheRemovedTooltip.setInfoText(LocaleController.formatString("CacheWasCleared", R.string.CacheWasCleared, AndroidUtilities.formatFileSize(clearedSize)));
        cacheRemovedTooltip.showWithAction(0, UndoView.ACTION_CACHE_WAS_CLEARED, null, null);
    }

    @Override
    public void onDatabaseCleared(long newDatabaseSize) {
        try {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        progressDialog = null;
        databaseSize = newDatabaseSize;
        updateDatabaseSize = true;
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int position = holder.getAdapterPosition();
            return position == migrateOldFolderRow || position == databaseRow || (position == storageUsageRow && (totalSize > 0) && !calculating);
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 2:
                    view = new StroageUsageView(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 3:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 4:
                    SlideChooseView slideChooseView = new SlideChooseView(mContext);
                    view = slideChooseView;
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    slideChooseView.setCallback(index -> {
                        if (index == 0) {
                            presenter.setKeepMedia(3);
                        } else if (index == 1) {
                            presenter.setKeepMedia(0);
                        } else if (index == 2) {
                            presenter.setKeepMedia(1);
                        } else if (index == 3) {
                            presenter.setKeepMedia(2);
                        }
                    });
                    int keepMedia = presenter.getKeepMedia();
                    int index;
                    if (keepMedia == 3) {
                        index = 0;
                    } else {
                        index = keepMedia + 1;
                    }
                    slideChooseView.setOptions(index, LocaleController.formatPluralString("Days", 3), LocaleController.formatPluralString("Weeks", 1), LocaleController.formatPluralString("Months", 1), LocaleController.getString("KeepMediaForever", R.string.KeepMediaForever));
                    break;
                case 1:
                default:
                    view = new TextInfoPrivacyCell(mContext);
                    break;
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0:
                    TextSettingsCell textCell = (TextSettingsCell) holder.itemView;
                    if (position == databaseRow) {
                        textCell.setTextAndValue(LocaleController.getString("ClearLocalDatabase", R.string.ClearLocalDatabase), AndroidUtilities.formatFileSize(databaseSize), updateDatabaseSize, false);
                        updateDatabaseSize = false;
                    } else if (position == migrateOldFolderRow) {
                        textCell.setTextAndValue(LocaleController.getString("MigrateOldFolder", R.string.MigrateOldFolder), null, false);
                    }
                    break;
                case 1:
                    TextInfoPrivacyCell privacyCell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == databaseInfoRow) {
                        privacyCell.setText(LocaleController.getString("LocalDatabaseInfo", R.string.LocalDatabaseInfo));
                        privacyCell.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                    } else if (position == cacheInfoRow) {
                        privacyCell.setText("");
                        privacyCell.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    } else if (position == keepMediaInfoRow) {
                        privacyCell.setText(AndroidUtilities.replaceTags(LocaleController.getString("KeepMediaInfo", R.string.KeepMediaInfo)));
                        privacyCell.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    }
                    break;
                case 2:
                    StroageUsageView stroageUsageView = (StroageUsageView) holder.itemView;
                    stroageUsageView.setStorageUsage(calculating, databaseSize, totalSize, totalDeviceFreeSize, totalDeviceSize);
                    break;
                case 3:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == keepMediaHeaderRow) {
                        headerCell.setText(LocaleController.getString("KeepMedia", R.string.KeepMedia));
                    } else if (position == deviseStorageHeaderRow) {
                        headerCell.setText(LocaleController.getString("DeviceStorage", R.string.DeviceStorage));
                    }
                    break;
            }
        }

        @Override
        public int getItemViewType(int i) {
            if (i == databaseInfoRow || i == cacheInfoRow || i == keepMediaInfoRow) {
                return 1;
            }
            if (i == storageUsageRow) {
                return 2;
            }
            if (i == keepMediaHeaderRow || i == deviseStorageHeaderRow) {
                return 3;
            }
            if (i == keepMediaChooserRow) {
                return 4;
            }
            return 0;
        }
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ThemeDescription.ThemeDescriptionDelegate deldegagte = () -> {
            if (bottomSheet != null) {
                bottomSheet.setBackgroundColor(Theme.getColor(Theme.key_dialogBackground));
            }

            if (actionTextView != null) {
                actionTextView.setBackground(Theme.AdaptiveRipple.filledRect(Theme.key_featuredStickers_addButton, 4));
            }
        };
        ArrayList<ThemeDescription> arrayList = new ArrayList<>();

        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{TextSettingsCell.class, SlideChooseView.class, StroageUsageView.class, HeaderCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        arrayList.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray));

        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault));
        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon));
        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle));
        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector));

        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector));

        arrayList.add(new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText));

        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{StroageUsageView.class}, new String[]{"paintFill"}, null, null, null, Theme.key_player_progressBackground));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{StroageUsageView.class}, new String[]{"paintProgress"}, null, null, null, Theme.key_player_progress));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{StroageUsageView.class}, new String[]{"telegramCacheTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{StroageUsageView.class}, new String[]{"freeSizeTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{StroageUsageView.class}, new String[]{"calculationgTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{StroageUsageView.class}, new String[]{"paintProgress2"}, null, null, null, Theme.key_player_progressBackground2));

        arrayList.add(new ThemeDescription(listView, 0, new Class[]{SlideChooseView.class}, null, null, null, Theme.key_switchTrack));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{SlideChooseView.class}, null, null, null, Theme.key_switchTrackChecked));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{SlideChooseView.class}, null, null, null, Theme.key_windowBackgroundWhiteGrayText));

        arrayList.add(new ThemeDescription(bottomSheetView, 0, null, null, null, null, Theme.key_windowBackgroundWhiteGrayText));
        arrayList.add(new ThemeDescription(bottomSheetView, 0, new Class[]{CheckBoxCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(bottomSheetView, 0, new Class[]{CheckBoxCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText));
        arrayList.add(new ThemeDescription(bottomSheetView, 0, new Class[]{CheckBoxCell.class}, Theme.dividerPaint, null, null, Theme.key_divider));
        arrayList.add(new ThemeDescription(bottomSheetView, 0, new Class[]{StorageDiagramView.class}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));

        arrayList.add(new ThemeDescription(null, 0, new Class[]{TextCheckBoxCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, deldegagte, Theme.key_dialogBackground));

        arrayList.add(new ThemeDescription(bottomSheetView, 0, null, null, null, null, Theme.key_statisticChartLine_blue));
        arrayList.add(new ThemeDescription(bottomSheetView, 0, null, null, null, null, Theme.key_statisticChartLine_green));
        arrayList.add(new ThemeDescription(bottomSheetView, 0, null, null, null, null, Theme.key_statisticChartLine_red));
        arrayList.add(new ThemeDescription(bottomSheetView, 0, null, null, null, null, Theme.key_statisticChartLine_golden));
        arrayList.add(new ThemeDescription(bottomSheetView, 0, null, null, null, null, Theme.key_statisticChartLine_lightblue));
        arrayList.add(new ThemeDescription(bottomSheetView, 0, null, null, null, null, Theme.key_statisticChartLine_lightgreen));
        arrayList.add(new ThemeDescription(bottomSheetView, 0, null, null, null, null, Theme.key_statisticChartLine_orange));
        arrayList.add(new ThemeDescription(bottomSheetView, 0, null, null, null, null, Theme.key_statisticChartLine_indigo));
        return arrayList;
    }

    @Override
    public void onRequestPermissionsResultFragment(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 4) {
            boolean allGranted = true;
            for (int a = 0; a < grantResults.length; a++) {
                if (grantResults[a] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && FilesMigrationService.filesMigrationBottomSheet != null) {
                FilesMigrationService.filesMigrationBottomSheet.migrateOldFolder();
            }

        }
    }
}
