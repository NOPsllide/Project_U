package org.telegram.core.data.repository;

import org.telegram.messenger.LocaleController;

public interface IntroRepository {

    interface SuggestedLanguageCallback {
        void onSuggestedLanguageLoaded(String buttonText, LocaleController.LocaleInfo localeInfo);
    }

    interface LanguageAppliedCallback {
        void onLanguageApplied();
    }

    void recordCrashTime(long time);
    void resetCrashTime();
    void updateDcSettings();
    void loadRemoteLanguages();
    void checkSuggestedLanguage(SuggestedLanguageCallback callback);
    void applyLanguage(LocaleController.LocaleInfo info, LanguageAppliedCallback callback);
}
