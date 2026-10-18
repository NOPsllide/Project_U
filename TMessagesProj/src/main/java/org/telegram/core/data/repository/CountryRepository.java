package org.telegram.core.data.repository;

import org.telegram.ui.CountrySelectActivity.Country;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface CountryRepository {

    interface Callback<T> {
        void onResult(T result);
    }

    List<Country> loadCountries(ArrayList<Country> existingCountries);

    void searchCountries(List<Country> countryList, Map<Country, List<String>> countrySearchMap, String query, Callback<ArrayList<Country>> callback);
}
