package org.telegram.core.data.repository;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.Utilities;
import org.telegram.ui.CountrySelectActivity.Country;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CountryRepositoryImpl implements CountryRepository {

    @Override
    public List<Country> loadCountries(ArrayList<Country> existingCountries) {
        if (existingCountries != null && !existingCountries.isEmpty()) {
            return new ArrayList<>(existingCountries);
        }
        ArrayList<Country> countries = new ArrayList<>();
        try {
            InputStream stream = ApplicationLoader.applicationContext.getResources().getAssets().open("countries.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] args = line.split(";");
                Country c = new Country();
                c.name = args[2];
                c.code = args[0];
                c.shortname = args[1];
                countries.add(c);
            }
            reader.close();
            stream.close();
        } catch (Exception e) {
            FileLog.e(e);
        }
        return countries;
    }

    @Override
    public void searchCountries(List<Country> countryList, Map<Country, List<String>> countrySearchMap, String query, Callback<ArrayList<Country>> callback) {
        Utilities.searchQueue.postRunnable(() -> {
            String q = query != null ? query.trim().toLowerCase() : "";
            if (q.length() == 0) {
                if (callback != null) {
                    callback.onResult(new ArrayList<>());
                }
                return;
            }
            ArrayList<Country> resultArray = new ArrayList<>();
            if (countryList != null && countrySearchMap != null) {
                for (Country country : countryList) {
                    List<String> words = countrySearchMap.get(country);
                    if (words != null) {
                        for (String key : words) {
                            if (key.toLowerCase().startsWith(q)) {
                                resultArray.add(country);
                                break;
                            }
                        }
                    }
                }
            }
            if (callback != null) {
                callback.onResult(resultArray);
            }
        });
    }
}
