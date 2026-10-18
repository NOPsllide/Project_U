package org.telegram.core.data.repository;

import android.content.SharedPreferences;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.Utilities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    void applyLanguage(LocaleController.LocaleInfo localeInfo, boolean override, boolean init, boolean fromFile, boolean force, int account);

    boolean isTranslateButtonEnabled();

    void setTranslateButtonEnabled(boolean enabled);

    LocaleController.LocaleInfo getLanguageFromDict(String langCode);

    void searchLanguages(List<LocaleController.LocaleInfo> unofficial, List<LocaleController.LocaleInfo> sorted, String query, Utilities.Callback<ArrayList<LocaleController.LocaleInfo>> callback);
}
