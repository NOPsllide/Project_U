package org.telegram.core.data.repository;

import android.location.Location;
import android.os.Bundle;

import org.telegram.messenger.LocationController;

public interface ActionIntroRepository {

    boolean isLocationEnabled();

    void fetchLocationAddress(Location location, LocationController.LocationFetchCallback callback);

    String getCurrentUserPhone(int currentAccount);

    Bundle buildChannelCreateArgs();

    Bundle buildGroupCreateArgs(long userId, String address, Location location);
}
