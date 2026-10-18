package org.telegram.core.data.repository;

import android.content.SharedPreferences;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class LanguageRepositoryImpl implements LanguageRepository {

    @Override
    public ArrayList<LocaleController.LocaleInfo> getLanguages() {
        return LocaleController.getInstance().languages;
    }

    @Override
    public ArrayList<LocaleController.LocaleInfo> getUnofficialLanguages() {
        return LocaleController.getInstance().unofficialLanguages;
    }

    @Override
    public LocaleController.LocaleInfo getCurrentLocaleInfo() {
        return LocaleController.getInstance().getCurrentLocaleInfo();
    }

    @Override
    public void loadRemoteLanguages(int account) {
        LocaleController.getInstance().loadRemoteLanguages(account);
    }

    @Override
    public void loadRemoteLanguages(int account, boolean applyCurrent) {
        LocaleController.getInstance().loadRemoteLanguages(account, applyCurrent);
    }

    @Override
    public boolean deleteLanguage(LocaleController.LocaleInfo localeInfo, int account) {
        return LocaleController.getInstance().deleteLanguage(localeInfo, account);
    }

    @Override
    public HashSet<String> getRestrictedLanguages() {
        return new HashSet<>(MessagesController.getGlobalMainSettings().getStringSet("translate_button_restricted_languages", new HashSet<>()));
    }

    @Override
    public void saveRestrictedLanguages(Set<String> languages) {
        LocaleController.LocaleInfo currentLocaleInfo = getCurrentLocaleInfo();
        SharedPreferences.Editor editor = MessagesController.getGlobalMainSettings().edit();
        if (languages == null || (languages.size() == 1 && currentLocaleInfo != null && languages.contains(currentLocaleInfo.pluralLangCode))) {
            editor.remove("translate_button_restricted_languages").apply();
        } else {
            editor.putStringSet("translate_button_restricted_languages", languages).apply();
        }
    }

    @Override
    public void registerSettingsChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        MessagesController.getGlobalMainSettings().registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void unregisterSettingsChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        MessagesController.getGlobalMainSettings().unregisterOnSharedPreferenceChangeListener(listener);
    }
}
