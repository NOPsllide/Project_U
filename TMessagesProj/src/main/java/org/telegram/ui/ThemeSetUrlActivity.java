/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.core.presenter.ThemeSetUrlPresenter;
import org.telegram.ui.Cells.ThemePreviewMessagesCell;
import org.telegram.ui.Cells.ThemesHorizontalListCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;

public class ThemeSetUrlActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, ThemeSetUrlPresenter.ThemeSetUrlView {

    private ThemeSetUrlPresenter presenter;

    private EditTextBoldCursor linkField;
    private EditTextBoldCursor nameField;
    private View doneButton;
    private TextInfoPrivacyCell helpInfoCell;
    private TextInfoPrivacyCell checkInfoCell;
    private ThemePreviewMessagesCell messagesCell;
    private TextSettingsCell createCell;
    private TextInfoPrivacyCell createInfoCell;

    private AlertDialog progressDialog;

    private View divider;
    private HeaderCell headerCell;
    private EditTextBoldCursor editText;
    private LinearLayout linearLayoutTypeContainer;

    private boolean ignoreCheck;
    private CharSequence infoText;
    private boolean creatingNewTheme;

    private Theme.ThemeInfo themeInfo;
    private Theme.ThemeAccent themeAccent;
    private TLRPC.TL_theme info;

    private final static int done_button = 1;

    public class LinkSpan extends ClickableSpan {

        private String url;

        public LinkSpan(String value) {
            url = value;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }

        @Override
        public void onClick(View widget) {
            try {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) ApplicationLoader.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("label", url);
                clipboard.setPrimaryClip(clip);
                if (BulletinFactory.canShowBulletin(ThemeSetUrlActivity.this)) {
                    BulletinFactory.createCopyLinkBulletin(ThemeSetUrlActivity.this).show();
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    }

    private static class LinkMovementMethodMy extends LinkMovementMethod {
        @Override
        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            try {
                boolean result = super.onTouchEvent(widget, buffer, event);
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    Selection.removeSelection(buffer);
                }
                return result;
            } catch (Exception e) {
                FileLog.e(e);
            }
            return false;
        }
    }

    public ThemeSetUrlActivity(Theme.ThemeInfo theme, Theme.ThemeAccent accent, boolean newTheme) {
        super();
        themeInfo = theme;
        themeAccent = accent;
        info = accent != null ? accent.info : theme.info;
        currentAccount = accent != null ? accent.account : theme.account;
        creatingNewTheme = newTheme;
        presenter = new ThemeSetUrlPresenter(currentAccount, theme, accent, newTheme);
    }

