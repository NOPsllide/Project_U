package org.telegram.core.data.repository;

import org.telegram.tgnet.TLRPC;
import java.util.ArrayList;

public interface UserRepository {

    interface Callback<T> {
        void onSuccess(T result);
        void onError(TLRPC.TL_error error);
    }

    TLRPC.UserFull getCurrentUserFull();

    String getCurrentBio();

    int getAboutLimit();

    int updateBio(String newBio, Callback<TLRPC.User> callback);

    TLRPC.User getCurrentUser();

    void updateName(String firstName, String lastName);

    void cancelRequest(int requestId);

    int checkUsername(String username, Callback<Boolean> callback);

    int updateUsername(String username, int classGuid, Callback<TLRPC.User> callback);

    void reorderUsernames(ArrayList<String> order);

    int toggleUsername(String username, boolean active, Callback<Boolean> callback);

    void updateUsernames(ArrayList<TLRPC.TL_username> usernames);
}
