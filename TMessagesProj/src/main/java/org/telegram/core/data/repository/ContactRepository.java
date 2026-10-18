package org.telegram.core.data.repository;

import org.telegram.tgnet.TLRPC;

public interface ContactRepository {

    TLRPC.User getUser(long userId);

    void addContact(TLRPC.User user, boolean sharePhone);

    boolean needsAddException(long userId);

    void markDialogBarAsProcessed(long userId);

    void notifyContactAdded(long userId);
}
