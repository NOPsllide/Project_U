package org.telegram.core.data.repository;

import android.content.SharedPreferences;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.Utilities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    @Override
    public void applyLanguage(LocaleController.LocaleInfo localeInfo, boolean override, boolean init, boolean fromFile, boolean force, int account) {
        LocaleController.getInstance().applyLanguage(localeInfo, override, init, fromFile, force, account);
    }

    @Override
    public boolean isTranslateButtonEnabled() {
        return MessagesController.getGlobalMainSettings().getBoolean("translate_button", false);
    }

    @Override
    public void setTranslateButtonEnabled(boolean enabled) {
        MessagesController.getGlobalMainSettings().edit().putBoolean("translate_button", enabled).apply();
    }

    @Override
    public LocaleController.LocaleInfo getLanguageFromDict(String langCode) {
        return LocaleController.getInstance().getLanguageFromDict(langCode);
    }

    @Override
    public void searchLanguages(List<LocaleController.LocaleInfo> unofficial, List<LocaleController.LocaleInfo> sorted, String query, Utilities.Callback<ArrayList<LocaleController.LocaleInfo>> callback) {
        Utilities.searchQueue.postRunnable(() -> {
            String q = query != null ? query.trim().toLowerCase() : "";
            if (q.length() == 0) {
                if (callback != null) {
                    callback.onResult(new ArrayList<>());
                }
                return;
            }
            ArrayList<LocaleController.LocaleInfo> resultArray = new ArrayList<>();
            if (unofficial != null) {
                for (LocaleController.LocaleInfo c : unofficial) {
                    if ((c.name != null && c.name.toLowerCase().startsWith(q)) || (c.nameEnglish != null && c.nameEnglish.toLowerCase().startsWith(q))) {
                        resultArray.add(c);
                    }
                }
            }
            if (sorted != null) {
                for (LocaleController.LocaleInfo c : sorted) {
                    if ((c.name != null && c.name.toLowerCase().startsWith(q)) || (c.nameEnglish != null && c.nameEnglish.toLowerCase().startsWith(q))) {
                        resultArray.add(c);
                    }
                }
            }
            if (callback != null) {
                callback.onResult(resultArray);
            }
        });
    }
}
