package org.telegram.core.presenter;

import android.content.SharedPreferences;
import org.telegram.core.data.repository.LanguageRepository;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

public class RestrictedLanguagesPresenter {

    public interface View {
        void notifyDataSetChanged();
        void notifyItemChanged(int position);
        void shakeView(int position);
        void showDeleteLanguageDialog(LocaleController.LocaleInfo localeInfo);
    }

    private final LanguageRepository languageRepository;
    private final int currentAccount;
    private View view;

    private HashSet<String> selectedLanguages;
    private ArrayList<LocaleController.LocaleInfo> sortedLanguages = new ArrayList<>();
    private ArrayList<LocaleController.LocaleInfo> searchResult;
    private boolean searching;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceListener;

    public RestrictedLanguagesPresenter(LanguageRepository languageRepository, int currentAccount) {
        this.languageRepository = languageRepository;
        this.currentAccount = currentAccount;
    }

    public void attachView(View view) {
        this.view = view;
        selectedLanguages = languageRepository.getRestrictedLanguages();
        if (preferenceListener == null) {
            preferenceListener = (sharedPreferences, s) -> onSharedPreferenceChanged();
        }
        languageRepository.registerSettingsChangeListener(preferenceListener);
        fillLanguages();
        languageRepository.loadRemoteLanguages(currentAccount);
    }

    public void detachView() {
        if (preferenceListener != null) {
            languageRepository.unregisterSettingsChangeListener(preferenceListener);
        }
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
        ArrayList<LocaleController.LocaleInfo> arrayList = languageRepository.getLanguages();
        if (arrayList != null) {
            for (int a = 0, size = arrayList.size(); a < size; a++) {
                LocaleController.LocaleInfo info = arrayList.get(a);
                if (info != null && info.serverIndex != Integer.MAX_VALUE) {
                    sortedLanguages.add(info);
                }
            }
        }
        Collections.sort(sortedLanguages, comparator);
    }

    public void search(final String query) {
        if (query == null) {
            searchResult = null;
            searching = false;
        } else {
            searching = true;
            String q = query.trim().toLowerCase();
            if (q.length() == 0) {
                searchResult = new ArrayList<>();
            } else {
                ArrayList<LocaleController.LocaleInfo> resultArray = new ArrayList<>();
                for (int a = 0, n = sortedLanguages.size(); a < n; a++) {
                    LocaleController.LocaleInfo c = sortedLanguages.get(a);
                    if ((c.name != null && c.name.toLowerCase().startsWith(q)) || (c.nameEnglish != null && c.nameEnglish.toLowerCase().startsWith(q))) {
                        resultArray.add(c);
                    }
                }
                searchResult = resultArray;
            }
        }
        if (view != null) {
            view.notifyDataSetChanged();
        }
    }

    public void onItemClick(int position, boolean isSearch) {
        if (!isSearch) {
            position--;
        }
        LocaleController.LocaleInfo localeInfo = isSearch ? (searchResult != null && position >= 0 && position < searchResult.size() ? searchResult.get(position) : null) : (sortedLanguages != null && position >= 0 && position < sortedLanguages.size() ? sortedLanguages.get(position) : null);
        if (localeInfo != null) {
            LocaleController.LocaleInfo currentLocaleInfo = languageRepository.getCurrentLocaleInfo();
            String langCode = localeInfo.pluralLangCode;
            if (langCode != null && currentLocaleInfo != null && langCode.equals(currentLocaleInfo.pluralLangCode)) {
                if (view != null) {
                    view.shakeView(isSearch ? position : position + 1);
                }
                return;
            }
            if (selectedLanguages == null) {
                selectedLanguages = languageRepository.getRestrictedLanguages();
            }
            HashSet<String> newSelectedLanguages = new HashSet<>(selectedLanguages);
            if (newSelectedLanguages.contains(langCode)) {
                newSelectedLanguages.remove(langCode);
            } else {
                if (langCode != null) {
                    newSelectedLanguages.add(langCode);
                }
            }
            languageRepository.saveRestrictedLanguages(newSelectedLanguages);
        }
    }

    public boolean onItemLongClick(int position, boolean isSearch) {
        if (!isSearch) {
            position--;
        }
        LocaleController.LocaleInfo localeInfo = isSearch ? (searchResult != null && position >= 0 && position < searchResult.size() ? searchResult.get(position) : null) : (sortedLanguages != null && position >= 0 && position < sortedLanguages.size() ? sortedLanguages.get(position) : null);
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

    public LocaleController.LocaleInfo getItem(int position, boolean isSearch) {
        if (!isSearch) {
            position--;
        }
        if (isSearch) {
            if (searchResult != null && position >= 0 && position < searchResult.size()) {
                return searchResult.get(position);
            }
            return null;
        } else {
            if (sortedLanguages != null && position >= 0 && position < sortedLanguages.size()) {
                return sortedLanguages.get(position);
            }
            return null;
        }
    }

    public boolean isLastItem(int position, boolean isSearch) {
        if (isSearch) {
            return searchResult != null && position == searchResult.size() - 1;
        } else {
            return sortedLanguages != null && (position - 1) == sortedLanguages.size() - 1;
        }
    }

    public int getItemCount(boolean isSearch) {
        if (isSearch) {
            return searchResult != null ? searchResult.size() : 0;
        } else {
            return 1 + (sortedLanguages != null ? sortedLanguages.size() : 0);
        }
    }

    public int getItemViewType(int position, boolean isSearch) {
        if (!isSearch) {
            position--;
        }
        if (position == -1) {
            return 2;
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

    public boolean isLanguageSelected(String langCode) {
        return selectedLanguages != null && selectedLanguages.contains(langCode);
    }

    public boolean isCurrentLocale(String langCode) {
        LocaleController.LocaleInfo info = languageRepository.getCurrentLocaleInfo();
        return langCode != null && info != null && langCode.equals(info.pluralLangCode);
    }

    private void onSharedPreferenceChanged() {
        HashSet<String> newSelectedLanguages = languageRepository.getRestrictedLanguages();
        if (view != null && selectedLanguages != null) {
            int offset = !searching ? 1 : 0;
            for (String lng : selectedLanguages) {
                if (!newSelectedLanguages.contains(lng)) {
                    int pos = langPos(lng);
                    if (pos != -1) {
                        view.notifyItemChanged(pos + offset);
                    }
                }
            }
            for (String lng : newSelectedLanguages) {
                if (!selectedLanguages.contains(lng)) {
                    int pos = langPos(lng);
                    if (pos != -1) {
                        view.notifyItemChanged(pos + offset);
                    }
                }
            }
        }
        selectedLanguages = newSelectedLanguages;
    }

    private int langPos(String lng) {
        if (lng == null) {
            return -1;
        }
        ArrayList<LocaleController.LocaleInfo> arr = searching ? searchResult : sortedLanguages;
        if (arr == null) {
            return -1;
        }
        for (int i = 0; i < arr.size(); ++i) {
            LocaleController.LocaleInfo info = arr.get(i);
            if (info != null && lng.equals(info.pluralLangCode)) {
                return i;
            }
        }
        return -1;
    }
}
