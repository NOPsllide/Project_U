package org.telegram.core.presenter;

import org.telegram.core.data.repository.PrivacyRepository;
import org.telegram.core.data.repository.PrivacyRepositoryImpl;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

public class PrivacyControlPresenter {

    public interface PrivacyControlView {
        void onPrivacySettingsApplied();
        void onPrivacySettingsError();
    }

    private PrivacyControlView view;
    private final PrivacyRepository repository;

    public PrivacyControlPresenter(int currentAccount) {
        this(new PrivacyRepositoryImpl(currentAccount));
    }

    public PrivacyControlPresenter(PrivacyRepository repository) {
        this.repository = repository;
    }

    public void attachView(PrivacyControlView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void loadPrivacySettings() {
        repository.loadPrivacySettings();
    }

    public ArrayList<TLRPC.PrivacyRule> getPrivacyRules(int rulesType) {
        return repository.getPrivacyRules(rulesType);
    }

    public void applyPrivacyRules(TLRPC.TL_account_setPrivacy req, int rulesType, int currentSubType, boolean isPhoneNobody) {
        if (isPhoneNobody) {
            repository.setPhoneAddedPrivacyRules(currentSubType);
        }
        repository.setPrivacyRules(req, rulesType, (success, error) -> {
            if (view != null) {
                if (success) {
                    view.onPrivacySettingsApplied();
                } else {
                    view.onPrivacySettingsError();
                }
            }
        });
    }
}
