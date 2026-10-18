package org.telegram.core.presenter;

import org.telegram.core.data.repository.ChatRepository;
import org.telegram.core.data.repository.ChatRepositoryImpl;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;

public class GroupCreateFinalPresenter {

    public interface GroupCreateFinalView {
        void onChatCreated(long chatId);
        void onChatCreateFailed();
    }

    private GroupCreateFinalView view;
    private final ChatRepository repository;

    public GroupCreateFinalPresenter(int currentAccount) {
        this(new ChatRepositoryImpl(currentAccount));
    }

    public GroupCreateFinalPresenter(ChatRepository repository) {
        this.repository = repository;
    }

    public void attachView(GroupCreateFinalView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public boolean loadMissingUsers(ArrayList<Long> selectedContacts) {
        return repository.loadMissingUsers(selectedContacts);
    }

    public int createChat(String title, ArrayList<Long> selectedContacts, int chatType, boolean forImport, TLRPC.InputGeoPoint location, String address, BaseFragment fragment) {
        return repository.createChat(title, selectedContacts, null, chatType, forImport, location, address, fragment);
    }

    public void changeChatAvatar(long chatId, TLRPC.InputFile inputPhoto, TLRPC.InputFile inputVideo, double videoTimestamp, String inputVideoPath, TLRPC.FileLocation avatar, TLRPC.FileLocation avatarBig) {
        repository.changeChatAvatar(chatId, inputPhoto, inputVideo, videoTimestamp, inputVideoPath, avatar, avatarBig);
    }

    public void cancelCreationRequest(int reqId) {
        if (reqId != 0) {
            repository.cancelRequest(reqId);
        }
    }

    public TLRPC.User getUser(long userId) {
        return repository.getUser(userId);
    }
}
