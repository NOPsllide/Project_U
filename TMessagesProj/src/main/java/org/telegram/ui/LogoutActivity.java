package org.telegram.ui;

import static org.telegram.ui.Components.Premium.LimitReachedBottomSheet.TYPE_ACCOUNTS;

import android.animation.AnimatorSet;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.core.data.repository.AuthRepository;
import org.telegram.core.data.repository.AuthRepositoryImpl;
import org.telegram.core.presenter.LogoutPresenter;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.Premium.LimitReachedBottomSheet;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;

public class LogoutActivity extends BaseFragment implements LogoutPresenter.View {

    private ListAdapter listAdapter;
    private RecyclerListView listView;
    private AnimatorSet animatorSet;
    private LogoutPresenter presenter;

    private int alternativeHeaderRow;
    private int addAccountRow;
    private int passcodeRow;
    private int cacheRow;
    private int phoneRow;
    private int supportRow;
    private int alternativeSectionRow;
    private int logoutRow;
    private int logoutSectionRow;
    private int rowCount;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        AuthRepository repository = new AuthRepositoryImpl(currentAccount);
        presenter = new LogoutPresenter(repository);
        presenter.attachView(this);

        rowCount = 0;
        alternativeHeaderRow = rowCount++;
        if (presenter.canAddAccount()) {
            addAccountRow = rowCount++;
        } else {
            addAccountRow = -1;
        }
        if (!presenter.hasPasscode()) {
            passcodeRow = rowCount++;
        } else {
            passcodeRow = -1;
        }
        cacheRow = rowCount++;
        phoneRow = rowCount++;
        supportRow = rowCount++;
        alternativeSectionRow = rowCount++;
        logoutRow = rowCount++;
        logoutSectionRow = rowCount++;

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
        actionBar.setTitle(LocaleController.getString("LogOutTitle", R.string.LogOutTitle));
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
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener((view, position, x, y) -> {
            if (position == addAccountRow) {
                presenter.onAddAccountClicked();
            } else if (position == passcodeRow) {
                presenter.onPasscodeClicked();
            } else if (position == cacheRow) {
                presenter.onCacheClicked();
            } else if (position == phoneRow) {
                presenter.onPhoneClicked();
            } else if (position == supportRow) {
                presenter.onSupportClicked();
            } else if (position == logoutRow) {
                presenter.onLogoutClicked();
            }
        });

