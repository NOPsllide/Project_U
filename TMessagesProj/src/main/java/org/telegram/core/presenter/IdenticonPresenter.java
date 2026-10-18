package org.telegram.core.presenter;

import android.text.SpannableStringBuilder;
import android.text.Spanned;

import org.telegram.core.data.repository.ChatRepository;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.EmojiData;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.URLSpanReplacement;

public class IdenticonPresenter {

    public interface View {
    }

    private final ChatRepository chatRepository;
    private final int chatId;
    private View view;

    private TLRPC.EncryptedChat encryptedChat;
    private TLRPC.User user;
    private String formattedHash = "";
    private String emojiText = "";
    private CharSequence explanationText = "";

    public IdenticonPresenter(ChatRepository chatRepository, int chatId) {
        this.chatRepository = chatRepository;
        this.chatId = chatId;
    }

    public void attachView(View view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void onCreate() {
        encryptedChat = chatRepository.getEncryptedChat(chatId);
        if (encryptedChat == null) {
            return;
        }

        user = chatRepository.getUser(encryptedChat.user_id);

        SpannableStringBuilder hash = new SpannableStringBuilder();
        StringBuilder emojis = new StringBuilder();
        if (encryptedChat.key_hash != null && encryptedChat.key_hash.length > 16) {
            String hex = Utilities.bytesToHex(encryptedChat.key_hash);
            for (int a = 0; a < 32; a++) {
                if (a != 0) {
                    if (a % 8 == 0) {
                        hash.append('\n');
                    } else if (a % 4 == 0) {
                        hash.append(' ');
                    }
                }
                hash.append(hex.substring(a * 2, a * 2 + 2));
                hash.append(' ');
            }
            hash.append("\n");
            for (int a = 0; a < 5; a++) {
                int num = ((encryptedChat.key_hash[16 + a * 4] & 0x7f) << 24)
                        | ((encryptedChat.key_hash[16 + a * 4 + 1] & 0xff) << 16)
                        | ((encryptedChat.key_hash[16 + a * 4 + 2] & 0xff) << 8)
                        | (encryptedChat.key_hash[16 + a * 4 + 3] & 0xff);
                if (a != 0) {
                    emojis.append(" ");
                }
                emojis.append(EmojiData.emojiSecret[num % EmojiData.emojiSecret.length]);
            }
            emojiText = emojis.toString();
        }
        formattedHash = hash.toString();

        SpannableStringBuilder explanation = new SpannableStringBuilder();
        if (user != null) {
            explanation.append(AndroidUtilities.replaceTags(LocaleController.formatString("EncryptionKeyDescription", R.string.EncryptionKeyDescription, user.first_name, user.first_name)));
            final String url = "telegram.org";
            int index = explanation.toString().indexOf(url);
            if (index != -1) {
                explanation.setSpan(new URLSpanReplacement(LocaleController.getString("EncryptionKeyLink", R.string.EncryptionKeyLink)), index, index + url.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        explanationText = explanation;
    }

    public TLRPC.EncryptedChat getEncryptedChat() {
        return encryptedChat;
    }

    public TLRPC.User getUser() {
        return user;
    }

    public String getFormattedHash() {
        return formattedHash;
    }

    public String getEmojiText() {
        return emojiText;
    }

    public CharSequence getExplanationText() {
        return explanationText;
    }
}
