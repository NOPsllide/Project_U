package org.telegram.core.presenter;

import android.location.Location;
import org.telegram.core.data.repository.LocationRepository;
import org.telegram.core.data.repository.LocationRepositoryImpl;
import org.telegram.messenger.LocationController;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

public class LocationPresenter {

    public interface LocationView {
    }

    private LocationView view;
    private final LocationRepository repository;

    public LocationPresenter(int currentAccount) {
        this(new LocationRepositoryImpl(currentAccount));
    }

    public LocationPresenter(LocationRepository repository) {
        this.repository = repository;
    }

    public void attachView(LocationView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public Location getLastKnownLocation() {
        return repository.getLastKnownLocation();
    }

    public LocationController.SharingLocationInfo getSharingLocationInfo(long dialogId) {
        return repository.getSharingLocationInfo(dialogId);
    }

    public boolean isSharingLocation(long dialogId) {
        return repository.isSharingLocation(dialogId);
    }

    public void removeSharingLocation(long dialogId) {
        repository.removeSharingLocation(dialogId);
    }

    public void setProximityLocation(long dialogId, int radius, boolean notify) {
        repository.setProximityLocation(dialogId, radius, notify);
    }

    public void setMapLocation(Location location, boolean isFirstLocation) {
        repository.setMapLocation(location, isFirstLocation);
    }

    public ArrayList<TLRPC.Message> getLocationsCache(long dialogId) {
        return repository.getLocationsCache(dialogId);
    }

    public void putLocationsCache(long dialogId, ArrayList<TLRPC.Message> messages) {
        repository.putLocationsCache(dialogId, messages);
    }

    public void markLiveLocationsAsRead(long dialogId) {
        repository.markLiveLocationsAsRead(dialogId);
    }

    public TLRPC.User getUser(long userId) {
        return repository.getUser(userId);
    }

    public TLRPC.Chat getChat(long chatId) {
        return repository.getChat(chatId);
    }

    public void putUsers(ArrayList<TLRPC.User> users, boolean fromCache) {
        repository.putUsers(users, fromCache);
    }

    public void putChats(ArrayList<TLRPC.Chat> chats, boolean fromCache) {
        repository.putChats(chats, fromCache);
    }

    public TLRPC.InputChannel getInputChannel(long chatId) {
        return repository.getInputChannel(chatId);
    }

    public TLRPC.InputPeer getInputPeer(long peerId) {
        return repository.getInputPeer(peerId);
    }

    public int getCurrentTime() {
        return repository.getCurrentTime();
    }

    public int sendRequest(TLObject req, RequestDelegate callback) {
        return repository.sendRequest(req, callback);
    }

    public void bindRequestToGuid(int reqId, int classGuid) {
        repository.bindRequestToGuid(reqId, classGuid);
    }

    public void cancelRequest(int reqId) {
        repository.cancelRequest(reqId);
    }
}
