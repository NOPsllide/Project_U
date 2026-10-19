package org.telegram.core.data.repository;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import java.util.ArrayList;

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

    @Override
    public void loadWallpapersStorage() {
        MessagesStorage.getInstance(accountNum).getWallpapers();
    }

    @Override
    public void deleteWallpaper(long id) {
        MessagesStorage.getInstance(accountNum).deleteWallpaper(id);
    }

    @Override
    public void putWallpapers(ArrayList<TLRPC.WallPaper> wallpapers, int type) {
        MessagesStorage.getInstance(accountNum).putWallpapers(wallpapers, type);
    }

    @Override
    public int getWallpapers(long hash, WallpapersCallback callback) {
        TLRPC.TL_account_getWallPapers req = new TLRPC.TL_account_getWallPapers();
        req.hash = hash;
        return ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (response instanceof TLRPC.TL_account_wallPapers) {
                TLRPC.TL_account_wallPapers res = (TLRPC.TL_account_wallPapers) response;
                if (callback != null) {
                    callback.onSuccess(res.wallpapers);
                }
            } else {
                if (callback != null) {
                    callback.onError();
                }
            }
        }));
    }

    @Override
    public int resetWallpapers(Runnable onComplete) {
        TLRPC.TL_account_resetWallPapers req = new TLRPC.TL_account_resetWallPapers();
        return ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (onComplete != null) {
                onComplete.run();
            }
        }));
    }

    @Override
    public int saveWallpaper(TLRPC.WallPaper wallPaper, boolean unsave, Runnable onComplete) {
        TLRPC.TL_account_saveWallPaper req = new TLRPC.TL_account_saveWallPaper();
        req.settings = new TLRPC.TL_wallPaperSettings();
        req.unsave = unsave;
        if (wallPaper instanceof TLRPC.TL_wallPaperNoFile) {
            TLRPC.TL_inputWallPaperNoFile inputWallPaper = new TLRPC.TL_inputWallPaperNoFile();
            inputWallPaper.id = wallPaper.id;
            req.wallpaper = inputWallPaper;
        } else {
            TLRPC.TL_inputWallPaper inputWallPaper = new TLRPC.TL_inputWallPaper();
            inputWallPaper.id = wallPaper.id;
            inputWallPaper.access_hash = wallPaper.access_hash;
            req.wallpaper = inputWallPaper;
        }
        return ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (onComplete != null) {
                onComplete.run();
            }
        }));
    }

    @Override
    public long calcHash(long hash, long id) {
        return MediaDataController.calcHash(hash, id);
    }

    @Override
    public void saveThemeToServer(Theme.ThemeInfo themeInfo, Theme.ThemeAccent accent) {
        MessagesController.getInstance(accountNum).saveThemeToServer(themeInfo, accent);
    }

    @Override
    public void saveTheme(Theme.ThemeInfo themeInfo, Theme.ThemeAccent accent, boolean night, boolean delete) {
        MessagesController.getInstance(themeInfo != null ? themeInfo.account : accountNum).saveTheme(themeInfo, accent, night, delete);
    }

    @Override
    public boolean deleteTheme(Theme.ThemeInfo themeInfo) {
        return Theme.deleteTheme(themeInfo);
    }

    @Override
    public void postNotification(int id, Object... args) {
        NotificationCenter.getInstance(accountNum).postNotificationName(id, args);
    }

    @Override
    public void postGlobalNotification(int id, Object... args) {
        NotificationCenter.getGlobalInstance().postNotificationName(id, args);
    }
}
