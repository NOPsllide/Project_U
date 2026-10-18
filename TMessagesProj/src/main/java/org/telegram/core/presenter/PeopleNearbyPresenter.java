package org.telegram.core.presenter;

import android.location.Location;
import org.telegram.core.data.repository.LocationRepository;
import org.telegram.core.data.repository.LocationRepositoryImpl;
import org.telegram.messenger.LocationController;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

public class PeopleNearbyPresenter {

    public interface PeopleNearbyView {
        void onCachedDataLoaded(ArrayList<TLRPC.TL_peerLocated> users, ArrayList<TLRPC.TL_peerLocated> chats);
        void onCanCreateGroupResult(boolean canCreate);
        void onLocatedSuccess(TLRPC.TL_updates updates, int share);
        void onLocatedError(TLRPC.TL_error error, int share);
    }

    private PeopleNearbyView view;
    private final LocationRepository repository;
    private int currentReqId;
    private int checkCanCreateReqId;

    public PeopleNearbyPresenter(int currentAccount) {
        this(new LocationRepositoryImpl(currentAccount));
    }

    public PeopleNearbyPresenter(LocationRepository repository) {
        this.repository = repository;
    }

    public void attachView(PeopleNearbyView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public Location getLastKnownLocation() {
        return repository.getLastKnownLocation();
    }

    public void loadCachedData() {
        ArrayList<TLRPC.TL_peerLocated> users = repository.getCachedNearbyUsers();
        ArrayList<TLRPC.TL_peerLocated> chats = repository.getCachedNearbyChats();
        if (view != null) {
            view.onCachedDataLoaded(users != null ? new ArrayList<>(users) : new ArrayList<>(),
                    chats != null ? new ArrayList<>(chats) : new ArrayList<>());
        }
    }

    public void setCachedNearbyUsersAndChats(ArrayList<TLRPC.TL_peerLocated> users, ArrayList<TLRPC.TL_peerLocated> chats) {
        repository.setCachedNearbyUsersAndChats(users, chats);
    }

    public void startLocationLookup() {
        repository.startLocationLookupForPeopleNearby(false);
    }

    public void stopLocationLookup() {
        repository.startLocationLookupForPeopleNearby(true);
    }

    public void fetchLocationAddress(Location location, LocationController.LocationFetchCallback callback) {
        repository.fetchLocationAddress(location, callback);
    }

    public void checkCanCreateGroup(int classGuid) {
        checkCanCreateReqId = repository.checkAdminedPublicChannels(classGuid, canCreate -> {
            checkCanCreateReqId = 0;
            if (view != null) {
                view.onCanCreateGroupResult(canCreate);
            }
        });
    }

    public void sendLocatedRequest(Location location, int share, int classGuid) {
        currentReqId = repository.sendGetLocatedRequest(location, share, classGuid, new LocationRepository.LocatedCallback() {
            @Override
            public void onSuccess(TLRPC.TL_updates updates, int share) {
                currentReqId = 0;
                if (view != null) {
                    view.onLocatedSuccess(updates, share);
                }
            }

            @Override
            public void onError(TLRPC.TL_error error, int share) {
                currentReqId = 0;
                if (view != null) {
                    view.onLocatedError(error, share);
                }
            }
        });
    }

    public void cancelLocatedRequest() {
        if (currentReqId != 0) {
            repository.cancelRequest(currentReqId);
            currentReqId = 0;
        }
    }

    public boolean hasPendingRequest() {
        return currentReqId != 0;
    }
}
