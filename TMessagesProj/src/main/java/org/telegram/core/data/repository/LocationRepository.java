package org.telegram.core.data.repository;

import android.location.Location;
import org.telegram.messenger.LocationController;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
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

    LocationController.SharingLocationInfo getSharingLocationInfo(long dialogId);
    boolean isSharingLocation(long dialogId);
    void removeSharingLocation(long dialogId);
    void setProximityLocation(long dialogId, int radius, boolean notify);
    void setMapLocation(Location location, boolean isFirstLocation);
    ArrayList<TLRPC.Message> getLocationsCache(long dialogId);
    void putLocationsCache(long dialogId, ArrayList<TLRPC.Message> messages);
    void markLiveLocationsAsRead(long dialogId);

    TLRPC.User getUser(long userId);
    TLRPC.Chat getChat(long chatId);
    void putUsers(ArrayList<TLRPC.User> users, boolean fromCache);
    void putChats(ArrayList<TLRPC.Chat> chats, boolean fromCache);
    TLRPC.InputChannel getInputChannel(long chatId);
    TLRPC.InputPeer getInputPeer(long peerId);

    int getCurrentTime();
    int sendRequest(TLObject req, RequestDelegate callback);
    void bindRequestToGuid(int reqId, int classGuid);

    interface LocatedCallback {
        void onSuccess(TLRPC.TL_updates updates, int share);
        void onError(TLRPC.TL_error error, int share);
    }

    interface AdminedChannelsCallback {
        void onResult(boolean canCreate);
    }
}
