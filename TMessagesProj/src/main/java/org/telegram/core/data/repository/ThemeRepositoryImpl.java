package org.telegram.core.data.repository;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;

public class ThemeRepositoryImpl implements ThemeRepository {

    private final int accountNum;

    public ThemeRepositoryImpl(int accountNum) {
        this.accountNum = accountNum;
    }

    @Override
    public int checkThemeSlug(String slug, SlugCheckCallback callback) {
        TLRPC.TL_account_createTheme req = new TLRPC.TL_account_createTheme();
        req.slug = slug;
        req.title = "";
        req.document = new TLRPC.TL_inputDocumentEmpty();
        return ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (callback == null) {
                return;
            }
            if (error == null || (!"THEME_SLUG_INVALID".equals(error.text) && !"THEME_SLUG_OCCUPIED".equals(error.text))) {
                callback.onResult(true, false);
            } else {
                callback.onResult(false, true);
            }
        }), ConnectionsManager.RequestFlagFailOnServerErrors);
    }

    @Override
    public void cancelRequest(int reqId) {
        if (reqId != 0) {
            ConnectionsManager.getInstance(accountNum).cancelRequest(reqId, true);
        }
    }

    @Override
    public int updateTheme(long themeId, long accessHash, String slug, String title, UpdateThemeCallback callback) {
        TLRPC.TL_account_updateTheme req = new TLRPC.TL_account_updateTheme();
        TLRPC.TL_inputTheme inputTheme = new TLRPC.TL_inputTheme();
        inputTheme.id = themeId;
        inputTheme.access_hash = accessHash;
        req.theme = inputTheme;
        req.format = "android";
        req.slug = slug;
        req.flags |= 1;
        req.title = title;
        req.flags |= 2;

        return ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> {
            if (response instanceof TLRPC.TL_theme) {
                TLRPC.TL_theme theme = (TLRPC.TL_theme) response;
                AndroidUtilities.runOnUIThread(() -> {
                    if (callback != null) {
                        callback.onSuccess(theme);
                    }
                });
            } else {
                AndroidUtilities.runOnUIThread(() -> {
                    if (callback != null) {
                        callback.onError(error, req);
                    }
                });
            }
        }, ConnectionsManager.RequestFlagFailOnServerErrors);
    }

    @Override
    public void saveCurrentTheme(Theme.ThemeInfo themeInfo, boolean create, boolean save, boolean createAccent) {
        Theme.saveCurrentTheme(themeInfo, create, save, createAccent);
    }

    @Override
    public void setThemeUploadInfo(Theme.ThemeInfo themeInfo, Theme.ThemeAccent themeAccent, TLRPC.TL_theme theme, boolean createAccent) {
        Theme.setThemeUploadInfo(themeInfo, themeAccent, theme, accountNum, createAccent);
    }

    @Override
    public void applyTheme(Theme.ThemeInfo themeInfo, boolean night) {
        Theme.applyTheme(themeInfo, night);
    }
}
