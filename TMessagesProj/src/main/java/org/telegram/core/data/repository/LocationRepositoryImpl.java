package org.telegram.core.data.repository;

import android.location.Location;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocationController;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

public class LocationRepositoryImpl implements LocationRepository {

    private final int currentAccount;

    public LocationRepositoryImpl(int currentAccount) {
        this.currentAccount = currentAccount;
    }

    @Override
    public Location getLastKnownLocation() {
        return LocationController.getInstance(currentAccount).getLastKnownLocation();
    }

    @Override
    public ArrayList<TLRPC.TL_peerLocated> getCachedNearbyUsers() {
        return LocationController.getInstance(currentAccount).getCachedNearbyUsers();
    }

    @Override
    public ArrayList<TLRPC.TL_peerLocated> getCachedNearbyChats() {
        return LocationController.getInstance(currentAccount).getCachedNearbyChats();
    }

    @Override
    public void setCachedNearbyUsersAndChats(ArrayList<TLRPC.TL_peerLocated> users, ArrayList<TLRPC.TL_peerLocated> chats) {
        LocationController.getInstance(currentAccount).setCachedNearbyUsersAndChats(users, chats);
    }

    @Override
    public void startLocationLookupForPeopleNearby(boolean stop) {
        LocationController.getInstance(currentAccount).startLocationLookupForPeopleNearby(stop);
    }

    @Override
    public void fetchLocationAddress(Location location, LocationController.LocationFetchCallback callback) {
        LocationController.fetchLocationAddress(location, callback);
    }

    @Override
    public int sendGetLocatedRequest(Location location, int share, int classGuid, LocatedCallback callback) {
        TLRPC.TL_contacts_getLocated req = new TLRPC.TL_contacts_getLocated();
        req.geo_point = new TLRPC.TL_inputGeoPoint();
        req.geo_point.lat = location.getLatitude();
        req.geo_point._long = location.getLongitude();
        if (share != 0) {
            req.flags |= 1;
            req.self_expires = share == 1 ? 0x7fffffff : 0;
        }
        int reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (response instanceof TLRPC.TL_updates) {
                TLRPC.TL_updates updates = (TLRPC.TL_updates) response;
                MessagesController.getInstance(currentAccount).putUsers(updates.users, false);
                MessagesController.getInstance(currentAccount).putChats(updates.chats, false);
                if (callback != null) {
                    callback.onSuccess(updates, share);
                }
            } else {
                if (callback != null) {
                    callback.onError(error, share);
                }
            }
        }));
        if (classGuid != 0) {
            ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(reqId, classGuid);
        }
        return reqId;
    }

    @Override
    public int checkAdminedPublicChannels(int classGuid, AdminedChannelsCallback callback) {
        TLRPC.TL_channels_getAdminedPublicChannels req = new TLRPC.TL_channels_getAdminedPublicChannels();
        req.by_location = true;
        req.check_limit = true;
        int reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (callback != null) {
                callback.onResult(error == null);
            }
        }));
        if (classGuid != 0) {
            ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(reqId, classGuid);
        }
        return reqId;
    }

    @Override
    public void cancelRequest(int reqId) {
        if (reqId != 0) {
            ConnectionsManager.getInstance(currentAccount).cancelRequest(reqId, true);
        }
    }

    @Override
    public LocationController.SharingLocationInfo getSharingLocationInfo(long dialogId) {
        return LocationController.getInstance(currentAccount).getSharingLocationInfo(dialogId);
    }

    @Override
    public boolean isSharingLocation(long dialogId) {
        return LocationController.getInstance(currentAccount).isSharingLocation(dialogId);
    }

    @Override
    public void removeSharingLocation(long dialogId) {
        LocationController.getInstance(currentAccount).removeSharingLocation(dialogId);
    }

    @Override
    public void setProximityLocation(long dialogId, int radius, boolean notify) {
        LocationController.getInstance(currentAccount).setProximityLocation(dialogId, radius, notify);
    }

    @Override
    public void setMapLocation(Location location, boolean isFirstLocation) {
        LocationController.getInstance(currentAccount).setMapLocation(location, isFirstLocation);
    }

    @Override
    public ArrayList<TLRPC.Message> getLocationsCache(long dialogId) {
        return LocationController.getInstance(currentAccount).locationsCache.get(dialogId);
    }

    @Override
    public void putLocationsCache(long dialogId, ArrayList<TLRPC.Message> messages) {
        LocationController.getInstance(currentAccount).locationsCache.put(dialogId, messages);
    }

    @Override
    public void markLiveLocationsAsRead(long dialogId) {
        LocationController.getInstance(currentAccount).markLiveLoactionsAsRead(dialogId);
    }

    @Override
    public TLRPC.User getUser(long userId) {
        return MessagesController.getInstance(currentAccount).getUser(userId);
    }

    @Override
    public TLRPC.Chat getChat(long chatId) {
        return MessagesController.getInstance(currentAccount).getChat(chatId);
    }

    @Override
    public void putUsers(ArrayList<TLRPC.User> users, boolean fromCache) {
        MessagesController.getInstance(currentAccount).putUsers(users, fromCache);
    }

    @Override
    public void putChats(ArrayList<TLRPC.Chat> chats, boolean fromCache) {
        MessagesController.getInstance(currentAccount).putChats(chats, fromCache);
    }

    @Override
    public TLRPC.InputChannel getInputChannel(long chatId) {
        return MessagesController.getInstance(currentAccount).getInputChannel(chatId);
    }

    @Override
    public TLRPC.InputPeer getInputPeer(long peerId) {
        return MessagesController.getInstance(currentAccount).getInputPeer(peerId);
    }

    @Override
    public int getCurrentTime() {
        return ConnectionsManager.getInstance(currentAccount).getCurrentTime();
    }

    @Override
    public int sendRequest(TLObject req, RequestDelegate callback) {
        return ConnectionsManager.getInstance(currentAccount).sendRequest(req, callback);
    }

    @Override
    public void bindRequestToGuid(int reqId, int classGuid) {
        ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(reqId, classGuid);
    }
}