        return fragmentView;
    }

    @Override
    public void openLoginActivity(int account) {
        presentFragment(new LoginActivity(account));
    }

    @Override
    public void showAccountLimitBottomSheet() {
        LimitReachedBottomSheet limitReachedBottomSheet = new LimitReachedBottomSheet(this, getContext(), TYPE_ACCOUNTS, currentAccount);
        showDialog(limitReachedBottomSheet);
    }

    @Override
    public void openPasscodeActivity() {
        presentFragment(PasscodeActivity.determineOpenFragment());
    }

    @Override
    public void openCacheControlActivity() {
        presentFragment(new CacheControlActivity());
    }

    @Override
    public void openChangePhoneNumber() {
        presentFragment(new ActionIntroActivity(ActionIntroActivity.ACTION_TYPE_CHANGE_PHONE_NUMBER));
    }

    @Override
    public void showSupportAlert() {
        showDialog(AlertsCreator.createSupportAlert(LogoutActivity.this, null));
    }

    @Override
    public void showLogoutConfirmationDialog() {
        if (getParentActivity() == null) {
            return;
        }
        showDialog(makeLogOutDialog(getParentActivity(), presenter));
    }

    public static AlertDialog makeLogOutDialog(Context context, LogoutPresenter presenter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(LocaleController.getString("AreYouSureLogout", R.string.AreYouSureLogout));
        builder.setTitle(LocaleController.getString("LogOut", R.string.LogOut));
        builder.setPositiveButton(LocaleController.getString("LogOut", R.string.LogOut), (dialogInterface, i) -> presenter.confirmLogout());
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        AlertDialog alertDialog = builder.create();
        TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (button != null) {
            button.setTextColor(Theme.getColor(Theme.key_dialogTextRed2));
        }
        return alertDialog;
    }

    public static AlertDialog makeLogOutDialog(Context context, int currentAccount) {
        AuthRepository repo = new AuthRepositoryImpl(currentAccount);
        LogoutPresenter pres = new LogoutPresenter(repo);
        return makeLogOutDialog(context, pres);
    }

    @Override
    protected void onDialogDismiss(Dialog dialog) {
        if (presenter != null) {
            presenter.onDialogDismissed();
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
                    HeaderCell view = (HeaderCell) holder.itemView;
                    if (position == alternativeHeaderRow) {
                        view.setText(LocaleController.getString("AlternativeOptions", R.string.AlternativeOptions));
                    }
                    break;
                }
                case 1: {
                    TextDetailSettingsCell view = (TextDetailSettingsCell) holder.itemView;
                    if (position == addAccountRow) {
                        view.setTextAndValueAndIcon(LocaleController.getString("AddAnotherAccount", R.string.AddAnotherAccount), LocaleController.getString("AddAnotherAccountInfo", R.string.AddAnotherAccountInfo), R.drawable.msg_contact_add, true);
                    } else if (position == passcodeRow) {
                        view.setTextAndValueAndIcon(LocaleController.getString("SetPasscode", R.string.SetPasscode), LocaleController.getString("SetPasscodeInfo", R.string.SetPasscodeInfo), R.drawable.msg_permissions, true);
                    } else if (position == cacheRow) {
                        view.setTextAndValueAndIcon(LocaleController.getString("ClearCache", R.string.ClearCache), LocaleController.getString("ClearCacheInfo", R.string.ClearCacheInfo), R.drawable.msg_clearcache, true);
                    } else if (position == phoneRow) {
                        view.setTextAndValueAndIcon(LocaleController.getString("ChangePhoneNumber", R.string.ChangePhoneNumber), LocaleController.getString("ChangePhoneNumberInfo", R.string.ChangePhoneNumberInfo), R.drawable.msg_newphone, true);
                    } else if (position == supportRow) {
                        view.setTextAndValueAndIcon(LocaleController.getString("ContactSupport", R.string.ContactSupport), LocaleController.getString("ContactSupportInfo", R.string.ContactSupportInfo), R.drawable.msg_help, false);
                    }
                    break;
                }
                case 3: {
                    TextSettingsCell view = (TextSettingsCell) holder.itemView;
                    if (position == logoutRow) {
                        view.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText5));
                        view.setText(LocaleController.getString("LogOutTitle", R.string.LogOutTitle), false);
                    }
                    break;
                }
                case 4: {
                    TextInfoPrivacyCell view = (TextInfoPrivacyCell) holder.itemView;
                    if (position == logoutSectionRow) {
                        view.setText(LocaleController.getString("LogOutInfo", R.string.LogOutInfo));
                    }
                    break;
                }
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int position = holder.getAdapterPosition();
            return position == addAccountRow || position == passcodeRow || position == cacheRow || position == phoneRow || position == supportRow || position == logoutRow;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0: {
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                }
                case 1: {
                    TextDetailSettingsCell cell = new TextDetailSettingsCell(mContext);
                    cell.setMultilineDetail(true);
                    cell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    view = cell;
                    break;
                }
                case 2: {
                    view = new ShadowSectionCell(mContext);
                    break;
                }
                case 3: {
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                }
                case 4:
                default: {
                    view = new TextInfoPrivacyCell(mContext);
                    view.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                }
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == alternativeHeaderRow) {
                return 0;
            } else if (position == addAccountRow || position == passcodeRow || position == cacheRow || position == phoneRow || position == supportRow) {
                return 1;
            } else if (position == alternativeSectionRow) {
                return 2;
            } else if (position == logoutRow) {
                return 3;
            } else {
                return 4;
            }
        }
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{TextSettingsCell.class, HeaderCell.class, TextDetailSettingsCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray));

        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteRedText5));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextDetailSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextDetailSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextDetailSettingsCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon));

        return themeDescriptions;
    }
}
