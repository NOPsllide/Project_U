package org.telegram.core.presenter;

import android.net.Uri;
import android.text.TextUtils;

import org.telegram.core.data.repository.WebviewRepository;
import org.telegram.core.data.repository.WebviewRepositoryImpl;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.UserConfig;

public class SharePresenter {

    public interface ShareView {
        void showShareDialog(MessageObject messageObject, String link);
        void finishActivity();
    }

    private final int currentAccount;
    private final WebviewRepository repository;
    private ShareView view;

    public SharePresenter() {
        this(UserConfig.selectedAccount);
    }

    public SharePresenter(int currentAccount) {
        this(currentAccount, new WebviewRepositoryImpl(currentAccount));
    }

    public SharePresenter(int currentAccount, WebviewRepository repository) {
        this.currentAccount = currentAccount;
        this.repository = repository;
    }

    public void attachView(ShareView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void processIntentData(Uri data) {
        if (data == null) {
            if (view != null) {
                view.finishActivity();
            }
            return;
        }
        String scheme = data.getScheme();
        String url = data.toString();
        String hash = data.getQueryParameter("hash");
        if (!"tgb".equals(scheme) || !url.toLowerCase().startsWith("tgb://share_game_score") || TextUtils.isEmpty(hash)) {
            if (view != null) {
                view.finishActivity();
            }
            return;
        }
        WebviewRepository.GameShareData shareData = repository.loadGameShareData(hash);
        if (shareData == null || shareData.messageObject == null) {
            if (view != null) {
                view.finishActivity();
            }
            return;
        }
        if (view != null) {
            view.showShareDialog(shareData.messageObject, shareData.link);
        }
    }
}
