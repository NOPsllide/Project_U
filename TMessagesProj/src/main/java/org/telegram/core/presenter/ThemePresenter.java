package org.telegram.core.presenter;

import org.telegram.core.data.repository.ThemeRepository;
import org.telegram.core.data.repository.ThemeRepositoryImpl;
import org.telegram.ui.ActionBar.Theme;

public class ThemePresenter {

    public interface ThemeView {
        void onThemeListUpdated();
        void onThemeAccentListUpdated();
    }

    private ThemeView view;
    private final ThemeRepository repository;
    private final int currentAccount;

    public ThemePresenter(int currentAccount) {
        this(new ThemeRepositoryImpl(currentAccount), currentAccount);
    }

    public ThemePresenter(ThemeRepository repository, int currentAccount) {
        this.repository = repository;
        this.currentAccount = currentAccount;
    }

    public void attachView(ThemeView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void saveThemeToServer(Theme.ThemeInfo themeInfo, Theme.ThemeAccent accent) {
        repository.saveThemeToServer(themeInfo, accent);
    }

    public void saveTheme(Theme.ThemeInfo themeInfo, Theme.ThemeAccent accent, boolean night, boolean delete) {
        repository.saveTheme(themeInfo, accent, night, delete);
    }

    public boolean deleteTheme(Theme.ThemeInfo themeInfo) {
        return repository.deleteTheme(themeInfo);
    }

    public void postNotification(int id, Object... args) {
        repository.postNotification(id, args);
    }

    public void postGlobalNotification(int id, Object... args) {
        repository.postGlobalNotification(id, args);
    }

    public void applyTheme(Theme.ThemeInfo themeInfo, boolean night) {
        repository.applyTheme(themeInfo, night);
    }
}
