package org.telegram.core.data.repository;

import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;

public interface ThemeRepository {

    int checkThemeSlug(String slug, SlugCheckCallback callback);

    void cancelRequest(int reqId);

    int updateTheme(long themeId, long accessHash, String slug, String title, UpdateThemeCallback callback);

    void saveCurrentTheme(Theme.ThemeInfo themeInfo, boolean create, boolean save, boolean createAccent);

    void setThemeUploadInfo(Theme.ThemeInfo themeInfo, Theme.ThemeAccent themeAccent, TLRPC.TL_theme theme, boolean createAccent);

    void applyTheme(Theme.ThemeInfo themeInfo, boolean night);

    interface SlugCheckCallback {
        void onResult(boolean available, boolean inUse);
        void onError();
    }

    interface UpdateThemeCallback {
        void onSuccess(TLRPC.TL_theme theme);
        void onError(TLRPC.TL_error error, TLRPC.TL_account_updateTheme req);
    }
}
