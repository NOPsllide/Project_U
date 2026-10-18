package org.telegram.core.presenter;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.core.data.repository.CountryRepository;
import org.telegram.ui.CountrySelectActivity.Country;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class CountrySelectPresenter {

    public interface View {
        void showSearchResults(ArrayList<Country> results);
        void onCountrySelected(Country country);
        void finishFragment();
    }

    private final CountryRepository countryRepository;
    private final boolean needPhoneCode;
    private View view;

    private HashMap<String, ArrayList<Country>> countries = new HashMap<>();
    private ArrayList<String> sortedCountries = new ArrayList<>();
    private List<Country> countryList = new ArrayList<>();
    private Map<Country, List<String>> countrySearchMap = new HashMap<>();
    private ArrayList<Country> searchResult;
    private Timer searchTimer;

    public CountrySelectPresenter(CountryRepository countryRepository, boolean needPhoneCode) {
        this.countryRepository = countryRepository;
        this.needPhoneCode = needPhoneCode;
    }

    public void attachView(View view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
        cancelSearchTimer();
    }

    public void loadCountries(ArrayList<Country> existingCountries) {
        List<Country> loaded = countryRepository.loadCountries(existingCountries);
        countries.clear();
        sortedCountries.clear();
        countryList.clear();
        countrySearchMap.clear();

        for (Country c : loaded) {
            String n = c.name.substring(0, 1).toUpperCase();
            ArrayList<Country> arr = countries.get(n);
            if (arr == null) {
                arr = new ArrayList<>();
                countries.put(n, arr);
                sortedCountries.add(n);
            }
            arr.add(c);
        }

        Collections.sort(sortedCountries, String::compareTo);

        for (ArrayList<Country> arr : countries.values()) {
            Collections.sort(arr, (country, country2) -> country.name.compareTo(country2.name));
            for (Country country : arr) {
                countryList.add(country);
                countrySearchMap.put(country, Arrays.asList(country.name.split(" ")));
            }
        }
    }

    public void search(final String query) {
        if (query == null) {
            searchResult = null;
            cancelSearchTimer();
        } else {
            cancelSearchTimer();
            searchTimer = new Timer();
            searchTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    cancelSearchTimer();
                    countryRepository.searchCountries(countryList, countrySearchMap, query, results -> {
                        AndroidUtilities.runOnUIThread(() -> {
                            searchResult = results;
                            if (view != null) {
                                view.showSearchResults(results);
                            }
                        });
                    });
                }
            }, 100, 300);
        }
    }

    private void cancelSearchTimer() {
        try {
            if (searchTimer != null) {
                searchTimer.cancel();
                searchTimer = null;
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public void selectCountry(Country country) {
        if (view != null) {
            view.finishFragment();
            if (country != null) {
                view.onCountrySelected(country);
            }
        }
    }

    public Country getItem(int section, int position) {
        if (section < 0 || section >= sortedCountries.size()) {
            return null;
        }
        ArrayList<Country> arr = countries.get(sortedCountries.get(section));
        if (arr == null || position < 0 || position >= arr.size()) {
            return null;
        }
        return arr.get(position);
    }

    public Country getSearchItem(int position) {
        if (searchResult == null || position < 0 || position >= searchResult.size()) {
            return null;
        }
        return searchResult.get(position);
    }

    public int getSectionCount() {
        return sortedCountries.size();
    }

    public int getCountForSection(int section) {
        if (section < 0 || section >= sortedCountries.size()) {
            return 0;
        }
        ArrayList<Country> arr = countries.get(sortedCountries.get(section));
        int count = arr != null ? arr.size() : 0;
        if (section != sortedCountries.size() - 1) {
            count++;
        }
        return count;
    }

    public boolean isRowEnabled(int section, int row) {
        if (section < 0 || section >= sortedCountries.size()) {
            return false;
        }
        ArrayList<Country> arr = countries.get(sortedCountries.get(section));
        return arr != null && row < arr.size();
    }

    public boolean isDivider(int section, int position) {
        if (section < 0 || section >= sortedCountries.size()) {
            return true;
        }
        ArrayList<Country> arr = countries.get(sortedCountries.get(section));
        return arr == null || position >= arr.size();
    }

    public String getLetterForSection(int section) {
        if (section < 0 || section >= sortedCountries.size()) {
            if (!sortedCountries.isEmpty()) {
                return sortedCountries.get(sortedCountries.size() - 1);
            }
            return "";
        }
        return sortedCountries.get(section);
    }

    public int getSearchResultCount() {
        return searchResult != null ? searchResult.size() : 0;
    }

    public boolean isNeedPhoneCode() {
        return needPhoneCode;
    }

    public HashMap<String, ArrayList<Country>> getCountries() {
        return countries;
    }

    public ArrayList<String> getSortedCountries() {
        return sortedCountries;
    }

    public ArrayList<Country> getSearchResult() {
        return searchResult;
    }
}
