package org.telegram.core.data.repository;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import java.util.ArrayList;

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

    @Override
    public int checkUsername(String username, Callback<Boolean> callback) {
        TLRPC.TL_account_checkUsername req = new TLRPC.TL_account_checkUsername();
        req.username = username;
        return ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (callback != null) {
                if (error == null && response instanceof TLRPC.TL_boolTrue) {
                    callback.onSuccess(true);
                } else {
                    callback.onError(error);
                }
            }
        }), ConnectionsManager.RequestFlagFailOnServerErrors);
    }

    @Override
    public int updateUsername(String username, int classGuid, Callback<TLRPC.User> callback) {
        TLRPC.TL_account_updateUsername req = new TLRPC.TL_account_updateUsername();
        req.username = username;
        NotificationCenter.getInstance(accountNum).postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_NAME);
        int reqId = ConnectionsManager.getInstance(accountNum).sendRequest(req, (response, error) -> {
            if (error == null) {
                TLRPC.User user = (TLRPC.User) response;
                AndroidUtilities.runOnUIThread(() -> {
                    ArrayList<TLRPC.User> users = new ArrayList<>();
                    users.add(user);
                    MessagesController.getInstance(accountNum).putUsers(users, false);
                    MessagesStorage.getInstance(accountNum).putUsersAndChats(users, null, false, true);
                    UserConfig.getInstance(accountNum).saveConfig(true);
                    if (callback != null) {
                        callback.onSuccess(user);
                    }
                });
            } else {
                AndroidUtilities.runOnUIThread(() -> {
                    if (callback != null) {
                        callback.onError(error);
                    }
                });
            }
        }, ConnectionsManager.RequestFlagFailOnServerErrors);
        ConnectionsManager.getInstance(accountNum).bindRequestToGuid(reqId, classGuid);
        return reqId;
    }

    @Override
    public void reorderUsernames(ArrayList<String> order) {
        TLRPC.TL_account_reorderUsernames req = new TLRPC.TL_account_reorderUsernames();
        req.order = order;
        ConnectionsManager.getInstance(accountNum).sendRequest(req, (res, err) -> {});
    }

    @Override
    public int toggleUsername(String username, boolean active, Callback<Boolean> callback) {
        TLRPC.TL_account_toggleUsername req = new TLRPC.TL_account_toggleUsername();
        req.username = username;
        req.active = active;
        return ConnectionsManager.getInstance(accountNum).sendRequest(req, (res, err) -> {
            if (res instanceof TLRPC.TL_boolTrue) {
                if (callback != null) {
                    AndroidUtilities.runOnUIThread(() -> callback.onSuccess(true));
                }
            } else {
                if (callback != null) {
                    AndroidUtilities.runOnUIThread(() -> callback.onError(err));
                }
            }
        }, ConnectionsManager.RequestFlagDoNotWaitFloodWait);
    }

    @Override
    public void updateUsernames(ArrayList<TLRPC.TL_username> usernames) {
        TLRPC.User user = MessagesController.getInstance(accountNum).getUser(UserConfig.getInstance(accountNum).getClientUserId());
        if (user != null) {
            user.usernames = usernames;
            MessagesController.getInstance(accountNum).putUser(user, false, true);
        }
    }
}