    @Override
    public boolean onFragmentCreate() {
        if (presenter != null) {
            presenter.attachView(this);
            presenter.onFragmentCreate();
        }
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (presenter != null) {
            presenter.onFragmentDestroy();
            presenter.detachView();
        }
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        if (creatingNewTheme) {
            actionBar.setTitle(LocaleController.getString("NewThemeTitle", R.string.NewThemeTitle));
        } else {
            actionBar.setTitle(LocaleController.getString("EditThemeTitle", R.string.EditThemeTitle));
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == done_button) {
                    saveTheme();
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        doneButton = menu.addItem(done_button, LocaleController.getString("Done", R.string.Done).toUpperCase());

        fragmentView = new LinearLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        LinearLayout linearLayout = (LinearLayout) fragmentView;
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        fragmentView.setOnTouchListener((v, event) -> true);

        linearLayoutTypeContainer = new LinearLayout(context);
        linearLayoutTypeContainer.setOrientation(LinearLayout.VERTICAL);
        linearLayoutTypeContainer.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        linearLayout.addView(linearLayoutTypeContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        headerCell = new HeaderCell(context, 23);
        headerCell.setText(LocaleController.getString("Info", R.string.Info));
        linearLayoutTypeContainer.addView(headerCell);

        nameField = new EditTextBoldCursor(context);
        nameField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        nameField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        nameField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        nameField.setMaxLines(1);
        nameField.setLines(1);
        nameField.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        nameField.setBackgroundDrawable(null);
        nameField.setPadding(0, 0, 0, 0);
        nameField.setSingleLine(true);
        InputFilter[] inputFilters = new InputFilter[1];
        inputFilters[0] = new InputFilter.LengthFilter(128);
        nameField.setFilters(inputFilters);
        nameField.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        nameField.setImeOptions(EditorInfo.IME_ACTION_DONE);
        nameField.setHint(LocaleController.getString("ThemeNamePlaceholder", R.string.ThemeNamePlaceholder));
        nameField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        nameField.setCursorSize(AndroidUtilities.dp(20));
        nameField.setCursorWidth(1.5f);
        linearLayoutTypeContainer.addView(nameField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 50, 23, 0, 23, 0));
        nameField.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                AndroidUtilities.hideKeyboard(nameField);
                return true;
            }
            return false;
        });

        divider = new View(context) {
            @Override
            protected void onDraw(Canvas canvas) {
                canvas.drawLine(LocaleController.isRTL ? 0 : AndroidUtilities.dp(20), getMeasuredHeight() - 1, getMeasuredWidth() - (LocaleController.isRTL ? AndroidUtilities.dp(20) : 0), getMeasuredHeight() - 1, Theme.dividerPaint);
            }
        };
        linearLayoutTypeContainer.addView(divider, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));

        LinearLayout linkContainer = new LinearLayout(context);
        linkContainer.setOrientation(LinearLayout.HORIZONTAL);
        linearLayoutTypeContainer.addView(linkContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 50, 23, 0, 23, 0));

        editText = new EditTextBoldCursor(context);
        editText.setText(getMessagesController().linkPrefix + "/addtheme/");
        editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        editText.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        editText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        editText.setMaxLines(1);
        editText.setLines(1);
        editText.setEnabled(false);
        editText.setBackgroundDrawable(null);
        editText.setPadding(0, 0, 0, 0);
        editText.setSingleLine(true);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        linkContainer.addView(editText, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 50));

        linkField = new EditTextBoldCursor(context);
        linkField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        linkField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        linkField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        linkField.setMaxLines(1);
        linkField.setLines(1);
        linkField.setBackgroundDrawable(null);
        linkField.setPadding(0, 0, 0, 0);
        linkField.setSingleLine(true);
        linkField.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        linkField.setImeOptions(EditorInfo.IME_ACTION_DONE);
        linkField.setHint(LocaleController.getString("SetUrlPlaceholder", R.string.SetUrlPlaceholder));
        linkField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        linkField.setCursorSize(AndroidUtilities.dp(20));
        linkField.setCursorWidth(1.5f);
        linkContainer.addView(linkField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 50));
        linkField.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE && doneButton != null) {
                doneButton.performClick();
                return true;
            }
            return false;
        });
        linkField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (ignoreCheck) {
                    return;
                }
                checkUrl(linkField.getText().toString(), false);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (creatingNewTheme) {
                    return;
                }
                if (linkField.length() > 0) {
                    String url = "https://" + getMessagesController().linkPrefix + "/addtheme/" + linkField.getText();
                    String text = LocaleController.formatString("ThemeHelpLink", R.string.ThemeHelpLink, url);
                    int index = text.indexOf(url);
                    SpannableStringBuilder textSpan = new SpannableStringBuilder(text);
                    if (index >= 0) {
                        textSpan.setSpan(new LinkSpan(url), index, index + url.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    helpInfoCell.setText(TextUtils.concat(infoText, "\n\n", textSpan));
                } else {
                    helpInfoCell.setText(infoText);
                }
            }
        });
        if (creatingNewTheme) {
            linkField.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    helpInfoCell.setText(AndroidUtilities.replaceTags(LocaleController.getString("ThemeCreateHelp2", R.string.ThemeCreateHelp2)));
                } else {
                    helpInfoCell.setText(AndroidUtilities.replaceTags(LocaleController.getString("ThemeCreateHelp", R.string.ThemeCreateHelp)));
                }
            });
        }

        checkInfoCell = new TextInfoPrivacyCell(context);
        checkInfoCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
        checkInfoCell.setVisibility(View.GONE);
        checkInfoCell.setBottomPadding(0);
        linearLayout.addView(checkInfoCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        helpInfoCell = new TextInfoPrivacyCell(context);
        helpInfoCell.getTextView().setMovementMethod(new LinkMovementMethodMy());
        helpInfoCell.getTextView().setHighlightColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkSelection));
        if (creatingNewTheme) {
            helpInfoCell.setText(AndroidUtilities.replaceTags(LocaleController.getString("ThemeCreateHelp", R.string.ThemeCreateHelp)));
        } else {
            helpInfoCell.setText(infoText = AndroidUtilities.replaceTags(LocaleController.getString("ThemeSetUrlHelp", R.string.ThemeSetUrlHelp)));
        }
        linearLayout.addView(helpInfoCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        if (creatingNewTheme) {
            helpInfoCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
            messagesCell = new ThemePreviewMessagesCell(context, parentLayout, 1);
            linearLayout.addView(messagesCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            createCell = new TextSettingsCell(context);
            createCell.setBackgroundDrawable(Theme.getSelectorDrawable(true));
            createCell.setText(LocaleController.getString("UseDifferentTheme", R.string.UseDifferentTheme), false);
            linearLayout.addView(createCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            createCell.setOnClickListener(v -> {
                if (getParentActivity() == null) {
                    return;
                }
                BottomSheet.Builder builder = new BottomSheet.Builder(getParentActivity(), false);
                builder.setApplyBottomPadding(false);

                LinearLayout container = new LinearLayout(context);
                container.setOrientation(LinearLayout.VERTICAL);

                TextView titleView = new TextView(context);
                titleView.setText(LocaleController.getString("ChooseTheme", R.string.ChooseTheme));
                titleView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
                titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                titleView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                container.addView(titleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 22, 12, 22, 4));
                titleView.setOnTouchListener((v2, event) -> true);
                builder.setCustomView(container);

                ArrayList<Theme.ThemeInfo> themes = new ArrayList<>();
                for (int a = 0, N = Theme.themes.size(); a < N; a++) {
                    Theme.ThemeInfo themeInfo = Theme.themes.get(a);
                    if (themeInfo.info != null && themeInfo.info.document == null) {
                        continue;
                    }
                    themes.add(themeInfo);
                }
                ThemesHorizontalListCell cell = new ThemesHorizontalListCell(context, ThemeActivity.THEME_TYPE_OTHER, themes, new ArrayList<>()) {
                    @Override
                    protected void updateRows() {
                        builder.getDismissRunnable().run();
                    }
                };
                container.addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 148, 0, 7, 0, 1));
                cell.scrollToCurrentTheme(fragmentView.getMeasuredWidth(), false);
                showDialog(builder.create());
            });

            createInfoCell = new TextInfoPrivacyCell(context);
            createInfoCell.setText(AndroidUtilities.replaceTags(LocaleController.getString("UseDifferentThemeInfo", R.string.UseDifferentThemeInfo)));
            createInfoCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
            linearLayout.addView(createInfoCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        } else {
            helpInfoCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
        }

        if (info != null) {
            ignoreCheck = true;
            nameField.setText(info.title);
            nameField.setSelection(nameField.length());
            linkField.setText(info.slug);
            linkField.setSelection(linkField.length());
            ignoreCheck = false;
        }

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences preferences = MessagesController.getGlobalMainSettings();
        boolean animations = preferences.getBoolean("view_animations", true);
        if (!animations && creatingNewTheme) {
            linkField.requestFocus();
            AndroidUtilities.showKeyboard(linkField);
        }
        AndroidUtilities.requestAdjustResize(getParentActivity(), classGuid);
        AndroidUtilities.removeAdjustResize(getParentActivity(), classGuid);
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (presenter != null) {
            presenter.didReceivedNotification(id, account, args);
        }
    }

    private boolean checkUrl(final String url, boolean alert) {
        if (presenter != null) {
            return presenter.checkUrl(url, alert);
        }
        return false;
    }

    private void saveTheme() {
        if (presenter != null) {
            presenter.saveTheme(nameField.getText().toString(), linkField.getText().toString());
        }
    }

    @Override
    public void showCheckText(String text, String colorKey) {
        if (TextUtils.isEmpty(text)) {
            checkInfoCell.setVisibility(View.GONE);
            if (creatingNewTheme) {
                helpInfoCell.setBackgroundDrawable(Theme.getThemedDrawable(getParentActivity(), R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
            } else {
                helpInfoCell.setBackgroundDrawable(Theme.getThemedDrawable(getParentActivity(), R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
            }
        } else {
            checkInfoCell.setVisibility(View.VISIBLE);
            checkInfoCell.setText(text);
            checkInfoCell.setTag(colorKey);
            checkInfoCell.setTextColor(colorKey);
            if (creatingNewTheme) {
                helpInfoCell.setBackgroundDrawable(Theme.getThemedDrawable(getParentActivity(), R.drawable.greydivider_top, Theme.key_windowBackgroundGrayShadow));
            } else {
                helpInfoCell.setBackgroundDrawable(null);
            }
        }
    }

    @Override
    public void showSimpleAlert(String title, String message) {
        AlertsCreator.showSimpleAlert(this, title, message);
    }

    @Override
    public void showNameInvalidAlert() {
        AlertsCreator.showSimpleAlert(this, LocaleController.getString("Theme", R.string.Theme), LocaleController.getString("ThemeNameInvalid", R.string.ThemeNameInvalid));
    }

    @Override
    public void showProgressDialog(Runnable onCancel) {
        if (getParentActivity() == null) {
            return;
        }
        progressDialog = new AlertDialog(getParentActivity(), 3);
        if (onCancel != null) {
            progressDialog.setOnCancelListener(dialog -> onCancel.run());
        }
        progressDialog.show();
    }

    @Override
    public void dismissProgressDialog() {
        if (progressDialog != null) {
            try {
                progressDialog.dismiss();
            } catch (Exception e) {
                FileLog.e(e);
            }
            progressDialog = null;
        }
    }

    @Override
    public void showUpdateError(TLRPC.TL_error error, TLRPC.TL_account_updateTheme req) {
        AlertsCreator.processError(currentAccount, error, ThemeSetUrlActivity.this, req);
    }

    @Override
    public void finishScreen() {
        finishFragment();
    }

    @Override
    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
        if (isOpen && !creatingNewTheme) {
            linkField.requestFocus();
            AndroidUtilities.showKeyboard(linkField);
        }
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();

        themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray));

        themeDescriptions.add(new ThemeDescription(linearLayoutTypeContainer, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite));

        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector));

        themeDescriptions.add(new ThemeDescription(headerCell, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader));

        themeDescriptions.add(new ThemeDescription(createInfoCell, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));
        themeDescriptions.add(new ThemeDescription(createInfoCell, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4));

        themeDescriptions.add(new ThemeDescription(helpInfoCell, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));
        themeDescriptions.add(new ThemeDescription(helpInfoCell, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4));

        themeDescriptions.add(new ThemeDescription(checkInfoCell, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));
        themeDescriptions.add(new ThemeDescription(checkInfoCell, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteRedText4));
        themeDescriptions.add(new ThemeDescription(checkInfoCell, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText8));
        themeDescriptions.add(new ThemeDescription(checkInfoCell, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGreenText));

        themeDescriptions.add(new ThemeDescription(createCell, 0, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(createCell, ThemeDescription.FLAG_SELECTORWHITE, null, null, null, null, Theme.key_listSelector));
        themeDescriptions.add(new ThemeDescription(createCell, ThemeDescription.FLAG_SELECTORWHITE, null, null, null, null, Theme.key_windowBackgroundWhite));

        themeDescriptions.add(new ThemeDescription(linkField, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(linkField, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteHintText));
        themeDescriptions.add(new ThemeDescription(linkField, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_windowBackgroundWhiteInputField));
        themeDescriptions.add(new ThemeDescription(linkField, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_windowBackgroundWhiteInputFieldActivated));

        themeDescriptions.add(new ThemeDescription(linkField, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(linkField, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteHintText));
        themeDescriptions.add(new ThemeDescription(linkField, ThemeDescription.FLAG_CURSORCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(nameField, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(nameField, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteHintText));
        themeDescriptions.add(new ThemeDescription(nameField, ThemeDescription.FLAG_CURSORCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(editText, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(editText, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteHintText));

        themeDescriptions.add(new ThemeDescription(divider, 0, null, Theme.dividerPaint, null, null, Theme.key_divider));
        themeDescriptions.add(new ThemeDescription(divider, ThemeDescription.FLAG_BACKGROUND, null, Theme.dividerPaint, null, null, Theme.key_divider));

        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, new Drawable[]{Theme.chat_msgInDrawable, Theme.chat_msgInMediaDrawable}, null, Theme.key_chat_inBubble));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, new Drawable[]{Theme.chat_msgInSelectedDrawable, Theme.chat_msgInMediaSelectedDrawable}, null, Theme.key_chat_inBubbleSelected));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, Theme.chat_msgInDrawable.getShadowDrawables(), null, Theme.key_chat_inBubbleShadow));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, Theme.chat_msgInMediaDrawable.getShadowDrawables(), null, Theme.key_chat_inBubbleShadow));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, new Drawable[]{Theme.chat_msgOutDrawable, Theme.chat_msgOutMediaDrawable}, null, Theme.key_chat_outBubble));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, new Drawable[]{Theme.chat_msgOutDrawable, Theme.chat_msgOutMediaDrawable}, null, Theme.key_chat_outBubbleGradient1));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, new Drawable[]{Theme.chat_msgOutDrawable, Theme.chat_msgOutMediaDrawable}, null, Theme.key_chat_outBubbleGradient2));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, new Drawable[]{Theme.chat_msgOutDrawable, Theme.chat_msgOutMediaDrawable}, null, Theme.key_chat_outBubbleGradient3));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, new Drawable[]{Theme.chat_msgOutSelectedDrawable, Theme.chat_msgOutMediaSelectedDrawable}, null, Theme.key_chat_outBubbleSelected));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, Theme.chat_msgOutDrawable.getShadowDrawables(), null, Theme.key_chat_outBubbleShadow));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, Theme.chat_msgOutMediaDrawable.getShadowDrawables(), null, Theme.key_chat_outBubbleShadow));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, null, null, Theme.key_chat_messageTextIn));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, null, null, Theme.key_chat_messageTextOut));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, new Drawable[]{Theme.chat_msgOutCheckDrawable}, null, Theme.key_chat_outSentCheck));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, new Drawable[]{Theme.chat_msgOutCheckSelectedDrawable}, null, Theme.key_chat_outSentCheckSelected));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, new Drawable[]{Theme.chat_msgOutCheckReadDrawable, Theme.chat_msgOutHalfCheckDrawable}, null, Theme.key_chat_outSentCheckRead));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, new Drawable[]{Theme.chat_msgOutCheckReadSelectedDrawable, Theme.chat_msgOutHalfCheckSelectedDrawable}, null, Theme.key_chat_outSentCheckReadSelected));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, new Drawable[]{Theme.chat_msgMediaCheckDrawable, Theme.chat_msgMediaHalfCheckDrawable}, null, Theme.key_chat_mediaSentCheck));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, null, null, Theme.key_chat_inReplyLine));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, null, null, Theme.key_chat_outReplyLine));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, null, null, Theme.key_chat_inReplyNameText));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, null, null, Theme.key_chat_outReplyNameText));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, null, null, Theme.key_chat_inReplyMessageText));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, null, null, Theme.key_chat_outReplyMessageText));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, null, null, Theme.key_chat_inReplyMediaMessageSelectedText));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, null, null, Theme.key_chat_outReplyMediaMessageSelectedText));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, null, null, Theme.key_chat_inTimeText));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, null, null, Theme.key_chat_outTimeText));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, null, null, Theme.key_chat_inTimeSelectedText));
        themeDescriptions.add(new ThemeDescription(messagesCell, 0, null, null, null, null, Theme.key_chat_outTimeSelectedText));

        return themeDescriptions;
    }
}
