package org.telegram.core.presenter;

import android.text.TextUtils;

import org.telegram.core.data.repository.ChatRepository;
import org.telegram.core.data.repository.ChatRepositoryImpl;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

public class LinkEditPresenter {

    public interface LinkEditView {
        void shakeTime();
        void showLoading(boolean show);
        void onLinkCreated(TLObject response);
        void onLinkEdited(TLRPC.TL_chatInviteExported invite, TLObject response);
        void showError(String error);
        void finish();
    }

    private final ChatRepository chatRepository;
    private LinkEditView view;
    private boolean loading;

    public LinkEditPresenter(int currentAccount) {
        this(new ChatRepositoryImpl(currentAccount));
    }

    public LinkEditPresenter(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public void attachView(LinkEditView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public boolean isLoading() {
        return loading;
    }

    public TLRPC.Chat getChat(long chatId) {
        return chatRepository.getChat(chatId);
    }

    public int getCurrentTime() {
        return chatRepository.getCurrentTime();
    }

    public void createLink(long chatId, String title, int timeIndex, ArrayList<Integer> displayedDates, int usesIndex, ArrayList<Integer> displayedUses, boolean requestNeeded) {
        if (loading || view == null) {
            return;
        }
        if (displayedDates != null && timeIndex < displayedDates.size() && displayedDates.get(timeIndex) < 0) {
            view.shakeTime();
            return;
        }

        loading = true;
        view.showLoading(true);

        int expireDate = 0;
        if (displayedDates != null && timeIndex < displayedDates.size()) {
            expireDate = displayedDates.get(timeIndex) + chatRepository.getCurrentTime();
        }

        int usageLimit = 0;
        if (displayedUses != null && usesIndex < displayedUses.size()) {
            usageLimit = displayedUses.get(usesIndex);
        }

        chatRepository.exportChatInvite(chatId, title, expireDate, usageLimit, requestNeeded, new ChatRepository.LinkCallback() {
            @Override
            public void onSuccess(TLObject response) {
                loading = false;
                if (view != null) {
                    view.showLoading(false);
                    view.onLinkCreated(response);
                    view.finish();
                }
            }

            @Override
            public void onError(TLRPC.TL_error error) {
                loading = false;
                if (view != null) {
                    view.showLoading(false);
                    view.showError(error != null ? error.text : null);
                }
            }
        });
    }

    public void editLink(long chatId, TLRPC.TL_chatInviteExported inviteToEdit, String newTitle, int timeIndex, ArrayList<Integer> displayedDates, int currentInviteDate, int usesIndex, ArrayList<Integer> displayedUses, boolean requestNeeded) {
        if (loading || view == null || inviteToEdit == null) {
            return;
        }
        if (displayedDates != null && timeIndex < displayedDates.size() && displayedDates.get(timeIndex) < 0) {
            view.shakeTime();
            return;
        }

        boolean edited = false;
        boolean expireDateChanged = false;
        int expireDate = 0;

        if (displayedDates != null && timeIndex < displayedDates.size()) {
            if (currentInviteDate != displayedDates.get(timeIndex)) {
                expireDateChanged = true;
                expireDate = displayedDates.get(timeIndex) + chatRepository.getCurrentTime();
                edited = true;
            }
        } else {
            if (currentInviteDate != 0) {
                expireDateChanged = true;
                expireDate = 0;
                edited = true;
            }
        }

        boolean usageLimitChanged = false;
        int usageLimit = 0;
        if (displayedUses != null && usesIndex < displayedUses.size()) {
            int newLimit = displayedUses.get(usesIndex);
            if (inviteToEdit.usage_limit != newLimit) {
                usageLimitChanged = true;
                usageLimit = newLimit;
                edited = true;
            }
        } else {
            if (inviteToEdit.usage_limit != 0) {
                usageLimitChanged = true;
                usageLimit = 0;
                edited = true;
            }
        }

        boolean requestNeededChanged = false;
        if (inviteToEdit.request_needed != requestNeeded) {
            requestNeededChanged = true;
            edited = true;
        }

        boolean titleChanged = false;
        if (!TextUtils.equals(inviteToEdit.title, newTitle)) {
            titleChanged = true;
            edited = true;
        }

        if (!edited) {
            view.finish();
            return;
        }

        loading = true;
        view.showLoading(true);

        chatRepository.editExportedChatInvite(chatId, inviteToEdit.link, newTitle, titleChanged, expireDate, expireDateChanged, usageLimit, usageLimitChanged, requestNeeded, requestNeededChanged, new ChatRepository.LinkCallback() {
            @Override
            public void onSuccess(TLObject response) {
                loading = false;
                if (view != null) {
                    view.showLoading(false);
                    TLRPC.TL_chatInviteExported updatedInvite = inviteToEdit;
                    if (response instanceof TLRPC.TL_messages_exportedChatInvite) {
                        updatedInvite = (TLRPC.TL_chatInviteExported) ((TLRPC.TL_messages_exportedChatInvite) response).invite;
                    }
                    view.onLinkEdited(updatedInvite, response);
                    view.finish();
                }
            }

            @Override
            public void onError(TLRPC.TL_error error) {
                loading = false;
                if (view != null) {
                    view.showLoading(false);
                    view.showError(error != null ? error.text : null);
                }
            }
        });
    }
}
