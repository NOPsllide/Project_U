package org.telegram.core.data.repository;

import org.telegram.tgnet.TLRPC;

public interface UserRepository {

    interface Callback<T> {
        void onSuccess(T result);
        void onError(TLRPC.TL_error error);
    }

    TLRPC.UserFull getCurrentUserFull();

    String getCurrentBio();

    int getAboutLimit();

    int updateBio(String newBio, Callback<TLRPC.User> callback);

    void cancelRequest(int requestId);
}
