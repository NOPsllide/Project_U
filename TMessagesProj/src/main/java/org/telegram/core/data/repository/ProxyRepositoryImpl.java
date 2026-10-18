package org.telegram.core.data.repository;

import android.content.SharedPreferences;
import android.os.SystemClock;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.SharedConfig;
import org.telegram.tgnet.ConnectionsManager;

import java.util.List;

public class ProxyRepositoryImpl implements ProxyRepository {

    @Override
    public void loadProxyList() {
        SharedConfig.loadProxyList();
    }

    @Override
    public List<SharedConfig.ProxyInfo> getProxyList() {
        return SharedConfig.proxyList;
    }

    @Override
    public SharedConfig.ProxyInfo getCurrentProxy() {
        return SharedConfig.currentProxy;
    }

    @Override
    public void setCurrentProxy(SharedConfig.ProxyInfo proxyInfo) {
        SharedConfig.currentProxy = proxyInfo;
    }

    @Override
    public void addProxy(SharedConfig.ProxyInfo proxyInfo) {
        SharedConfig.addProxy(proxyInfo);
    }

    @Override
    public void deleteProxy(SharedConfig.ProxyInfo proxyInfo) {
        SharedConfig.deleteProxy(proxyInfo);
    }

    @Override
    public void saveProxyList() {
        SharedConfig.saveProxyList();
    }

    @Override
    public boolean isProxyEnabled() {
        return MessagesController.getGlobalMainSettings().getBoolean("proxy_enabled", false) && !SharedConfig.proxyList.isEmpty();
    }

    @Override
    public void setProxyEnabled(boolean enabled) {
        SharedPreferences.Editor editor = MessagesController.getGlobalMainSettings().edit();
        editor.putBoolean("proxy_enabled", enabled);
        editor.commit();
    }

    @Override
    public boolean isProxyForCallsEnabled() {
        return MessagesController.getGlobalMainSettings().getBoolean("proxy_enabled_calls", false);
    }

    @Override
    public void setProxyForCallsEnabled(boolean enabled) {
        SharedPreferences.Editor editor = MessagesController.getGlobalMainSettings().edit();
        editor.putBoolean("proxy_enabled_calls", enabled);
        editor.commit();
    }

    @Override
    public void applyProxySettings(boolean enabled, SharedConfig.ProxyInfo proxyInfo) {
        if (proxyInfo != null) {
            SharedPreferences.Editor editor = MessagesController.getGlobalMainSettings().edit();
            editor.putString("proxy_ip", proxyInfo.address);
            editor.putString("proxy_pass", proxyInfo.password);
            editor.putString("proxy_user", proxyInfo.username);
            editor.putInt("proxy_port", proxyInfo.port);
            editor.putString("proxy_secret", proxyInfo.secret);
            editor.putBoolean("proxy_enabled", enabled);
            if (!proxyInfo.secret.isEmpty()) {
                editor.putBoolean("proxy_enabled_calls", false);
            }
            editor.commit();
            ConnectionsManager.setProxySettings(enabled, proxyInfo.address, proxyInfo.port, proxyInfo.username, proxyInfo.password, proxyInfo.secret);
        }
    }

    @Override
    public int getConnectionState(int account) {
        return ConnectionsManager.getInstance(account).getConnectionState();
    }

    @Override
    public void checkProxy(int account, SharedConfig.ProxyInfo proxyInfo, Runnable onDone) {
        if (proxyInfo == null || proxyInfo.checking || SystemClock.elapsedRealtime() - proxyInfo.availableCheckTime < 2 * 60 * 1000) {
            return;
        }
        proxyInfo.checking = true;
        proxyInfo.proxyCheckPingId = ConnectionsManager.getInstance(account).checkProxy(proxyInfo.address, proxyInfo.port, proxyInfo.username, proxyInfo.password, proxyInfo.secret, time -> AndroidUtilities.runOnUIThread(() -> {
            proxyInfo.availableCheckTime = SystemClock.elapsedRealtime();
            proxyInfo.checking = false;
            if (time == -1) {
                proxyInfo.available = false;
                proxyInfo.ping = 0;
            } else {
                proxyInfo.ping = time;
                proxyInfo.available = true;
            }
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.proxyCheckDone, proxyInfo);
            if (onDone != null) {
                onDone.run();
            }
        }));
    }
}
