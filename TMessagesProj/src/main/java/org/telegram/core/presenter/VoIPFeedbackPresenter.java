package org.telegram.core.presenter;

import android.app.Activity;

import org.telegram.core.data.repository.VoIPRepository;
import org.telegram.core.data.repository.VoIPRepositoryImpl;

public class VoIPFeedbackPresenter {

    public interface VoIPFeedbackView {
        void finishActivity();
    }

    private final VoIPRepository repository;
    private VoIPFeedbackView view;

    public VoIPFeedbackPresenter() {
        this(new VoIPRepositoryImpl());
    }

    public VoIPFeedbackPresenter(VoIPRepository repository) {
        this.repository = repository;
    }

    public void attachView(VoIPFeedbackView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void showRateAlert(Activity activity, boolean callVideo, long callId, long callAccessHash, int account) {
        repository.showRateAlert(activity, () -> {
            if (view != null) {
                view.finishActivity();
            }
        }, callVideo, callId, callAccessHash, account);
    }
}
