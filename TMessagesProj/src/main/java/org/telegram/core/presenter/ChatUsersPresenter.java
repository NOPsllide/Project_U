package org.telegram.core.presenter;

import android.app.Activity;
import org.telegram.core.data.repository.ChatRepository;
import org.telegram.core.data.repository.ChatRepositoryImpl;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;

public class ChatUsersPresenter {

    public interface ChatUsersView {
    }

    private final ChatRepository repository;
    private final int currentAccount;
    private ChatUsersView view;

    public ChatUsersPresenter(int currentAccount) {
        this(new ChatRepositoryImpl(currentAccount), currentAccount);
    }

    public ChatUsersPresenter(ChatRepository repository, int currentAccount) {
        this.repository = repository;
        this.currentAccount = currentAccount;
    }

    public void attachView(ChatUsersView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public TLRPC.Chat getChat(long chatId) {
        return repository.getChat(chatId);
    }

    public TLRPC.User getUser(long userId) {
        return repository.getUser(userId);
    }

    public TLRPC.ChatFull getChatFull(long chatId) {
        return repository.getChatFull(chatId);
    }

    public void loadFullChat(long chatId, int classGuid, boolean force) {
        repository.loadFullChat(chatId, classGuid, force);
    }

    public int getCurrentTime() {
        return repository.getCurrentTime();
    }

    public void convertToMegaGroup(Activity activity, long chatId, BaseFragment fragment, MessagesStorage.LongCallback callback) {
        repository.convertToMegaGroup(activity, chatId, fragment, callback);
    }

    public void convertToGigaGroup(Activity activity, TLRPC.Chat chat, BaseFragment fragment, MessagesStorage.BooleanCallback callback) {
        repository.convertToGigaGroup(activity, chat, fragment, callback);
    }

    public void setDefaultBannedRole(long chatId, TLRPC.TL_chatBannedRights rights, boolean isChannel, BaseFragment fragment) {
        repository.setDefaultBannedRole(chatId, rights, isChannel, fragment);
    }

    public void setChannelSlowMode(long chatId, int slowmode) {
        repository.setChannelSlowMode(chatId, slowmode);
    }

    public void setUserAdminRole(long chatId, TLRPC.User user, TLRPC.TL_chatAdminRights adminRights, String rank, boolean isChannel, BaseFragment fragment, boolean isAddingNew, boolean asAdmin, String botHash, Runnable onFinish) {
        repository.setUserAdminRole(chatId, user, adminRights, rank, isChannel, fragment, isAddingNew, asAdmin, botHash, onFinish);
    }

    public void setUserAdminRole(long chatId, TLRPC.User user, TLRPC.TL_chatAdminRights adminRights, String rank, boolean isChannel, BaseFragment fragment, boolean isAddingNew, boolean asAdmin, String botHash, Runnable onFinish, MessagesController.ErrorDelegate onError) {
        repository.setUserAdminRole(chatId, user, adminRights, rank, isChannel, fragment, isAddingNew, asAdmin, botHash, onFinish, onError);
    }

    public void setParticipantBannedRole(long chatId, TLRPC.User user, TLRPC.TL_chatBannedRights rights, TLRPC.TL_chatBannedRights bannedRights, boolean isChannel, BaseFragment fragment) {
        repository.setParticipantBannedRole(chatId, user, rights, bannedRights, isChannel, fragment);
    }

    public void addUserToChat(long chatId, TLRPC.User user, int fwdCount, String botHash, BaseFragment fragment, boolean check, Runnable onFinish, MessagesController.ErrorDelegate onError) {
        repository.addUserToChat(chatId, user, fwdCount, botHash, fragment, check, onFinish, onError);
    }

    public void deleteParticipantFromChat(long chatId, TLRPC.User user) {
        repository.deleteParticipantFromChat(chatId, user);
    }

    public int sendRequest(TLObject request, RequestDelegate delegate) {
        return repository.sendRequest(request, delegate);
    }

    public void bindRequestToGuid(int reqId, int classGuid) {
        repository.bindRequestToGuid(reqId, classGuid);
    }

    public void cancelRequest(int reqId) {
        repository.cancelRequest(reqId);
    }

    public void putUser(TLRPC.User user, boolean inCache) {
        repository.putUser(user, inCache);
    }

    public void putUsers(ArrayList<TLRPC.User> users, boolean fromCache) {
        repository.putUsers(users, fromCache);
    }

    public void putChats(ArrayList<TLRPC.Chat> chats, boolean fromCache) {
        repository.putChats(chats, fromCache);
    }

    public void processUpdates(TLRPC.Updates updates, boolean isChannel) {
        repository.processUpdates(updates, isChannel);
    }

    public TLRPC.InputPeer getInputPeer(long peerId) {
        return repository.getInputPeer(peerId);
    }

    public TLRPC.InputChannel getInputChannel(long chatId) {
        return repository.getInputChannel(chatId);
    }

    public void processLoadedAdminsResponse(long chatId, ArrayList<TLRPC.ChatParticipant> participants) {
        repository.processLoadedAdminsResponse(chatId, participants);
    }
}
