package org.telegram.core.data.repository;

public interface SettingsRepository {

    String getQuickReply(int index);

    void saveQuickReply(int index, String text);

    boolean getAllowCustomQuickReply();

    void setAllowCustomQuickReply(boolean allow);
}
