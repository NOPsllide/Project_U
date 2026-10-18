package org.telegram.core.data.repository;

import android.location.Location;
import org.telegram.messenger.LocationController;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

public interface LocationRepository {
    Location getLastKnownLocation();
    ArrayList<TLRPC.TL_peerLocated> getCachedNearbyUsers();
    ArrayList<TLRPC.TL_peerLocated> getCachedNearbyChats();
    void setCachedNearbyUsersAndChats(ArrayList<TLRPC.TL_peerLocated> users, ArrayList<TLRPC.TL_peerLocated> chats);
    void startLocationLookupForPeopleNearby(boolean stop);
    void fetchLocationAddress(Location location, LocationController.LocationFetchCallback callback);
    int sendGetLocatedRequest(Location location, int share, int classGuid, LocatedCallback callback);
    int checkAdminedPublicChannels(int classGuid, AdminedChannelsCallback callback);
    void cancelRequest(int reqId);

    interface LocatedCallback {
        void onSuccess(TLRPC.TL_updates updates, int share);
        void onError(TLRPC.TL_error error, int share);
    }

    interface AdminedChannelsCallback {
        void onResult(boolean canCreate);
    }
}
