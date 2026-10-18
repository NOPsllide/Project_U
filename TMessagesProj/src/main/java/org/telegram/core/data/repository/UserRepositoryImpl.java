package org.telegram.core.data.repository;

import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;

public class UserRepositoryImpl implements UserRepository {

    private final int accountNum;

    public UserRepositoryImpl(int accountNum) {
        this.accountNum = accountNum;
    }

    @Override
    public TLRPC.UserFull getCurrentUserFull() {
        long userId = UserConfig.getInstance(accountNum).getClientUserId();
        return MessagesController.getInstance(accountNum).getUserFull(userId);
    }

    @Override
    public String getCurrentBio() {
        TLRPC.UserFull userFull = getCurrentUserFull();
        if (userFull != null && userFull.about != null) {
            return userFull.about;
        }
        return "";
    }

    @Override
    public int getAboutLimit() {
        return MessagesController.getInstance(accountNum).getAboutLimit();
    }

    @Override
    public int updateBio(String newBio, Callback<TLRPC.User> callback) {
        final TLRPC.UserFull userFull = getCurrentUserFull();

        final TLRPC.TL_account_updateProfile req = new TLRPC.TL_account_updateProfile();
        req.about = newBio;
        req.flags |= 4;

        return ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> {
            if (error == null) {
                final TLRPC.User user = (TLRPC.User) response;
                AndroidUtilities.runOnUIThread(() -> {
                    if (userFull != null) {
                        userFull.about = newBio;
                        NotificationCenter.getInstance(accountNum)
                                .postNotificationName(NotificationCenter.userInfoDidLoad, user.id, userFull);
                    }
                    callback.onSuccess(user);
                });
            } else {
                AndroidUtilities.runOnUIThread(() -> callback.onError(error));
            }
        }, ConnectionsManager.RequestFlagFailOnServerErrors);
    }

    @Override
    public void cancelRequest(int requestId) {
        ConnectionsManager.getInstance(accountNum).cancelRequest(requestId, true);
    }
}
