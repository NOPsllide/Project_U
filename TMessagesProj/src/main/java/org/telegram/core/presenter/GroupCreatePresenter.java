package org.telegram.core.presenter;

import org.telegram.core.data.repository.ChatRepository;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DialogObject;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GroupCreatePresenter {

    public interface GroupCreateView {
        void onContactsDidLoad();
        void onUpdateInterfaces(int mask);
        void onChatDidCreated();
    }

    private final ChatRepository chatRepository;
    private final int currentAccount;
    private GroupCreateView view;

    public GroupCreatePresenter(ChatRepository chatRepository, int currentAccount) {
        this.chatRepository = chatRepository;
        this.currentAccount = currentAccount;
    }

    public void attachView(GroupCreateView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public ArrayList<TLObject> loadInitialContacts(boolean isNeverOrAlwaysShare) {
        ArrayList<TLObject> contacts = new ArrayList<>();
        ArrayList<TLRPC.TL_contact> arrayList = chatRepository.getContacts();
        if (arrayList != null) {
            for (int a = 0; a < arrayList.size(); a++) {
                TLRPC.User user = chatRepository.getUser(arrayList.get(a).user_id);
                if (user == null || user.self || user.deleted) {
                    continue;
                }
                contacts.add(user);
            }
        }
        if (isNeverOrAlwaysShare) {
            ArrayList<TLRPC.Dialog> dialogs = chatRepository.getAllDialogs();
            if (dialogs != null) {
                for (int a = 0, N = dialogs.size(); a < N; a++) {
                    TLRPC.Dialog dialog = dialogs.get(a);
                    if (!DialogObject.isChatDialog(dialog.id)) {
                        continue;
                    }
                    TLRPC.Chat chat = chatRepository.getChat(-dialog.id);
                    if (chat == null || chat.migrated_to != null || (ChatObject.isChannel(chat) && !chat.megagroup)) {
                        continue;
                    }
                    contacts.add(chat);
                }
            }
            Collections.sort(contacts, new Comparator<TLObject>() {
                private String getName(TLObject object) {
                    if (object instanceof TLRPC.User) {
                        TLRPC.User user = (TLRPC.User) object;
                        return ContactsController.formatName(user.first_name, user.last_name);
                    } else {
                        TLRPC.Chat chat = (TLRPC.Chat) object;
                        return chat.title;
                    }
                }

                @Override
                public int compare(TLObject o1, TLObject o2) {
                    return getName(o1).compareTo(getName(o2));
                }
            });
        }
        return contacts;
    }

    public TLRPC.User getUser(long userId) {
        return chatRepository.getUser(userId);
    }

    public TLRPC.Chat getChat(long chatId) {
        return chatRepository.getChat(chatId);
    }

    public void putUser(TLRPC.User user, boolean inCache) {
        chatRepository.putUser(user, inCache);
    }

    public void putChat(TLRPC.Chat chat, boolean inCache) {
        chatRepository.putChat(chat, inCache);
    }

    public TLRPC.InputUser getInputUser(TLRPC.User user) {
        return chatRepository.getInputUser(user);
    }

    public void addUsersToChannel(long chatId, ArrayList<TLRPC.InputUser> users) {
        chatRepository.addUsersToChannel(chatId, users);
    }

    public int getMaxGroupCount() {
        return chatRepository.getMaxGroupCount();
    }

    public int getMaxMegagroupCount() {
        return chatRepository.getMaxMegagroupCount();
    }

    public int getMaxBroadcastCount() {
        return chatRepository.getMaxBroadcastCount();
    }

    public void onContactsDidLoad() {
        if (view != null) {
            view.onContactsDidLoad();
        }
    }

    public void onUpdateInterfaces(int mask) {
        if (view != null) {
            view.onUpdateInterfaces(mask);
        }
    }

    public void onChatDidCreated() {
        if (view != null) {
            view.onChatDidCreated();
        }
    }
}
