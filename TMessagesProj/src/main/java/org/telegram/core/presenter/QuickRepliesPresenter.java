package org.telegram.core.presenter;

import org.telegram.core.data.repository.SettingsRepository;

public class QuickRepliesPresenter {

    public interface View {
        void notifyDataSetChanged();
    }

    private final SettingsRepository settingsRepository;
    private View view;

    public QuickRepliesPresenter(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public void attachView(View view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public String getQuickReply(int index) {
        return settingsRepository.getQuickReply(index);
    }

    public void saveQuickReply(int index, String text) {
        settingsRepository.saveQuickReply(index, text);
    }

    public boolean getAllowCustomQuickReply() {
        return settingsRepository.getAllowCustomQuickReply();
    }

    public void setAllowCustomQuickReply(boolean allow) {
        settingsRepository.setAllowCustomQuickReply(allow);
    }

    public void saveAllQuickReplies(String[] replies) {
        for (int i = 0; i < replies.length; i++) {
            if (replies[i] != null) {
                saveQuickReply(i + 1, replies[i]);
            }
        }
    }
}
