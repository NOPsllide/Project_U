package org.telegram.core.presenter;

import android.text.TextUtils;

import org.telegram.core.data.repository.ThemeRepository;
import org.telegram.core.data.repository.ThemeRepositoryImpl;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;

public class ThemeSetUrlPresenter implements NotificationCenter.NotificationCenterDelegate {

    public interface ThemeSetUrlView {
        void showCheckText(String text, String colorKey);
        void showSimpleAlert(String title, String message);
        void showNameInvalidAlert();
        void showProgressDialog(Runnable onCancel);
        void dismissProgressDialog();
        void showUpdateError(TLRPC.TL_error error, TLRPC.TL_account_updateTheme req);
        void finishScreen();
    }

    private final int currentAccount;
    private final ThemeRepository themeRepository;
    private ThemeSetUrlView view;

    private Theme.ThemeInfo themeInfo;
    private Theme.ThemeAccent themeAccent;
    private TLRPC.TL_theme info;
    private boolean creatingNewTheme;

    private int checkReqId;
    private int updateReqId;
    private String lastCheckName;
    private Runnable checkRunnable;
    private boolean lastNameAvailable;

    public ThemeSetUrlPresenter(int currentAccount, Theme.ThemeInfo theme, Theme.ThemeAccent accent, boolean newTheme) {
        this(currentAccount, theme, accent, newTheme, new ThemeRepositoryImpl(currentAccount));
    }

    public ThemeSetUrlPresenter(int currentAccount, Theme.ThemeInfo theme, Theme.ThemeAccent accent, boolean newTheme, ThemeRepository themeRepository) {
        this.currentAccount = currentAccount;
        this.themeInfo = theme;
        this.themeAccent = accent;
        this.creatingNewTheme = newTheme;
        this.info = accent != null ? accent.info : (theme != null ? theme.info : null);
        this.themeRepository = themeRepository;
    }

