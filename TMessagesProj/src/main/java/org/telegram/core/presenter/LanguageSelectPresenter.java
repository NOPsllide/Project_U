package org.telegram.core.presenter;

import android.content.SharedPreferences;
import org.telegram.core.data.repository.LanguageRepository;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

public class LanguageSelectPresenter {

    public interface View {
        void notifyDataSetChanged();
        void showSearchResults(ArrayList<LocaleController.LocaleInfo> results);
        void showDeleteLanguageDialog(LocaleController.LocaleInfo localeInfo);
        void onLanguageApplied();
        void finishFragment();
    }

    private final LanguageRepository languageRepository;
    private final int currentAccount;
    private View view;

    private ArrayList<LocaleController.LocaleInfo> sortedLanguages = new ArrayList<>();
    private ArrayList<LocaleController.LocaleInfo> unofficialLanguages = new ArrayList<>();
    private ArrayList<LocaleController.LocaleInfo> searchResult;
    private boolean searching;

    public LanguageSelectPresenter(LanguageRepository languageRepository, int currentAccount) {
        this.languageRepository = languageRepository;
        this.currentAccount = currentAccount;
    }

    public void attachView(View view) {
        this.view = view;
        fillLanguages();
        languageRepository.loadRemoteLanguages(currentAccount, false);
    }

    public void detachView() {
        this.view = null;
    }

    public void fillLanguages() {
        LocaleController.LocaleInfo currentLocale = languageRepository.getCurrentLocaleInfo();
        Comparator<LocaleController.LocaleInfo> comparator = (o, o2) -> {
            if (o == currentLocale) {
                return -1;
            } else if (o2 == currentLocale) {
                return 1;
            } else if (o.serverIndex == o2.serverIndex) {
                return o.name.compareTo(o2.name);
            }
            if (o.serverIndex > o2.serverIndex) {
                return 1;
            } else if (o.serverIndex < o2.serverIndex) {
                return -1;
            }
            return 0;
        };

        sortedLanguages = new ArrayList<>();
        unofficialLanguages = new ArrayList<>(languageRepository.getUnofficialLanguages());

        ArrayList<LocaleController.LocaleInfo> arrayList = languageRepository.getLanguages();
        if (arrayList != null) {
            for (int a = 0, size = arrayList.size(); a < size; a++) {
                LocaleController.LocaleInfo info = arrayList.get(a);
                if (info != null && info.serverIndex != Integer.MAX_VALUE) {
                    sortedLanguages.add(info);
                } else if (info != null) {
                    unofficialLanguages.add(info);
                }
            }
        }
        Collections.sort(sortedLanguages, comparator);
        Collections.sort(unofficialLanguages, comparator);
    }

    public void search(final String query) {
        if (query == null) {
            searching = false;
            searchResult = null;
            if (view != null) {
                view.showSearchResults(null);
            }
        } else {
            searching = true;
            languageRepository.searchLanguages(unofficialLanguages, sortedLanguages, query, results -> {
                AndroidUtilities.runOnUIThread(() -> {
                    searchResult = results;
                    if (view != null) {
                        view.showSearchResults(results);
                    }
                });
            });
        }
    }

    public void selectLanguage(LocaleController.LocaleInfo localeInfo) {
        if (localeInfo == null) {
            return;
        }
        LocaleController.LocaleInfo prevLocale = languageRepository.getCurrentLocaleInfo();
        languageRepository.applyLanguage(localeInfo, true, false, false, true, currentAccount);

        String langCode = localeInfo.pluralLangCode;
        String prevLangCode = prevLocale != null ? prevLocale.pluralLangCode : null;
        HashSet<String> selectedLanguages = languageRepository.getRestrictedLanguages();
        HashSet<String> newSelectedLanguages = new HashSet<>(selectedLanguages);

        if (selectedLanguages.contains(langCode)) {
            newSelectedLanguages.remove(langCode);
            if (prevLangCode != null && !selectedLanguages.contains(prevLangCode)) {
                newSelectedLanguages.add(prevLangCode);
            }
        }
        languageRepository.saveRestrictedLanguages(newSelectedLanguages);

        if (view != null) {
            view.onLanguageApplied();
            view.finishFragment();
        }
    }

    public void onItemClick(int position, boolean search) {
        if (!search) {
            position -= 2;
        }
        LocaleController.LocaleInfo localeInfo;
        if (search) {
            localeInfo = (searchResult != null && position >= 0 && position < searchResult.size()) ? searchResult.get(position) : null;
        } else if (!unofficialLanguages.isEmpty() && position >= 0 && position < unofficialLanguages.size()) {
            localeInfo = unofficialLanguages.get(position);
        } else {
            if (!unofficialLanguages.isEmpty()) {
                position -= unofficialLanguages.size() + 1;
            }
            localeInfo = (sortedLanguages != null && position >= 0 && position < sortedLanguages.size()) ? sortedLanguages.get(position) : null;
        }
        if (localeInfo != null) {
            selectLanguage(localeInfo);
        }
    }

