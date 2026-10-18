package org.telegram.core.presenter;

import org.telegram.core.data.repository.IntroRepository;
import org.telegram.core.data.repository.IntroRepositoryImpl;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;

public class IntroPresenter implements NotificationCenter.NotificationCenterDelegate {

    public interface IntroView {
        void onSuggestedLanguageLoaded(String buttonText, LocaleController.LocaleInfo localeInfo);
        void onLanguageApplied();
    }

    private final int currentAccount;
    private final IntroRepository repository;
    private IntroView view;

    public IntroPresenter(int currentAccount) {
        this(currentAccount, new IntroRepositoryImpl(currentAccount));
    }

    public IntroPresenter(int currentAccount, IntroRepository repository) {
        this.currentAccount = currentAccount;
        this.repository = repository;
    }

    public void attachView(IntroView view) {
        this.view = view;
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.suggestedLangpack);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.configLoaded);
    }

    public void detachView() {
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.suggestedLangpack);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.configLoaded);
        this.view = null;
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.suggestedLangpack || id == NotificationCenter.configLoaded) {
            checkSuggestedLanguage();
        }
    }

    public void recordCrashTime(long time) {
        repository.recordCrashTime(time);
    }

    public void resetCrashTime() {
        repository.resetCrashTime();
    }

    public void init() {
        repository.updateDcSettings();
        repository.loadRemoteLanguages();
        checkSuggestedLanguage();
    }

    public void checkSuggestedLanguage() {
        repository.checkSuggestedLanguage((buttonText, localeInfo) -> {
            if (view != null) {
                view.onSuggestedLanguageLoaded(buttonText, localeInfo);
            }
        });
    }

    public void applyLanguage(LocaleController.LocaleInfo info) {
        repository.applyLanguage(info, () -> {
            if (view != null) {
                view.onLanguageApplied();
            }
        });
    }
}
