package org.telegram.core.presenter;

import org.telegram.core.data.repository.UserRepository;
import org.telegram.tgnet.TLRPC;

public class ChangeNamePresenter {

    public interface View {
        void showCurrentName(String firstName, String lastName);
        void closeScreen();
    }

    private final UserRepository userRepository;
    private View view;

    public ChangeNamePresenter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void attachView(View view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void loadCurrentName() {
        if (view == null) return;
        TLRPC.User user = userRepository.getCurrentUser();
        if (user != null) {
            view.showCurrentName(
                    user.first_name != null ? user.first_name : "",
                    user.last_name != null ? user.last_name : ""
            );
        }
    }

    public void saveName(String firstName, String lastName) {
        if (view == null) return;
        if (firstName.isEmpty()) return;

        TLRPC.User user = userRepository.getCurrentUser();
        if (user == null) return;

        boolean firstSame = user.first_name != null && user.first_name.equals(firstName);
        boolean lastSame = user.last_name != null && user.last_name.equals(lastName);

        if (firstSame && lastSame) {
            view.closeScreen();
            return;
        }

        userRepository.updateName(firstName, lastName);
        view.closeScreen();
    }
}
