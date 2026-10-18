package org.telegram.core.data.repository;

import org.telegram.messenger.SharedConfig;

import java.util.List;

public interface ProxyRepository {

    void loadProxyList();

    List<SharedConfig.ProxyInfo> getProxyList();

    SharedConfig.ProxyInfo getCurrentProxy();

    void setCurrentProxy(SharedConfig.ProxyInfo proxyInfo);

    void addProxy(SharedConfig.ProxyInfo proxyInfo);

    void deleteProxy(SharedConfig.ProxyInfo proxyInfo);

    void saveProxyList();

    boolean isProxyEnabled();

    void setProxyEnabled(boolean enabled);

    boolean isProxyForCallsEnabled();

    void setProxyForCallsEnabled(boolean enabled);

    void applyProxySettings(boolean enabled, SharedConfig.ProxyInfo proxyInfo);

    int getConnectionState(int account);

    void checkProxy(int account, SharedConfig.ProxyInfo proxyInfo, Runnable onDone);
}