    public boolean onItemLongClick(int position, boolean search) {
        if (!search) {
            position -= 2;
        }
        LocaleController.LocaleInfo localeInfo;
        if (search) {
            localeInfo = (searchResult != null && position >= 0 && position < searchResult.size()) ? searchResult.get(position) : null;
        } else if (!unofficialLanguages.isEmpty() && position >= 0 && position < unofficialLanguages.size()) {
            localeInfo = unofficialLanguages.get(position);
        } else {
            if (!unofficialLanguages.isEmpty()) {
                position -= unofficialLanguages.size() + 1;
            }
            localeInfo = (sortedLanguages != null && position >= 0 && position < sortedLanguages.size()) ? sortedLanguages.get(position) : null;
        }
        if (localeInfo == null || localeInfo.pathToFile == null || (localeInfo.isRemote() && localeInfo.serverIndex != Integer.MAX_VALUE)) {
            return false;
        }
        if (view != null) {
            view.showDeleteLanguageDialog(localeInfo);
        }
        return true;
    }

    public void deleteLanguage(LocaleController.LocaleInfo localeInfo) {
        if (localeInfo == null) {
            return;
        }
        if (languageRepository.deleteLanguage(localeInfo, currentAccount)) {
            fillLanguages();
            if (searchResult != null) {
                searchResult.remove(localeInfo);
            }
            if (view != null) {
                view.notifyDataSetChanged();
            }
        }
    }

    public LocaleController.LocaleInfo getItem(int position, boolean search) {
        if (!search) {
            position -= 2;
        }
        if (search) {
            return (searchResult != null && position >= 0 && position < searchResult.size()) ? searchResult.get(position) : null;
        } else if (!unofficialLanguages.isEmpty() && position >= 0 && position < unofficialLanguages.size()) {
            return unofficialLanguages.get(position);
        } else {
            if (!unofficialLanguages.isEmpty()) {
                position -= unofficialLanguages.size() + 1;
            }
            return (sortedLanguages != null && position >= 0 && position < sortedLanguages.size()) ? sortedLanguages.get(position) : null;
        }
    }

    public boolean isLastItem(int position, boolean search) {
        if (search) {
            return searchResult != null && position == searchResult.size() - 1;
        } else {
            position -= 2;
            if (!unofficialLanguages.isEmpty() && position >= 0 && position < unofficialLanguages.size()) {
                return position == unofficialLanguages.size() - 1;
            } else {
                if (!unofficialLanguages.isEmpty()) {
                    position -= unofficialLanguages.size() + 1;
                }
                return sortedLanguages != null && position == sortedLanguages.size() - 1;
            }
        }
    }

    public boolean isCurrentLocale(LocaleController.LocaleInfo localeInfo) {
        return localeInfo != null && localeInfo == languageRepository.getCurrentLocaleInfo();
    }

    public boolean isDividerMiddle(int position) {
        position--;
        return !unofficialLanguages.isEmpty() && position == unofficialLanguages.size();
    }

    public int getItemCount(boolean search) {
        if (search) {
            return searchResult != null ? searchResult.size() : 0;
        } else {
            int count = sortedLanguages.size();
            if (count != 0) {
                count++;
            }
            if (!unofficialLanguages.isEmpty()) {
                count += unofficialLanguages.size() + 1;
            }
            return 2 + count;
        }
    }

    public int getItemViewType(int i, boolean search) {
        if (!search) {
            i -= 2;
        }
        if (i == -2) {
            return 2;
        }
        if (i == -1) {
            return 3;
        }
        if (search) {
            return 0;
        }
        if (!unofficialLanguages.isEmpty() && (i == unofficialLanguages.size() || i == unofficialLanguages.size() + sortedLanguages.size() + 1) || (unofficialLanguages.isEmpty() && i == sortedLanguages.size())) {
            return 1;
        }
        return 0;
    }

    public void onNotificationReceived(int id) {
        if (id == NotificationCenter.suggestedLangpack) {
            fillLanguages();
            if (view != null) {
                view.notifyDataSetChanged();
            }
        }
    }

    public boolean isTranslateButtonEnabled() {
        return languageRepository.isTranslateButtonEnabled();
    }

    public void setTranslateButtonEnabled(boolean enabled) {
        languageRepository.setTranslateButtonEnabled(enabled);
    }

    public ArrayList<String> getRestrictedLanguages() {
        LocaleController.LocaleInfo info = languageRepository.getCurrentLocaleInfo();
        String currentLang = info != null ? info.pluralLangCode : null;
        ArrayList<String> langCodes = new ArrayList<>(languageRepository.getRestrictedLanguages());
        if (currentLang != null && !langCodes.contains(currentLang)) {
            langCodes.add(currentLang);
        }
        return langCodes;
    }

    public LocaleController.LocaleInfo getLanguageFromDict(String langCode) {
        return languageRepository.getLanguageFromDict(langCode);
    }

    public void registerSettingsChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        languageRepository.registerSettingsChangeListener(listener);
    }

    public void unregisterSettingsChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        languageRepository.unregisterSettingsChangeListener(listener);
    }
}
