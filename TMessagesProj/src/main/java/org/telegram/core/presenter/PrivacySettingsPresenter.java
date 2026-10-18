package org.telegram.core.presenter;

import org.telegram.core.data.repository.PrivacyRepository;
import org.telegram.core.data.repository.PrivacyRepositoryImpl;
import org.telegram.tgnet.TLRPC;

public class PrivacySettingsPresenter {

    public interface PrivacySettingsView {
        void onPasswordLoaded(TLRPC.account_Password password);
        void onAccountTTLSet();
    }

    private PrivacySettingsView view;
    private final PrivacyRepository repository;

    public PrivacySettingsPresenter(int currentAccount) {
        this(new PrivacyRepositoryImpl(currentAccount));
    }

    public PrivacySettingsPresenter(PrivacyRepository repository) {
        this.repository = repository;
    }

    public void attachView(PrivacySettingsView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void loadSettings() {
        repository.loadPrivacySettings();
        repository.loadBlockedPeers(true);
    }

    public TLRPC.TL_globalPrivacySettings getGlobalPrivacySettings() {
        return repository.getGlobalPrivacySettings();
    }

    public void loadPasswordSettings() {
        repository.loadPasswordSettings(password -> {
            if (view != null) {
                view.onPasswordLoaded(password);
            }
        });
    }

    public void setAccountTTL(int days) {
        repository.setAccountTTL(days, () -> {
            if (view != null) {
                view.onAccountTTLSet();
            }
        });
    }

    public void deleteAllContacts(Runnable onComplete) {
        repository.deleteAllContacts(onComplete);
    }

    public void clearPaymentInfo(boolean info, boolean credentials) {
        repository.clearPaymentInfo(info, credentials);
    }

    public void toggleTopPeers(boolean enabled) {
        repository.toggleTopPeers(enabled);
    }

    public void setGlobalPrivacySettings(boolean archiveAndMute) {
        repository.setGlobalPrivacySettings(archiveAndMute);
    }
}
