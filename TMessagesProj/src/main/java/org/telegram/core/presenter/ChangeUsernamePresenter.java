package org.telegram.core.presenter;

import org.telegram.core.data.repository.UserRepository;
import org.telegram.core.data.repository.UserRepositoryImpl;
import org.telegram.tgnet.TLRPC;
import java.util.ArrayList;

public class ChangeUsernamePresenter {

    public interface ChangeUsernameView {
        void onUsernameCheckResult(String username, boolean available, String error);
        void onUsernameUpdated(TLRPC.User user);
        void onUsernameUpdateError(TLRPC.TL_error error);
        void onUsernameToggleResult(String username, boolean active, boolean success, String error);
    }

    private ChangeUsernameView view;
    private final UserRepository repository;

    public ChangeUsernamePresenter(int currentAccount) {
        this(new UserRepositoryImpl(currentAccount));
    }

    public ChangeUsernamePresenter(UserRepository repository) {
        this.repository = repository;
    }

    public void attachView(ChangeUsernameView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public TLRRPC_UserWrapper getCurrentUser() {
        TLRPC.User user = repository.getCurrentUser();
        return new TLRRPC_UserWrapper(user);
    }

    public static class TLRRPC_UserWrapper {
        public final TLRPC.User user;
        public TLRRPC_UserWrapper(TLRPC.User user) {
            this.user = user;
        }
    }

    public int checkUsername(String username) {
        return repository.checkUsername(username, new UserRepository.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean available) {
                if (view != null) {
                    view.onUsernameCheckResult(username, available, null);
                }
            }

            @Override
            public void onError(TLRPC.TL_error error) {
                if (view != null) {
                    view.onUsernameCheckResult(username, false, error != null ? error.text : null);
                }
            }
        });
    }

    public int updateUsername(String username, int classGuid) {
        return repository.updateUsername(username, classGuid, new UserRepository.Callback<TLRPC.User>() {
            @Override
            public void onSuccess(TLRPC.User user) {
                if (view != null) {
                    view.onUsernameUpdated(user);
                }
            }

            @Override
            public void onError(TLRPC.TL_error error) {
                if (view != null) {
                    view.onUsernameUpdateError(error);
                }
            }
        });
    }

    public void cancelRequest(int requestId) {
        repository.cancelRequest(requestId);
    }

    public void reorderUsernames(ArrayList<String> order) {
        repository.reorderUsernames(order);
    }

    public int toggleUsername(String username, boolean active) {
        return repository.toggleUsername(username, active, new UserRepository.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (view != null) {
                    view.onUsernameToggleResult(username, active, true, null);
                }
            }

            @Override
            public void onError(TLRPC.TL_error error) {
                if (view != null) {
                    view.onUsernameToggleResult(username, active, false, error != null ? error.text : null);
                }
            }
        });
    }

    public void updateUsernames(ArrayList<TLRPC.TL_username> usernames) {
        repository.updateUsernames(usernames);
    }
}
