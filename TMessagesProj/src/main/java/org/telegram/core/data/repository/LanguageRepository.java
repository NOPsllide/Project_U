package org.telegram.core.data.repository;

import android.content.SharedPreferences;
import org.telegram.messenger.LocaleController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public interface LanguageRepository {

    ArrayList<LocaleController.LocaleInfo> getLanguages();

    ArrayList<LocaleController.LocaleInfo> getUnofficialLanguages();

    LocaleController.LocaleInfo getCurrentLocaleInfo();

    void loadRemoteLanguages(int account);

    void loadRemoteLanguages(int account, boolean applyCurrent);

    boolean deleteLanguage(LocaleController.LocaleInfo localeInfo, int account);

    HashSet<String> getRestrictedLanguages();

    void saveRestrictedLanguages(Set<String> languages);

    void registerSettingsChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener);

    void unregisterSettingsChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener);
}
