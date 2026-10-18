package org.telegram.core.data.repository;

import org.telegram.tgnet.TLRPC;
import java.util.List;

public interface SettingsRepository {

    String getQuickReply(int index);

    void saveQuickReply(int index, String text);

    boolean getAllowCustomQuickReply();

    void setAllowCustomQuickReply(boolean allow);

    String getDoubleTapReaction();

    void setDoubleTapReaction(String reaction);

    List<TLRPC.TL_availableReaction> getAvailableReactions();

    TLRPC.TL_availableReaction getAvailableReaction(String key);

    boolean isPremium();
}
