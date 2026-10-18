package org.telegram.core.presenter;

import org.telegram.core.data.repository.ChatRepository;
import org.telegram.messenger.ChatObject;
import org.telegram.tgnet.TLRPC;
import java.util.ArrayList;
import java.util.List;

public class ChatReactionsPresenter {

    public interface View {
        void notifyDataSetChanged();
        void notifyItemChanged(int position);
        void notifyItemRangeInserted(int position, int count);
        void notifyItemRangeRemoved(int position, int count);
    }

    public static final int SELECT_TYPE_NONE = 2;
    public static final int SELECT_TYPE_SOME = 1;
    public static final int SELECT_TYPE_ALL = 0;

    private final ChatRepository chatRepository;
    private final long chatId;
    private View view;

    private TLRPC.Chat currentChat;
    private TLRPC.ChatFull info;
    private List<String> chatReactions = new ArrayList<>();
    private ArrayList<TLRPC.TL_availableReaction> availableReactions = new ArrayList<>();
    private int selectedType = -1;
    private int startFromType;
    private boolean isChannel;

    public ChatReactionsPresenter(ChatRepository chatRepository, long chatId) {
        this.chatRepository = chatRepository;
        this.chatId = chatId;
    }

    public void attachView(View view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public boolean onCreate() {
        currentChat = chatRepository.getChat(chatId);
        if (currentChat == null) {
            currentChat = chatRepository.getChatSync(chatId);
            if (currentChat != null) {
                chatRepository.putChat(currentChat, true);
            } else {
                return false;
            }
            if (info == null) {
                info = chatRepository.loadChatInfo(chatId, ChatObject.isChannel(currentChat));
                if (info == null) {
                    return false;
                }
            }
        }
        return true;
    }

    public void loadAvailableReactions() {
        availableReactions.clear();
        availableReactions.addAll(chatRepository.getEnabledReactionsList());
    }

    public void onReactionsLoaded() {
        loadAvailableReactions();
        if (view != null) {
            view.notifyDataSetChanged();
        }
    }

    public void setInfo(TLRPC.ChatFull info) {
        this.info = info;
        if (info != null) {
            if (currentChat == null) {
                currentChat = chatRepository.getChat(chatId);
            }
            chatReactions = new ArrayList<>();
            if (info.available_reactions instanceof TLRPC.TL_chatReactionsAll) {
                startFromType = SELECT_TYPE_ALL;
            } else if (info.available_reactions instanceof TLRPC.TL_chatReactionsNone) {
                startFromType = SELECT_TYPE_NONE;
            } else if (info.available_reactions instanceof TLRPC.TL_chatReactionsSome) {
                TLRPC.TL_chatReactionsSome reactionsSome = (TLRPC.TL_chatReactionsSome) info.available_reactions;
                for (int i = 0; i < reactionsSome.reactions.size(); i++) {
                    if (reactionsSome.reactions.get(i) instanceof TLRPC.TL_reactionEmoji) {
                        chatReactions.add(((TLRPC.TL_reactionEmoji) reactionsSome.reactions.get(i)).emoticon);
                    }
                }
                startFromType = SELECT_TYPE_SOME;
            }
        }
    }

    public void setSelectedType(int selectType, boolean animated, boolean isChannel) {
        if (selectedType == selectType) {
            return;
        }

        this.selectedType = selectType;

        if (selectType == SELECT_TYPE_SOME) {
            if (animated) {
                chatReactions.clear();
                for (TLRPC.TL_availableReaction a : availableReactions) {
                    if (a.reaction.equals("\uD83D\uDC4D") || a.reaction.equals("\uD83D\uDC4E")) {
                        chatReactions.add(a.reaction);
                    }
                }
                if (chatReactions.isEmpty() && availableReactions.size() >= 2) {
                    chatReactions.add(availableReactions.get(0).reaction);
                    chatReactions.add(availableReactions.get(1).reaction);
                }
            }
            if (view != null && animated) {
                view.notifyItemRangeInserted((isChannel ? 1 : 2), 1 + availableReactions.size());
            }
        } else {
            if (!chatReactions.isEmpty()) {
                chatReactions.clear();
                if (view != null && animated) {
                    view.notifyItemRangeRemoved((isChannel ? 1 : 2), 1 + availableReactions.size());
                }
            }
        }
        if (!isChannel && view != null && animated) {
            view.notifyItemChanged(1);
        }
        if (view != null && !animated) {
            view.notifyDataSetChanged();
        }
    }

    public boolean toggleReaction(String reaction) {
        boolean nc = !chatReactions.contains(reaction);
        if (nc) {
            chatReactions.add(reaction);
        } else {
            chatReactions.remove(reaction);
        }
        return nc;
    }

    public void onDestroy() {
        chatRepository.setChatReactions(chatId, selectedType, chatReactions);
    }

    public List<String> getChatReactions() {
        return chatReactions;
    }

    public ArrayList<TLRPC.TL_availableReaction> getAvailableReactions() {
        return availableReactions;
    }

    public int getSelectedType() {
        return selectedType;
    }

    public int getStartFromType() {
        return startFromType;
    }

    public boolean isChannel() {
        return ChatObject.isChannelAndNotMegaGroup(chatId, currentChat);
    }

    public TLRPC.Chat getCurrentChat() {
        return currentChat;
    }
}
