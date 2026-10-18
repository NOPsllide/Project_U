package org.telegram.core.presenter;

import org.telegram.core.data.repository.NotificationsRepository;
import org.telegram.core.data.repository.NotificationsRepositoryImpl;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

public class NotificationsSoundPresenter {

    public interface NotificationsSoundView {
        void onTonesLoaded();
        void onSoundSaved();
    }

    private NotificationsSoundView view;
    private final NotificationsRepository repository;

    public NotificationsSoundPresenter(int currentAccount) {
        this(new NotificationsRepositoryImpl(currentAccount));
    }

    public NotificationsSoundPresenter(NotificationsRepository repository) {
        this.repository = repository;
    }

    public void attachView(NotificationsSoundView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public long getSoundDocumentId(long dialogId, int topicId, int currentType) {
        return repository.getSoundDocumentId(dialogId, topicId, currentType);
    }

    public String getSoundPath(long dialogId, int topicId, int currentType) {
        return repository.getSoundPath(dialogId, topicId, currentType);
    }

    public void saveSound(long dialogId, int topicId, int currentType, boolean fromServer, TLRPC.Document document, String title, String uri, boolean isSystemDefault) {
        repository.saveSound(dialogId, topicId, currentType, fromServer, document, title, uri, isSystemDefault);
        if (view != null) {
            view.onSoundSaved();
        }
    }

    public void deleteServerTones(ArrayList<TLRPC.Document> documents) {
        repository.deleteServerTones(documents);
    }

    public void uploadRingtones(ArrayList<String> files) {
        if (files == null) {
            return;
        }
        for (int i = 0; i < files.size(); i++) {
            repository.uploadRingtone(files.get(i));
        }
    }
}
