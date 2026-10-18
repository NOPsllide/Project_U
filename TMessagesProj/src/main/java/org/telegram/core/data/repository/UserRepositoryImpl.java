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
    public TLRPC.User getCurrentUser() {
        TLRPC.User user = MessagesController.getInstance(accountNum)
                .getUser(UserConfig.getInstance(accountNum).getClientUserId());
        if (user == null) {
            user = UserConfig.getInstance(accountNum).getCurrentUser();
        }
        return user;
    }

    @Override
    public void updateName(String firstName, String lastName) {
        TLRPC.User currentUser = UserConfig.getInstance(accountNum).getCurrentUser();
        if (currentUser == null) return;

        TLRPC.TL_account_updateProfile req = new TLRPC.TL_account_updateProfile();
        req.flags = 3;
        currentUser.first_name = req.first_name = firstName;
        currentUser.last_name = req.last_name = lastName;

        TLRPC.User cachedUser = MessagesController.getInstance(accountNum)
                .getUser(UserConfig.getInstance(accountNum).getClientUserId());
        if (cachedUser != null) {
            cachedUser.first_name = firstName;
            cachedUser.last_name = lastName;
        }

        UserConfig.getInstance(accountNum).saveConfig(true);
        NotificationCenter.getInstance(accountNum)
                .postNotificationName(NotificationCenter.mainUserInfoChanged);
        NotificationCenter.getInstance(accountNum)
                .postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_NAME);

        ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> {});
    }

    @Override
    public void cancelRequest(int requestId) {
        ConnectionsManager.getInstance(accountNum).cancelRequest(requestId, true);
    }
}
