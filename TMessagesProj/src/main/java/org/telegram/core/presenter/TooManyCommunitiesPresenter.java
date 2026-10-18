package org.telegram.core.presenter;

import android.text.TextUtils;

import org.telegram.core.data.repository.ChatRepository;
import org.telegram.core.data.repository.ChatRepositoryImpl;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TooManyCommunitiesPresenter {

    public interface TooManyCommunitiesView {
        void showProgress(boolean show);
        void onInactiveChatsLoaded(ArrayList<TLRPC.Chat> chats, ArrayList<String> signatures);
        void onSearchResults(ArrayList<TLRPC.Chat> chats, ArrayList<String> signatures, int searchId);
        void onSelectedCountChanged(int count);
        void finishScreen();
    }

    private final ChatRepository chatRepository;
    private TooManyCommunitiesView view;

    private final ArrayList<TLRPC.Chat> inactiveChats = new ArrayList<>();
    private final ArrayList<String> inactiveChatsSignatures = new ArrayList<>();
    private final Set<Long> selectedIds = new HashSet<>();

    private int lastSearchId;
    private Runnable searchRunnable;

    public TooManyCommunitiesPresenter(int currentAccount) {
        this(new ChatRepositoryImpl(currentAccount));
    }

    public TooManyCommunitiesPresenter(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public void attachView(TooManyCommunitiesView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
        if (searchRunnable != null) {
            Utilities.searchQueue.cancelRunnable(searchRunnable);
            searchRunnable = null;
        }
    }

    public void loadInactiveChannels() {
        if (view != null) {
            view.showProgress(true);
        }
        chatRepository.loadInactiveChannels(new ChatRepository.InactiveChannelsCallback() {
            @Override
            public void onSuccess(TLRPC.TL_messages_inactiveChats chats) {
                ArrayList<String> signatures = new ArrayList<>();
                int currentDate = chatRepository.getCurrentTime();
                for (int i = 0; i < chats.chats.size(); i++) {
                    TLRPC.Chat chat = chats.chats.get(i);
                    int date = chats.dates.get(i);
                    int daysDif = (currentDate - date) / 86400;

                    String dateFormat;
                    if (daysDif < 30) {
                        dateFormat = LocaleController.formatPluralString("Days", daysDif);
                    } else if (daysDif < 365) {
                        dateFormat = LocaleController.formatPluralString("Months", daysDif / 30);
                    } else {
                        dateFormat = LocaleController.formatPluralString("Years", daysDif / 365);
                    }
                    if (ChatObject.isMegagroup(chat)) {
                        String members = LocaleController.formatPluralString("Members", chat.participants_count);
                        signatures.add(LocaleController.formatString("InactiveChatSignature", R.string.InactiveChatSignature, members, dateFormat));
                    } else if (ChatObject.isChannel(chat)) {
                        signatures.add(LocaleController.formatString("InactiveChannelSignature", R.string.InactiveChannelSignature, dateFormat));
                    } else {
                        String members = LocaleController.formatPluralString("Members", chat.participants_count);
                        signatures.add(LocaleController.formatString("InactiveChatSignature", R.string.InactiveChatSignature, members, dateFormat));
                    }
                }

                inactiveChatsSignatures.clear();
                inactiveChats.clear();
                inactiveChatsSignatures.addAll(signatures);
                inactiveChats.addAll(chats.chats);

                if (view != null) {
                    view.showProgress(false);
                    view.onInactiveChatsLoaded(inactiveChats, inactiveChatsSignatures);
                }
            }

            @Override
            public void onError() {
                if (view != null) {
                    view.showProgress(false);
                }
            }
        });
    }

    public void toggleChatSelection(long chatId) {
        if (selectedIds.contains(chatId)) {
            selectedIds.remove(chatId);
        } else {
            selectedIds.add(chatId);
        }
        if (view != null) {
            view.onSelectedCountChanged(selectedIds.size());
        }
    }

    public boolean isChatSelected(long chatId) {
        return selectedIds.contains(chatId);
    }

    public Set<Long> getSelectedIds() {
        return selectedIds;
    }

    public int getSelectedCount() {
        return selectedIds.size();
    }

    public ArrayList<TLRPC.Chat> getInactiveChats() {
        return inactiveChats;
    }

    public ArrayList<String> getInactiveChatsSignatures() {
        return inactiveChatsSignatures;
    }

    public void search(String query) {
        if (searchRunnable != null) {
            Utilities.searchQueue.cancelRunnable(searchRunnable);
            searchRunnable = null;
        }
        if (TextUtils.isEmpty(query)) {
            if (view != null) {
                view.onSearchResults(null, null, ++lastSearchId);
            }
        } else {
            int searchId = ++lastSearchId;
            Utilities.searchQueue.postRunnable(searchRunnable = () -> processSearch(query, searchId), 300);
        }
    }

    private void processSearch(String query, int id) {
        Utilities.searchQueue.postRunnable(() -> {
            String search1 = query.trim().toLowerCase();
            if (search1.length() == 0) {
                postSearchResults(null, null, id);
                return;
            }
            String search2 = LocaleController.getInstance().getTranslitString(search1);
            if (search1.equals(search2) || search2.length() == 0) {
                search2 = null;
            }
            String[] search = new String[1 + (search2 != null ? 1 : 0)];
            search[0] = search1;
            if (search2 != null) {
                search[1] = search2;
            }

            ArrayList<TLRPC.Chat> resultArray = new ArrayList<>();
            ArrayList<String> resultArraySignatures = new ArrayList<>();

            for (int a = 0; a < inactiveChats.size(); a++) {
                TLRPC.Chat chat = inactiveChats.get(a);
                boolean found = false;
                for (int i = 0; i < 2; i++) {
                    String name = i == 0 ? chat.title : ChatObject.getPublicUsername(chat);
                    if (name == null) {
                        continue;
                    }
                    name = name.toLowerCase();
                    for (String q : search) {
                        if (name.startsWith(q) || name.contains(" " + q)) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        resultArray.add(chat);
                        resultArraySignatures.add(inactiveChatsSignatures.get(a));
                        break;
                    }
                }
            }

            postSearchResults(resultArray, resultArraySignatures, id);
        });
    }

    private void postSearchResults(ArrayList<TLRPC.Chat> chats, ArrayList<String> signatures, int searchId) {
        AndroidUtilities.runOnUIThread(() -> {
            if (view != null) {
                view.onSearchResults(chats, signatures, searchId);
            }
        });
    }

    public void leaveSelectedChats() {
        if (selectedIds.isEmpty()) {
            return;
        }
        TLRPC.User currentUser = chatRepository.getUser(chatRepository.getClientUserId());
        ArrayList<TLRPC.Chat> chats = new ArrayList<>();
        for (int i = 0; i < inactiveChats.size(); i++) {
            if (selectedIds.contains(inactiveChats.get(i).id)) {
                chats.add(inactiveChats.get(i));
            }
        }
        for (int i = 0; i < chats.size(); i++) {
            TLRPC.Chat chat = chats.get(i);
            chatRepository.putChat(chat, false);
            chatRepository.deleteParticipantFromChat(chat.id, currentUser);
        }
        if (view != null) {
            view.finishScreen();
        }
    }
}
