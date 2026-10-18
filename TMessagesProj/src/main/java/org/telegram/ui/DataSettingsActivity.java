/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.DownloadController;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.voip.Instance;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.RadioColorCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.core.presenter.DataSettingsPresenter;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.voip.VoIPHelper;

import java.io.File;
import java.util.ArrayList;

public class DataSettingsActivity extends BaseFragment implements DataSettingsPresenter.DataSettingsView {

    private DataSettingsPresenter presenter;

    private ListAdapter listAdapter;
    private RecyclerListView listView;
    @SuppressWarnings("FieldCanBeLocal")
    private LinearLayoutManager layoutManager;

    private ArrayList<File> storageDirs;

    private int mediaDownloadSectionRow;
    private int mobileRow;
    private int roamingRow;
    private int wifiRow;
    private int storageNumRow;
    private int resetDownloadRow;
    private int mediaDownloadSection2Row;
    private int usageSectionRow;
    private int storageUsageRow;
    private int dataUsageRow;
    private int usageSection2Row;
    private int streamSectionRow;
    private int enableStreamRow;
    private int enableCacheStreamRow;
    private int enableAllStreamRow;
    private int enableMkvRow;
    private int enableAllStreamInfoRow;
    private int autoplayHeaderRow;
    private int autoplayGifsRow;
    private int autoplayVideoRow;
    private int autoplaySectionRow;
    private int callsSectionRow;
    private int useLessDataForCallsRow;
    private int quickRepliesRow;
    private int callsSection2Row;
    private int proxySectionRow;
    private int proxyRow;
    private int proxySection2Row;
    private int clearDraftsRow;
    private int clearDraftsSectionRow;
    private int saveToGallerySectionRow;
    private int saveToGalleryPeerRow;
    private int saveToGalleryChannelsRow;
    private int saveToGalleryGroupsRow;
    private int saveToGalleryDividerRow;

    private int rowCount;

    private boolean updateVoipUseLessData;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        presenter = new DataSettingsPresenter(currentAccount);
        presenter.attachView(this);
        presenter.loadAutoDownloadConfig(true);

        rowCount = 0;
        usageSectionRow = rowCount++;
        storageUsageRow = rowCount++;
        dataUsageRow = rowCount++;
        storageNumRow = -1;
        if (Build.VERSION.SDK_INT >= 19) {
            storageDirs = AndroidUtilities.getRootDirs();
            if (storageDirs.size() > 1) {
                storageNumRow = rowCount++;
            }
        }
        usageSection2Row = rowCount++;
        mediaDownloadSectionRow = rowCount++;
        mobileRow = rowCount++;
        wifiRow = rowCount++;
        roamingRow = rowCount++;
        resetDownloadRow = rowCount++;
        mediaDownloadSection2Row = rowCount++;

        saveToGallerySectionRow = rowCount++;
        saveToGalleryPeerRow = rowCount++;
        saveToGalleryGroupsRow = rowCount++;
        saveToGalleryChannelsRow = rowCount++;
        saveToGalleryDividerRow = rowCount++;

        autoplayHeaderRow = rowCount++;
        autoplayGifsRow = rowCount++;
        autoplayVideoRow = rowCount++;
        autoplaySectionRow = rowCount++;
        streamSectionRow = rowCount++;
        enableStreamRow = rowCount++;
        if (BuildVars.DEBUG_VERSION) {
            enableMkvRow = rowCount++;
            enableAllStreamRow = rowCount++;
        } else {
            enableAllStreamRow = -1;
            enableMkvRow = -1;
        }
        enableAllStreamInfoRow = rowCount++;

        enableCacheStreamRow = -1;
        callsSectionRow = rowCount++;
        useLessDataForCallsRow = rowCount++;
        quickRepliesRow = rowCount++;
        callsSection2Row = rowCount++;
        proxySectionRow = rowCount++;
        proxyRow = rowCount++;
        proxySection2Row = rowCount++;
        clearDraftsRow = rowCount++;
        clearDraftsSectionRow = rowCount++;