    public void attachView(ThemeSetUrlView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void onFragmentCreate() {
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.themeUploadedToServer);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.themeUploadError);
    }

    public void onFragmentDestroy() {
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.themeUploadedToServer);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.themeUploadError);
        if (checkReqId != 0) {
            themeRepository.cancelRequest(checkReqId);
            checkReqId = 0;
        }
        if (updateReqId != 0) {
            themeRepository.cancelRequest(updateReqId);
            updateReqId = 0;
        }
        if (checkRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(checkRunnable);
            checkRunnable = null;
        }
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.themeUploadedToServer) {
            Theme.ThemeInfo theme = (Theme.ThemeInfo) args[0];
            Theme.ThemeAccent accent = (Theme.ThemeAccent) args[1];
            if (theme == themeInfo && accent == themeAccent) {
                themeRepository.applyTheme(themeInfo, false);
                if (view != null) {
                    view.dismissProgressDialog();
                    view.finishScreen();
                }
            }
        } else if (id == NotificationCenter.themeUploadError) {
            Theme.ThemeInfo theme = (Theme.ThemeInfo) args[0];
            Theme.ThemeAccent accent = (Theme.ThemeAccent) args[1];
            if (theme == themeInfo && accent == themeAccent) {
                if (view != null) {
                    view.dismissProgressDialog();
                }
            }
        }
    }

    public boolean checkUrl(final String url, boolean alert) {
        if (checkRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(checkRunnable);
            checkRunnable = null;
            lastCheckName = null;
            if (checkReqId != 0) {
                themeRepository.cancelRequest(checkReqId);
                checkReqId = 0;
            }
        }
        lastNameAvailable = false;
        if (url != null) {
            if (url.startsWith("_") || url.endsWith("_")) {
                if (view != null) {
                    view.showCheckText(LocaleController.getString("SetUrlInvalid", R.string.SetUrlInvalid), Theme.key_windowBackgroundWhiteRedText4);
                }
                return false;
            }
            for (int a = 0; a < url.length(); a++) {
                char ch = url.charAt(a);
                if (a == 0 && ch >= '0' && ch <= '9') {
                    if (alert) {
                        if (view != null) {
                            view.showSimpleAlert(LocaleController.getString("Theme", R.string.Theme), LocaleController.getString("SetUrlInvalidStartNumber", R.string.SetUrlInvalidStartNumber));
                        }
                    } else {
                        if (view != null) {
                            view.showCheckText(LocaleController.getString("SetUrlInvalidStartNumber", R.string.SetUrlInvalidStartNumber), Theme.key_windowBackgroundWhiteRedText4);
                        }
                    }
                    return false;
                }
                if (!(ch >= '0' && ch <= '9' || ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch == '_')) {
                    if (alert) {
                        if (view != null) {
                            view.showSimpleAlert(LocaleController.getString("Theme", R.string.Theme), LocaleController.getString("SetUrlInvalid", R.string.SetUrlInvalid));
                        }
                    } else {
                        if (view != null) {
                            view.showCheckText(LocaleController.getString("SetUrlInvalid", R.string.SetUrlInvalid), Theme.key_windowBackgroundWhiteRedText4);
                        }
                    }
                    return false;
                }
            }
        }
        if (url == null || url.length() < 5) {
            if (alert) {
                if (view != null) {
                    view.showSimpleAlert(LocaleController.getString("Theme", R.string.Theme), LocaleController.getString("SetUrlInvalidShort", R.string.SetUrlInvalidShort));
                }
            } else {
                if (view != null) {
                    view.showCheckText(LocaleController.getString("SetUrlInvalidShort", R.string.SetUrlInvalidShort), Theme.key_windowBackgroundWhiteRedText4);
                }
            }
            return false;
        }
        if (url.length() > 64) {
            if (alert) {
                if (view != null) {
                    view.showSimpleAlert(LocaleController.getString("Theme", R.string.Theme), LocaleController.getString("SetUrlInvalidLong", R.string.SetUrlInvalidLong));
                }
            } else {
                if (view != null) {
                    view.showCheckText(LocaleController.getString("SetUrlInvalidLong", R.string.SetUrlInvalidLong), Theme.key_windowBackgroundWhiteRedText4);
                }
            }
            return false;
        }

        if (!alert) {
            String currentUrl = info != null && info.slug != null ? info.slug : "";
            if (url.equals(currentUrl)) {
                if (view != null) {
                    view.showCheckText(LocaleController.formatString("SetUrlAvailable", R.string.SetUrlAvailable, url), Theme.key_windowBackgroundWhiteGreenText);
                }
                return true;
            }

            if (view != null) {
                view.showCheckText(LocaleController.getString("SetUrlChecking", R.string.SetUrlChecking), Theme.key_windowBackgroundWhiteGrayText8);
            }
            lastCheckName = url;
            checkRunnable = () -> {
                checkReqId = themeRepository.checkThemeSlug(url, new ThemeRepository.SlugCheckCallback() {
                    @Override
                    public void onResult(boolean available, boolean inUse) {
                        checkReqId = 0;
                        if (lastCheckName != null && lastCheckName.equals(url) && view != null) {
                            if (available) {
                                view.showCheckText(LocaleController.formatString("SetUrlAvailable", R.string.SetUrlAvailable, url), Theme.key_windowBackgroundWhiteGreenText);
                                lastNameAvailable = true;
                            } else {
                                view.showCheckText(LocaleController.getString("SetUrlInUse", R.string.SetUrlInUse), Theme.key_windowBackgroundWhiteRedText4);
                                lastNameAvailable = false;
                            }
                        }
                    }

                    @Override
                    public void onError() {
                        checkReqId = 0;
                    }
                });
            };
            AndroidUtilities.runOnUIThread(checkRunnable, 300);
        }
        return true;
    }

    public void saveTheme(String newName, String newUrl) {
        if (!checkUrl(newUrl, true)) {
            return;
        }
        if (TextUtils.isEmpty(newName)) {
            if (view != null) {
                view.showNameInvalidAlert();
            }
            return;
        }
        if (creatingNewTheme) {
            if (view != null) {
                view.showProgressDialog(null);
            }
            themeInfo.name = info.title = newName;
            themeInfo.info.slug = newUrl;
            themeRepository.saveCurrentTheme(themeInfo, true, true, true);
            return;
        }
        String currentUrl = info.slug == null ? "" : info.slug;
        String currentName = info.title == null ? "" : info.title;
        if (currentUrl.equals(newUrl) && currentName.equals(newName)) {
            if (view != null) {
                view.finishScreen();
            }
            return;
        }

        if (view != null) {
            view.showProgressDialog(() -> {
                if (updateReqId != 0) {
                    themeRepository.cancelRequest(updateReqId);
                    updateReqId = 0;
                }
            });
        }

        updateReqId = themeRepository.updateTheme(info.id, info.access_hash, newUrl, newName, new ThemeRepository.UpdateThemeCallback() {
            @Override
            public void onSuccess(TLRPC.TL_theme theme) {
                updateReqId = 0;
                themeRepository.setThemeUploadInfo(themeInfo, themeAccent, theme, false);
                if (view != null) {
                    view.dismissProgressDialog();
                    view.finishScreen();
                }
            }

            @Override
            public void onError(TLRPC.TL_error error, TLRPC.TL_account_updateTheme req) {
                updateReqId = 0;
                if (view != null) {
                    view.dismissProgressDialog();
                    view.showUpdateError(error, req);
                }
            }
        });
    }

    public boolean isLastNameAvailable() {
        return lastNameAvailable;
    }

    public TLRPC.TL_theme getThemeInfo() {
        return info;
    }

    public boolean isCreatingNewTheme() {
        return creatingNewTheme;
    }
}
