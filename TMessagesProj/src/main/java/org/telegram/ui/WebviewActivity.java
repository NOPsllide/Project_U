/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import org.telegram.core.data.repository.WebviewRepositoryImpl;
import org.telegram.core.presenter.WebviewPresenter;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Components.ContextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ShareAlert;

import java.util.ArrayList;

public class WebviewActivity extends BaseFragment implements WebviewPresenter.WebviewView {

    private WebviewPresenter presenter;
    private WebView webView;
    private ActionBarMenuItem progressItem;
    private ContextProgressView progressView;

    private String currentUrl;
    private long currentDialogId;
    private String currentBot;
    private String currentGame;
    private String linkToCopy;
    private MessageObject currentMessageObject;
    private String short_param;

    private boolean loadStats;

    private final static int share = 1;
    private final static int open_in = 2;

    private static final int TYPE_GAME = 0;
    private static final int TYPE_STAT = 1;

    private int type;

    private class TelegramWebviewProxy {
        @JavascriptInterface
        public void postEvent(final String eventName, final String eventData) {
            AndroidUtilities.runOnUIThread(() -> {
                if (getParentActivity() == null) {
                    return;
                }
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.d(eventName);
                }
                switch (eventName) {
                    case "share_game":
                        currentMessageObject.messageOwner.with_my_score = false;
                        break;
                    case "share_score":
                        currentMessageObject.messageOwner.with_my_score = true;
                        break;
                }
                showDialog(ShareAlert.createShareAlert(getParentActivity(), currentMessageObject, null, false, linkToCopy, false));
            });
        }
    }

    public Runnable typingRunnable = new Runnable() {
        @Override
        public void run() {
            if (currentMessageObject == null || getParentActivity() == null || typingRunnable == null) {
                return;
            }
            if (presenter != null) {
                presenter.sendGameTyping(currentMessageObject.getDialogId());
            }
            AndroidUtilities.runOnUIThread(typingRunnable, 25000);
        }
    };

    public WebviewActivity(String url, String botName, String gameName, String startParam, MessageObject messageObject) {
        super();
        presenter = new WebviewPresenter(currentAccount);
        currentUrl = url;
        currentBot = botName;
        currentGame = gameName;
        currentMessageObject = messageObject;
        short_param = startParam;
        linkToCopy = presenter.getGameShareLink(currentBot, startParam);
        type = TYPE_GAME;
    }

    public WebviewActivity(String statUrl, long did) {
        super();
        presenter = new WebviewPresenter(currentAccount);
        currentUrl = statUrl;
        currentDialogId = did;
        type = TYPE_STAT;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (presenter != null) {
            presenter.detachView();
        }
        AndroidUtilities.cancelRunOnUIThread(typingRunnable);
        webView.setLayerType(View.LAYER_TYPE_NONE, null);
        typingRunnable = null;
        try {
            ViewParent parent = webView.getParent();
            if (parent != null) {
                ((FrameLayout) parent).removeView(webView);
            }
            webView.stopLoading();
            webView.loadUrl("about:blank");
            webView.destroy();
            webView = null;
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    public View createView(Context context) {
        if (presenter != null) {
            presenter.attachView(this);
        }
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == share) {
                    if (currentMessageObject != null) {
                        currentMessageObject.messageOwner.with_my_score = false;
                        showDialog(ShareAlert.createShareAlert(getParentActivity(), currentMessageObject, null, false, linkToCopy, false));
                    }
                } else if (id == open_in) {
                    openGameInBrowser(currentUrl, currentMessageObject, getParentActivity(), short_param, currentBot);
                }
            }
        });
        ActionBarMenu menu = actionBar.createMenu();
        progressItem = menu.addItemWithWidth(share, R.drawable.share, AndroidUtilities.dp(54));
        if (type == TYPE_GAME) {
            ActionBarMenuItem menuItem = menu.addItem(0, R.drawable.ic_ab_other);
            menuItem.addSubItem(open_in, R.drawable.msg_openin, LocaleController.getString("OpenInExternalApp", R.string.OpenInExternalApp));

            actionBar.setTitle(currentGame);
            actionBar.setSubtitle("@" + currentBot);

            progressView = new ContextProgressView(context, 1);
            progressItem.addView(progressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
            progressView.setAlpha(0.0f);
            progressView.setScaleX(0.1f);
            progressView.setScaleY(0.1f);
            progressView.setVisibility(View.INVISIBLE);
        } else if (type == TYPE_STAT) {
            actionBar.setBackgroundColor(Theme.getColor(Theme.key_player_actionBar));
            actionBar.setItemsColor(Theme.getColor(Theme.key_player_actionBarItems), false);
            actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_player_actionBarSelector), false);
            actionBar.setTitleColor(Theme.getColor(Theme.key_player_actionBarTitle));
            actionBar.setSubtitleColor(Theme.getColor(Theme.key_player_actionBarSubtitle));
            actionBar.setTitle(LocaleController.getString("Statistics", R.string.Statistics));

            progressView = new ContextProgressView(context, 3);
            progressItem.addView(progressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
            progressView.setAlpha(1.0f);
            progressView.setScaleX(1.0f);
            progressView.setScaleY(1.0f);
            progressView.setVisibility(View.VISIBLE);
            progressItem.getContentView().setVisibility(View.GONE);
            progressItem.setEnabled(false);
        }

        webView = new WebView(context);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        if (Build.VERSION.SDK_INT >= 19) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        if (Build.VERSION.SDK_INT >= 21) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptThirdPartyCookies(webView, true);
            if (type == TYPE_GAME) {
                webView.addJavascriptInterface(new TelegramWebviewProxy(), "TelegramWebviewProxy");
            }
        }

        webView.setWebViewClient(new WebViewClient() {

            private boolean isInternalUrl(String url) {
                if (TextUtils.isEmpty(url)) {
                    return false;
                }
                Uri uri = Uri.parse(url);
                if ("tg".equals(uri.getScheme())) {
                    if (type == TYPE_STAT) {
                        try {
                            uri = Uri.parse(url.replace("tg:statsrefresh", "tg://telegram.org"));
                            reloadStats(uri.getQueryParameter("params"));
                        } catch (Throwable e) {
                            FileLog.e(e);
                        }
                    } else {
                        finishFragment(false);
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            ComponentName componentName = new ComponentName(ApplicationLoader.applicationContext.getPackageName(), LaunchActivity.class.getName());
                            intent.setComponent(componentName);
                            intent.putExtra(android.provider.Browser.EXTRA_APPLICATION_ID, ApplicationLoader.applicationContext.getPackageName());
                            ApplicationLoader.applicationContext.startActivity(intent);
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                if (isInternalUrl(url)) {
                    return;
                }
                super.onLoadResource(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return isInternalUrl(url) || super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (progressView != null && progressView.getVisibility() == View.VISIBLE) {
                    AnimatorSet animatorSet = new AnimatorSet();
                    if (type == TYPE_GAME) {
                        progressItem.getContentView().setVisibility(View.VISIBLE);
                        progressItem.setEnabled(true);
                        animatorSet.playTogether(
                                ObjectAnimator.ofFloat(progressView, "scaleX", 1.0f, 0.1f),
                                ObjectAnimator.ofFloat(progressView, "scaleY", 1.0f, 0.1f),
                                ObjectAnimator.ofFloat(progressView, "alpha", 1.0f, 0.0f),
                                ObjectAnimator.ofFloat(progressItem.getContentView(), "scaleX", 0.0f, 1.0f),
                                ObjectAnimator.ofFloat(progressItem.getContentView(), "scaleY", 0.0f, 1.0f),
                                ObjectAnimator.ofFloat(progressItem.getContentView(), "alpha", 0.0f, 1.0f));
                    } else {
                        animatorSet.playTogether(
                                ObjectAnimator.ofFloat(progressView, "scaleX", 1.0f, 0.1f),
                                ObjectAnimator.ofFloat(progressView, "scaleY", 1.0f, 0.1f),
                                ObjectAnimator.ofFloat(progressView, "alpha", 1.0f, 0.0f));
                    }
                    animatorSet.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animator) {
                            if (type == TYPE_STAT) {
                                progressItem.setVisibility(View.GONE);
                            } else {
                                progressView.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                    animatorSet.setDuration(150);
                    animatorSet.start();
                }
            }
        });

        frameLayout.addView(webView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        AndroidUtilities.cancelRunOnUIThread(typingRunnable);
        typingRunnable.run();
    }

    @Override
    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
        if (isOpen && !backward && webView != null) {
            webView.loadUrl(currentUrl);
        }
    }

    @Override
    public boolean isSwipeBackEnabled(MotionEvent event) {
        return false;
    }

    public static boolean supportWebview() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if ("samsung".equals(manufacturer)) {
            if ("GT-I9500".equals(model)) {
                return false;
            }
        }
        return true;
    }

    private void reloadStats(String params) {
        if (loadStats) {
            return;
        }
        loadStats = true;
        if (presenter != null) {
            presenter.reloadStats(currentDialogId, params, Theme.getCurrentTheme().isDark());
        }
    }

    @Override
    public void onStatsUrlLoaded(String url) {
        loadStats = false;
        if (url != null && webView != null) {
            webView.loadUrl(currentUrl = url);
        }
    }

    public static void openGameInBrowser(String urlStr, MessageObject messageObject, Activity parentActivity, String short_name, String username) {
        if (messageObject == null || parentActivity == null) {
            return;
        }
        new WebviewRepositoryImpl(messageObject.currentAccount).openGameInBrowser(urlStr, messageObject, parentActivity, short_name, username);
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();
        if (type == TYPE_GAME) {
            themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite));

            themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault));
            themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon));
            themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle));
            themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector));
            themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUBACKGROUND, null, null, null, null, Theme.key_actionBarDefaultSubmenuBackground));
            themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM, null, null, null, null, Theme.key_actionBarDefaultSubmenuItem));
            themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM | ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_actionBarDefaultSubmenuItemIcon));

            themeDescriptions.add(new ThemeDescription(progressView, 0, null, null, null, null, Theme.key_contextProgressInner2));
            themeDescriptions.add(new ThemeDescription(progressView, 0, null, null, null, null, Theme.key_contextProgressOuter2));
        } else {
            themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite));

            themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_player_actionBar));
            themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_player_actionBarItems));
            themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_player_actionBarTitle));
            themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBTITLECOLOR, null, null, null, null, Theme.key_player_actionBarTitle));
            themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_player_actionBarSelector));
            themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUBACKGROUND, null, null, null, null, Theme.key_actionBarDefaultSubmenuBackground));
            themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM, null, null, null, null, Theme.key_actionBarDefaultSubmenuItem));
            themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM | ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_actionBarDefaultSubmenuItemIcon));

            themeDescriptions.add(new ThemeDescription(progressView, 0, null, null, null, null, Theme.key_contextProgressInner4));
            themeDescriptions.add(new ThemeDescription(progressView, 0, null, null, null, null, Theme.key_contextProgressOuter4));
        }
        return themeDescriptions;
    }
}
