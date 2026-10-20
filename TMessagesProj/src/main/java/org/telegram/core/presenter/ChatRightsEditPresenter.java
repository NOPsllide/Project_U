package org.telegram.core.presenter;

import android.app.Activity;
import org.telegram.core.data.repository.ChatRepository;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;

public class ChatRightsEditPresenter {

    public interface ChatRightsEditView {
        void onRoleUpdated(boolean success);
    }

    private final ChatRepository chatRepository;
    private final int currentAccount;
    private ChatRightsEditView view;

    public ChatRightsEditPresenter(ChatRepository chatRepository, int currentAccount) {
        this.chatRepository = chatRepository;
        this.currentAccount = currentAccount;
    }

    public void attachView(ChatRightsEditView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public TLRPC.User getUser(long userId) {
        return chatRepository.getUser(userId);
    }

    public TLRPC.Chat getChat(long chatId) {
        return chatRepository.getChat(chatId);
    }

    public TLRPC.UserFull getUserFull(long userId) {
        return chatRepository.getUserFull(userId);
    }

    public int getCurrentTime() {
        return chatRepository.getCurrentTime();
    }

    public void convertToMegaGroup(Activity activity, long chatId, BaseFragment fragment, MessagesStorage.LongCallback callback) {
        chatRepository.convertToMegaGroup(activity, chatId, fragment, callback);
    }

    public void setUserAdminRole(long chatId, TLRPC.User user, TLRPC.TL_chatAdminRights adminRights, String rank, boolean isChannel, BaseFragment fragment, boolean isAddingNew, boolean asAdmin, String botHash, Runnable onFinish) {
        chatRepository.setUserAdminRole(chatId, user, adminRights, rank, isChannel, fragment, isAddingNew, asAdmin, botHash, onFinish);
    }

    public void setUserAdminRole(long chatId, TLRPC.User user, TLRPC.TL_chatAdminRights adminRights, String rank, boolean isChannel, BaseFragment fragment, boolean isAddingNew, boolean asAdmin, String botHash, Runnable onFinish, MessagesController.ErrorDelegate onError) {
        chatRepository.setUserAdminRole(chatId, user, adminRights, rank, isChannel, fragment, isAddingNew, asAdmin, botHash, onFinish, onError);
    }

    public void setParticipantBannedRole(long chatId, TLRPC.User user, TLRPC.Chat chat, TLRPC.TL_chatBannedRights rights, boolean isChannel, BaseFragment fragment) {
        chatRepository.setParticipantBannedRole(chatId, user, chat, rights, isChannel, fragment);
    }

    public void addUserToChat(long chatId, TLRPC.User user, int fwdCount, String botHash, BaseFragment fragment, boolean check, Runnable onFinish, MessagesController.ErrorDelegate onError) {
        chatRepository.addUserToChat(chatId, user, fwdCount, botHash, fragment, check, onFinish, onError);
    }
}