        return true;
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
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString("DataSettings", R.string.DataSettings));
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setAllowOverlayTitle(true);
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
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener((view, position, x, y) -> {
            if (position == saveToGalleryGroupsRow || position == saveToGalleryChannelsRow || position == saveToGalleryPeerRow) {
                int flag;
                if (position == saveToGalleryGroupsRow) {
                    flag = SharedConfig.SAVE_TO_GALLERY_FLAG_GROUP;
                } else if (position == saveToGalleryChannelsRow) {
                    flag = SharedConfig.SAVE_TO_GALLERY_FLAG_CHANNELS;
                } else {
                    flag = SharedConfig.SAVE_TO_GALLERY_FLAG_PEER;
                }
                if (presenter != null) {
                    presenter.toggleSaveToGalleryFlag(flag);
                } else {
                    SharedConfig.toggleSaveToGalleryFlag(flag);
                }
                TextCheckCell textCheckCell = (TextCheckCell) view;
                int flags = presenter != null ? presenter.getSaveToGalleryFlags() : SharedConfig.saveToGalleryFlags;
                textCheckCell.setChecked((flags & flag) != 0);
            } else if (position == mobileRow || position == roamingRow || position == wifiRow) {
                if (LocaleController.isRTL && x <= AndroidUtilities.dp(76) || !LocaleController.isRTL && x >= view.getMeasuredWidth() - AndroidUtilities.dp(76)) {
                    boolean wasEnabled = listAdapter.isRowEnabled(resetDownloadRow);

                    NotificationsCheckCell cell = (NotificationsCheckCell) view;
                    boolean checked = cell.isChecked();

                    int type;
                    if (position == mobileRow) {
                        type = 0;
                    } else if (position == wifiRow) {
                        type = 1;
                    } else {
                        type = 2;
                    }

                    boolean newChecked = presenter != null ? presenter.toggleAutoDownload(type, checked) : !checked;
                    cell.setChecked(newChecked);
                    RecyclerView.ViewHolder holder = listView.findContainingViewHolder(view);
                    if (holder != null) {
                        listAdapter.onBindViewHolder(holder, position);
                    }
                    if (wasEnabled != listAdapter.isRowEnabled(resetDownloadRow)) {
                        listAdapter.notifyItemChanged(resetDownloadRow);
                    }
                } else {
                    int type;
                    if (position == mobileRow) {
                        type = 0;
                    } else if (position == wifiRow) {
                        type = 1;
                    } else {
                        type = 2;
                    }
                    presentFragment(new DataAutoDownloadActivity(type));
                }
            } else if (position == resetDownloadRow) {
                if (getParentActivity() == null || !view.isEnabled()) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("ResetAutomaticMediaDownloadAlertTitle", R.string.ResetAutomaticMediaDownloadAlertTitle));
                builder.setMessage(LocaleController.getString("ResetAutomaticMediaDownloadAlert", R.string.ResetAutomaticMediaDownloadAlert));
                builder.setPositiveButton(LocaleController.getString("Reset", R.string.Reset), (dialogInterface, i) -> {
                    if (presenter != null) {
                        presenter.resetAutoDownload();
                    }
                });
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                AlertDialog dialog = builder.create();
                showDialog(dialog);
                TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                if (button != null) {
                    button.setTextColor(Theme.getColor(Theme.key_dialogTextRed2));
                }
            } else if (position == storageUsageRow) {
                presentFragment(new CacheControlActivity());
            } else if (position == useLessDataForCallsRow) {
                int saving = presenter != null ? presenter.getVoipDataSaving() : MessagesController.getGlobalMainSettings().getInt("VoipDataSaving", VoIPHelper.getDataSavingDefault());
                int selected = 0;
                switch (saving) {
                    case Instance.DATA_SAVING_NEVER:
                        selected = 0;
                        break;
                    case Instance.DATA_SAVING_ROAMING:
                        selected = 1;
                        break;
                    case Instance.DATA_SAVING_MOBILE:
                        selected = 2;
                        break;
                    case Instance.DATA_SAVING_ALWAYS:
                        selected = 3;
                        break;
                }
                Dialog dlg = AlertsCreator.createSingleChoiceDialog(getParentActivity(), new String[]{
                                LocaleController.getString("UseLessDataNever", R.string.UseLessDataNever),
                                LocaleController.getString("UseLessDataOnRoaming", R.string.UseLessDataOnRoaming),
                                LocaleController.getString("UseLessDataOnMobile", R.string.UseLessDataOnMobile),
                                LocaleController.getString("UseLessDataAlways", R.string.UseLessDataAlways)},
                        LocaleController.getString("VoipUseLessData", R.string.VoipUseLessData), selected, (dialog, which) -> {
                            int val = -1;
                            switch (which) {
                                case 0:
                                    val = Instance.DATA_SAVING_NEVER;
                                    break;
                                case 1:
                                    val = Instance.DATA_SAVING_ROAMING;
                                    break;
                                case 2:
                                    val = Instance.DATA_SAVING_MOBILE;
                                    break;
                                case 3:
                                    val = Instance.DATA_SAVING_ALWAYS;
                                    break;
                            }
                            if (val != -1) {
                                if (presenter != null) {
                                    presenter.setVoipDataSaving(val);
                                } else {
                                    MessagesController.getGlobalMainSettings().edit().putInt("VoipDataSaving", val).commit();
                                }
                                updateVoipUseLessData = true;
                            }
                            if (listAdapter != null) {
                                listAdapter.notifyItemChanged(position);
                            }
                        });
                setVisibleDialog(dlg);
                dlg.show();
            } else if (position == dataUsageRow) {
                presentFragment(new DataUsageActivity());
            } else if (position == storageNumRow) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("StoragePath", R.string.StoragePath));
                final LinearLayout linearLayout = new LinearLayout(getParentActivity());
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                builder.setView(linearLayout);

                String cacheDir = presenter != null ? presenter.getStorageCacheDir() : SharedConfig.storageCacheDir;
                String dir = storageDirs.get(0).getAbsolutePath();
                if (!TextUtils.isEmpty(cacheDir)) {
                    for (int a = 0, N = storageDirs.size(); a < N; a++) {
                        String path = storageDirs.get(a).getAbsolutePath();
                        if (path.startsWith(cacheDir)) {
                            dir = path;
                            break;
                        }
                    }
                }

                for (int a = 0, N = storageDirs.size(); a < N; a++) {
                    String storageDir = storageDirs.get(a).getAbsolutePath();
                    RadioColorCell cell = new RadioColorCell(context);
                    cell.setPadding(AndroidUtilities.dp(4), 0, AndroidUtilities.dp(4), 0);
                    cell.setTag(a);
                    cell.setCheckColor(Theme.getColor(Theme.key_radioBackground), Theme.getColor(Theme.key_dialogRadioBackgroundChecked));
                    cell.setTextAndValue(storageDir, storageDir.startsWith(dir));
                    linearLayout.addView(cell);
                    cell.setOnClickListener(v -> {
                        if (presenter != null) {
                            presenter.setStorageCacheDir(storageDir);
                        } else {
                            SharedConfig.storageCacheDir = storageDir;
                            SharedConfig.saveConfig();
                            ImageLoader.getInstance().checkMediaPaths();
                        }
                        builder.getDismissRunnable().run();
                        listAdapter.notifyItemChanged(storageNumRow);
                    });
                }
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                showDialog(builder.create());
            } else if (position == proxyRow) {
                presentFragment(new ProxyListActivity());
            } else if (position == enableStreamRow) {
                if (presenter != null) {
                    presenter.toggleStreamMedia();
                } else {
                    SharedConfig.toggleStreamMedia();
                }
                TextCheckCell textCheckCell = (TextCheckCell) view;
                textCheckCell.setChecked(presenter != null ? presenter.isStreamMediaEnabled() : SharedConfig.streamMedia);
            } else if (position == enableAllStreamRow) {
                if (presenter != null) {
                    presenter.toggleStreamAllVideo();
                } else {
                    SharedConfig.toggleStreamAllVideo();
                }
                TextCheckCell textCheckCell = (TextCheckCell) view;
                textCheckCell.setChecked(presenter != null ? presenter.isStreamAllVideoEnabled() : SharedConfig.streamAllVideo);
            } else if (position == enableMkvRow) {
                if (presenter != null) {
                    presenter.toggleStreamMkv();
                } else {
                    SharedConfig.toggleStreamMkv();
                }
                TextCheckCell textCheckCell = (TextCheckCell) view;
                textCheckCell.setChecked(presenter != null ? presenter.isStreamMkvEnabled() : SharedConfig.streamMkv);
            } else if (position == enableCacheStreamRow) {
                if (presenter != null) {
                    presenter.toggleSaveStreamMedia();
                } else {
                    SharedConfig.toggleSaveStreamMedia();
                }
                TextCheckCell textCheckCell = (TextCheckCell) view;
                textCheckCell.setChecked(presenter != null ? presenter.isSaveStreamMediaEnabled() : SharedConfig.saveStreamMedia);
            } else if (position == quickRepliesRow) {
                presentFragment(new QuickRepliesSettingsActivity());
            } else if (position == autoplayGifsRow) {
                if (presenter != null) {
                    presenter.toggleAutoplayGifs();
                } else {
                    SharedConfig.toggleAutoplayGifs();
                }
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(presenter != null ? presenter.isAutoplayGifsEnabled() : SharedConfig.autoplayGifs);
                }
            } else if (position == autoplayVideoRow) {
                if (presenter != null) {
                    presenter.toggleAutoplayVideo();
                } else {
                    SharedConfig.toggleAutoplayVideo();
                }
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(presenter != null ? presenter.isAutoplayVideoEnabled() : SharedConfig.autoplayVideo);
                }
            } else if (position == clearDraftsRow) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("AreYouSureClearDraftsTitle", R.string.AreYouSureClearDraftsTitle));
                builder.setMessage(LocaleController.getString("AreYouSureClearDrafts", R.string.AreYouSureClearDrafts));
                builder.setPositiveButton(LocaleController.getString("Delete", R.string.Delete), (dialogInterface, i) -> {
                    if (presenter != null) {
                        presenter.clearAllDrafts();
                    } else {
                        TLRPC.TL_messages_clearAllDrafts req = new TLRPC.TL_messages_clearAllDrafts();
                        getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> getMediaDataController().clearAllDrafts(true)));
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
        });

        return fragmentView;
    }

    @Override
    protected void onDialogDismiss(Dialog dialog) {
        if (presenter != null) {
            presenter.checkAutodownloadSettings();
        } else {
            DownloadController.getInstance(currentAccount).checkAutodownloadSettings();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
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
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0: {
                    if (position == clearDraftsSectionRow) {
                        holder.itemView.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                    } else {
                        holder.itemView.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    }
                    break;
                }
                case 1: {
                    TextSettingsCell textCell = (TextSettingsCell) holder.itemView;
                    textCell.setCanDisable(false);
                    textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    if (position == storageUsageRow) {
                        textCell.setText(LocaleController.getString("StorageUsage", R.string.StorageUsage), true);
                    } else if (position == useLessDataForCallsRow) {
                        int saving = presenter != null ? presenter.getVoipDataSaving() : MessagesController.getGlobalMainSettings().getInt("VoipDataSaving", VoIPHelper.getDataSavingDefault());
                        String value = null;
                        switch (saving) {
                            case Instance.DATA_SAVING_NEVER:
                                value = LocaleController.getString("UseLessDataNever", R.string.UseLessDataNever);
                                break;
                            case Instance.DATA_SAVING_MOBILE:
                                value = LocaleController.getString("UseLessDataOnMobile", R.string.UseLessDataOnMobile);
                                break;
                            case Instance.DATA_SAVING_ROAMING:
                                value = LocaleController.getString("UseLessDataOnRoaming", R.string.UseLessDataOnRoaming);
                                break;
                            case Instance.DATA_SAVING_ALWAYS:
                                value = LocaleController.getString("UseLessDataAlways", R.string.UseLessDataAlways);
                                break;
                        }
                        textCell.setTextAndValue(LocaleController.getString("VoipUseLessData", R.string.VoipUseLessData), value, updateVoipUseLessData, true);
                        updateVoipUseLessData = false;
                    } else if (position == dataUsageRow) {
                        textCell.setText(LocaleController.getString("NetworkUsage", R.string.NetworkUsage), storageNumRow != -1);
                    } else if (position == storageNumRow) {
                        String cacheDir = presenter != null ? presenter.getStorageCacheDir() : SharedConfig.storageCacheDir;
                        String dir = storageDirs.get(0).getAbsolutePath();
                        if (!TextUtils.isEmpty(cacheDir)) {
                            for (int a = 0, N = storageDirs.size(); a < N; a++) {
                                String path = storageDirs.get(a).getAbsolutePath();
                                if (path.startsWith(cacheDir)) {
                                    dir = path;
                                    break;
                                }
                            }
                        }
                        textCell.setTextAndValue(LocaleController.getString("StoragePath", R.string.StoragePath), dir, false);
                    } else if (position == proxyRow) {
                        textCell.setText(LocaleController.getString("ProxySettings", R.string.ProxySettings), false);
                    } else if (position == resetDownloadRow) {
                        textCell.setCanDisable(true);
                        textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText));
                        textCell.setText(LocaleController.getString("ResetAutomaticMediaDownload", R.string.ResetAutomaticMediaDownload), false);
                    } else if (position == quickRepliesRow){
                        textCell.setText(LocaleController.getString("VoipQuickReplies", R.string.VoipQuickReplies), false);
                    } else if (position == clearDraftsRow) {
                        textCell.setText(LocaleController.getString("PrivacyDeleteCloudDrafts", R.string.PrivacyDeleteCloudDrafts), false);
                    }
                    break;
                }
                case 2: {
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == mediaDownloadSectionRow) {
                        headerCell.setText(LocaleController.getString("AutomaticMediaDownload", R.string.AutomaticMediaDownload));
                    } else if (position == usageSectionRow) {
                        headerCell.setText(LocaleController.getString("DataUsage", R.string.DataUsage));
                    } else if (position == callsSectionRow) {
                        headerCell.setText(LocaleController.getString("Calls", R.string.Calls));
                    } else if (position == proxySectionRow) {
                        headerCell.setText(LocaleController.getString("Proxy", R.string.Proxy));
                    } else if (position == streamSectionRow) {
                        headerCell.setText(LocaleController.getString("Streaming", R.string.Streaming));
                    } else if (position == autoplayHeaderRow) {
                        headerCell.setText(LocaleController.getString("AutoplayMedia", R.string.AutoplayMedia));
                    } else if (position == saveToGallerySectionRow) {
                        headerCell.setText(LocaleController.getString("SaveToGallery", R.string.SaveToGallery));
                    }
                    break;
                }
                case 3: {
                    TextCheckCell checkCell = (TextCheckCell) holder.itemView;
                    if (position == enableStreamRow) {
                        boolean enabled = presenter != null ? presenter.isStreamMediaEnabled() : SharedConfig.streamMedia;
                        checkCell.setTextAndCheck(LocaleController.getString("EnableStreaming", R.string.EnableStreaming), enabled, enableAllStreamRow != -1);
                    } else if (position == enableCacheStreamRow) {
                    } else if (position == enableMkvRow) {
                        boolean enabled = presenter != null ? presenter.isStreamMkvEnabled() : SharedConfig.streamMkv;
                        checkCell.setTextAndCheck("(beta only) Show MKV as Video", enabled, true);
                    } else if (position == enableAllStreamRow) {
                        boolean enabled = presenter != null ? presenter.isStreamAllVideoEnabled() : SharedConfig.streamAllVideo;
                        checkCell.setTextAndCheck("(beta only) Stream All Videos", enabled, false);
                    } else if (position == autoplayGifsRow) {
                        boolean enabled = presenter != null ? presenter.isAutoplayGifsEnabled() : SharedConfig.autoplayGifs;
                        checkCell.setTextAndCheck(LocaleController.getString("AutoplayGIF", R.string.AutoplayGIF), enabled, true);
                    } else if (position == autoplayVideoRow) {
                        boolean enabled = presenter != null ? presenter.isAutoplayVideoEnabled() : SharedConfig.autoplayVideo;
                        checkCell.setTextAndCheck(LocaleController.getString("AutoplayVideo", R.string.AutoplayVideo), enabled, false);
                    } else if (position == saveToGalleryPeerRow) {
                        int flags = presenter != null ? presenter.getSaveToGalleryFlags() : SharedConfig.saveToGalleryFlags;
                        checkCell.setTextAndCheck(LocaleController.getString("SaveToGalleryPrivate", R.string.SaveToGalleryPrivate), (flags & SharedConfig.SAVE_TO_GALLERY_FLAG_PEER) != 0, true);
                    } else if (position == saveToGalleryGroupsRow) {
                        int flags = presenter != null ? presenter.getSaveToGalleryFlags() : SharedConfig.saveToGalleryFlags;
                        checkCell.setTextAndCheck(LocaleController.getString("SaveToGalleryGroups", R.string.SaveToGalleryGroups), (flags & SharedConfig.SAVE_TO_GALLERY_FLAG_GROUP) != 0, true);
                    } else if (position == saveToGalleryChannelsRow) {
                        int flags = presenter != null ? presenter.getSaveToGalleryFlags() : SharedConfig.saveToGalleryFlags;
                        checkCell.setTextAndCheck(LocaleController.getString("SaveToGalleryChannels", R.string.SaveToGalleryChannels), (flags & SharedConfig.SAVE_TO_GALLERY_FLAG_CHANNELS) != 0, false);
                    }
                    break;
                }
                case 4: {
                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == enableAllStreamInfoRow) {
                        cell.setText(LocaleController.getString("EnableAllStreamingInfo", R.string.EnableAllStreamingInfo));
                    }
                    break;
                }
                case 5: {
                    NotificationsCheckCell checkCell = (NotificationsCheckCell) holder.itemView;

                    String text;
                    StringBuilder builder = new StringBuilder();
                    DownloadController.Preset preset;
                    boolean enabled;
                    int type = position == mobileRow ? 0 : (position == wifiRow ? 1 : 2);
                    if (position == mobileRow) {
                        text = LocaleController.getString("WhenUsingMobileData", R.string.WhenUsingMobileData);
                    } else if (position == wifiRow) {
                        text = LocaleController.getString("WhenConnectedOnWiFi", R.string.WhenConnectedOnWiFi);
                    } else {
                        text = LocaleController.getString("WhenRoaming", R.string.WhenRoaming);
                    }
                    if (presenter != null) {
                        enabled = presenter.getPreset(type).enabled;
                        preset = presenter.getCurrentPreset(type);
                    } else {
                        if (position == mobileRow) {
                            enabled = DownloadController.getInstance(currentAccount).mobilePreset.enabled;
                            preset = DownloadController.getInstance(currentAccount).getCurrentMobilePreset();
                        } else if (position == wifiRow) {
                            enabled = DownloadController.getInstance(currentAccount).wifiPreset.enabled;
                            preset = DownloadController.getInstance(currentAccount).getCurrentWiFiPreset();
                        } else {
                            enabled = DownloadController.getInstance(currentAccount).roamingPreset.enabled;
                            preset = DownloadController.getInstance(currentAccount).getCurrentRoamingPreset();
                        }
                    }

                    boolean photos = false;
                    boolean videos = false;
                    boolean files = false;
                    int count = 0;
                    for (int a = 0; a < preset.mask.length; a++) {
                        if (!photos && (preset.mask[a] & DownloadController.AUTODOWNLOAD_TYPE_PHOTO) != 0) {
                            photos = true;
                            count++;
                        }
                        if (!videos && (preset.mask[a] & DownloadController.AUTODOWNLOAD_TYPE_VIDEO) != 0) {
                            videos = true;
                            count++;
                        }
                        if (!files && (preset.mask[a] & DownloadController.AUTODOWNLOAD_TYPE_DOCUMENT) != 0) {
                            files = true;
                            count++;
                        }
                    }
                    if (preset.enabled && count != 0) {
                        if (photos) {
                            builder.append(LocaleController.getString("AutoDownloadPhotosOn", R.string.AutoDownloadPhotosOn));
                        }
                        if (videos) {
                            if (builder.length() > 0) {
                                builder.append(", ");
                            }
                            builder.append(LocaleController.getString("AutoDownloadVideosOn", R.string.AutoDownloadVideosOn));
                            builder.append(String.format(" (%1$s)", AndroidUtilities.formatFileSize(preset.sizes[DownloadController.typeToIndex(DownloadController.AUTODOWNLOAD_TYPE_VIDEO)], true)));
                        }
                        if (files) {
                            if (builder.length() > 0) {
                                builder.append(", ");
                            }
                            builder.append(LocaleController.getString("AutoDownloadFilesOn", R.string.AutoDownloadFilesOn));
                            builder.append(String.format(" (%1$s)", AndroidUtilities.formatFileSize(preset.sizes[DownloadController.typeToIndex(DownloadController.AUTODOWNLOAD_TYPE_DOCUMENT)], true)));
                        }
                    } else {
                        builder.append(LocaleController.getString("NoMediaAutoDownload", R.string.NoMediaAutoDownload));
                    }
                    checkCell.setTextAndValueAndCheck(text, builder, (photos || videos || files) && enabled, 0, true, true);
                    break;
                }
            }
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            int viewType = holder.getItemViewType();
            if (viewType == 3) {
                TextCheckCell checkCell = (TextCheckCell) holder.itemView;
                int position = holder.getAdapterPosition();
                if (position == enableCacheStreamRow) {
                    checkCell.setChecked(presenter != null ? presenter.isSaveStreamMediaEnabled() : SharedConfig.saveStreamMedia);
                } else if (position == enableStreamRow) {
                    checkCell.setChecked(presenter != null ? presenter.isStreamMediaEnabled() : SharedConfig.streamMedia);
                } else if (position == enableAllStreamRow) {
                    checkCell.setChecked(presenter != null ? presenter.isStreamAllVideoEnabled() : SharedConfig.streamAllVideo);
                } else if (position == enableMkvRow) {
                    checkCell.setChecked(presenter != null ? presenter.isStreamMkvEnabled() : SharedConfig.streamMkv);
                } else if (position == autoplayGifsRow) {
                    checkCell.setChecked(presenter != null ? presenter.isAutoplayGifsEnabled() : SharedConfig.autoplayGifs);
                } else if (position == autoplayVideoRow) {
                    checkCell.setChecked(presenter != null ? presenter.isAutoplayVideoEnabled() : SharedConfig.autoplayVideo);
                }
            }
        }

        public boolean isRowEnabled(int position) {
            if (position == resetDownloadRow) {
                if (presenter != null) {
                    return presenter.isAutoDownloadResetEnabled();
                }
                DownloadController controller = DownloadController.getInstance(currentAccount);
                return !controller.lowPreset.equals(controller.getCurrentRoamingPreset()) || controller.lowPreset.isEnabled() != controller.roamingPreset.enabled ||
                        !controller.mediumPreset.equals(controller.getCurrentMobilePreset()) || controller.mediumPreset.isEnabled() != controller.mobilePreset.enabled ||
                        !controller.highPreset.equals(controller.getCurrentWiFiPreset()) || controller.highPreset.isEnabled() != controller.wifiPreset.enabled;
            }
            return position == mobileRow || position == roamingRow || position == wifiRow || position == storageUsageRow || position == useLessDataForCallsRow || position == dataUsageRow || position == proxyRow || position == clearDraftsRow ||
                    position == enableCacheStreamRow || position == enableStreamRow || position == enableAllStreamRow || position == enableMkvRow || position == quickRepliesRow || position == autoplayVideoRow || position == autoplayGifsRow ||
                    position == storageNumRow || position == saveToGalleryGroupsRow || position == saveToGalleryPeerRow || position == saveToGalleryChannelsRow;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return isRowEnabled(holder.getAdapterPosition());
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    view = new ShadowSectionCell(mContext);
                    break;
                case 1:
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 2:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 3:
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 4:
                    view = new TextInfoPrivacyCell(mContext);
                    view.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case 5:
                default:
                    view = new NotificationsCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == mediaDownloadSection2Row || position == usageSection2Row || position == callsSection2Row || position == proxySection2Row || position == autoplaySectionRow || position == clearDraftsSectionRow || position == saveToGalleryDividerRow) {
                return 0;
            } else if (position == mediaDownloadSectionRow || position == streamSectionRow || position == callsSectionRow || position == usageSectionRow || position == proxySectionRow || position == autoplayHeaderRow || position == saveToGallerySectionRow) {
                return 2;
            } else if (position == enableCacheStreamRow || position == enableStreamRow || position == enableAllStreamRow || position == enableMkvRow || position == autoplayGifsRow || position == autoplayVideoRow || position == saveToGalleryGroupsRow || position == saveToGalleryPeerRow || position == saveToGalleryChannelsRow) {
                return 3;
            } else if (position == enableAllStreamInfoRow) {
                return 4;
            } else if (position == mobileRow || position == wifiRow || position == roamingRow) {
                return 5;
            } else {
                return 1;
            }
        }
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{TextSettingsCell.class, TextCheckCell.class, HeaderCell.class, NotificationsCheckCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray));

        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4));

        return themeDescriptions;
    }

    @Override
    public void onAutoDownloadReset() {
        if (listAdapter != null) {
            listAdapter.notifyItemRangeChanged(mobileRow, 4);
        }
    }

    @Override
    public void onDraftsCleared() {
    }
}
