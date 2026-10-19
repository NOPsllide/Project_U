package org.telegram.core.presenter;

import org.telegram.core.data.repository.ThemeRepository;
import org.telegram.tgnet.TLRPC;
import java.util.ArrayList;

public class WallpapersListPresenter {

    public interface WallpapersListView {
        void onWallpapersLoaded(ArrayList<TLRPC.WallPaper> wallpapers);
        void onWallpapersLoadError();
        void onWallpapersReset();
    }

    private final ThemeRepository themeRepository;
    private final int currentAccount;
    private WallpapersListView view;

    public WallpapersListPresenter(ThemeRepository themeRepository, int currentAccount) {
        this.themeRepository = themeRepository;
        this.currentAccount = currentAccount;
    }

    public void attachView(WallpapersListView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void loadWallpapersStorage() {
        themeRepository.loadWallpapersStorage();
    }

    public void deleteWallpaper(long id) {
        themeRepository.deleteWallpaper(id);
    }

    public void putWallpapers(ArrayList<TLRPC.WallPaper> wallpapers, int type) {
        themeRepository.putWallpapers(wallpapers, type);
    }

    public void saveWallpaper(TLRPC.WallPaper wallPaper, boolean unsave, Runnable onComplete) {
        themeRepository.saveWallpaper(wallPaper, unsave, onComplete);
    }

    public void resetWallpapers() {
        themeRepository.resetWallpapers(() -> {
            if (view != null) {
                view.onWallpapersReset();
            }
        });
    }

    public long calcHash(long hash, long id) {
        return themeRepository.calcHash(hash, id);
    }

    public void loadWallpapers(long hash) {
        themeRepository.getWallpapers(hash, new ThemeRepository.WallpapersCallback() {
            @Override
            public void onSuccess(ArrayList<TLRPC.WallPaper> wallpapers) {
                if (view != null) {
                    view.onWallpapersLoaded(wallpapers);
                }
            }

            @Override
            public void onError() {
                if (view != null) {
                    view.onWallpapersLoadError();
                }
            }
        });
    }
}
