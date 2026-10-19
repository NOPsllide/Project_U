package org.telegram.core.presenter;

import org.telegram.core.data.repository.ThemeRepository;
import org.telegram.core.data.repository.ThemeRepositoryImpl;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;

public class ThemePreviewPresenter {

    public interface ThemePreviewView {
    }

    private ThemePreviewView view;
    private final ThemeRepository repository;
    private final int currentAccount;

    public ThemePreviewPresenter(int currentAccount) {
        this(new ThemeRepositoryImpl(currentAccount), currentAccount);
    }

    public ThemePreviewPresenter(ThemeRepository repository, int currentAccount) {
        this.repository = repository;
        this.currentAccount = currentAccount;
    }

    public void attachView(ThemePreviewView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void applyTheme(Theme.ThemeInfo themeInfo, boolean night) {
        repository.applyTheme(themeInfo, night);
    }

    public void saveThemeToServer(Theme.ThemeInfo themeInfo, Theme.ThemeAccent accent) {
        repository.saveThemeToServer(themeInfo, accent);
    }

    public void saveTheme(Theme.ThemeInfo themeInfo, Theme.ThemeAccent accent, boolean night, boolean delete) {
        repository.saveTheme(themeInfo, accent, night, delete);
    }

    public int saveWallpaper(TLRPC.WallPaper wallPaper, boolean unsave, Runnable onComplete) {
        return repository.saveWallpaper(wallPaper, unsave, onComplete);
    }

    public int sendRequest(TLObject request, RequestDelegate delegate) {
        return repository.sendRequest(request, delegate);
    }
}
