package org.telegram.core.presenter;

import org.telegram.core.data.repository.ProxyRepository;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.SharedConfig;
import org.telegram.tgnet.ConnectionsManager;

import java.util.List;

public class ProxyListPresenter {

    public interface View {
        void notifyDataSetChanged();
        void notifyItemChanged(int position);
        void notifyItemRangeInserted(int position, int count);
        void notifyItemRangeRemoved(int position, int count);
        void notifyItemRemoved(int position);
        void notifyItemChanged(int position, Object payload);
        void updateProxyCells();
        void updateProxyCell(int index);
        void showProxySettings();
        void showDeleteConfirmation(SharedConfig.ProxyInfo proxyInfo, int position);
    }

    private final ProxyRepository proxyRepository;
    private final int currentAccount;
    private View view;

    private int currentConnectionState;
    private boolean useProxySettings;
    private boolean useProxyForCalls;

    private int rowCount;
    private int useProxyRow;
    private int useProxyDetailRow;
    private int connectionsHeaderRow;
    private int proxyStartRow;
    private int proxyEndRow;
    private int proxyAddRow;
    private int proxyDetailRow;
    private int callsRow;
    private int callsDetailRow;

    public ProxyListPresenter(ProxyRepository proxyRepository, int currentAccount) {
        this.proxyRepository = proxyRepository;
        this.currentAccount = currentAccount;
    }

    public void attachView(View view) {
        this.view = view;
        proxyRepository.loadProxyList();
        currentConnectionState = proxyRepository.getConnectionState(currentAccount);
        useProxySettings = proxyRepository.isProxyEnabled();
        useProxyForCalls = proxyRepository.isProxyForCallsEnabled();
        updateRows(true);
    }

    public void detachView() {
        this.view = null;
    }

    public void updateRows(boolean notify) {
        rowCount = 0;
        useProxyRow = rowCount++;
        useProxyDetailRow = rowCount++;
        connectionsHeaderRow = rowCount++;
        List<SharedConfig.ProxyInfo> list = proxyRepository.getProxyList();
        if (!list.isEmpty()) {
            proxyStartRow = rowCount;
            rowCount += list.size();
            proxyEndRow = rowCount;
        } else {
            proxyStartRow = -1;
            proxyEndRow = -1;
        }
        proxyAddRow = rowCount++;
        proxyDetailRow = rowCount++;

        SharedConfig.ProxyInfo current = proxyRepository.getCurrentProxy();
        if (current == null || current.secret.isEmpty()) {
            boolean change = callsRow == -1;
            callsRow = rowCount++;
            callsDetailRow = rowCount++;
            if (!notify && change && view != null) {
                view.notifyItemChanged(proxyDetailRow);
                view.notifyItemRangeInserted(proxyDetailRow + 1, 2);
            }
        } else {
            boolean change = callsRow != -1;
            callsRow = -1;
            callsDetailRow = -1;
            if (!notify && change && view != null) {
                view.notifyItemChanged(proxyDetailRow);
                view.notifyItemRangeRemoved(proxyDetailRow + 1, 2);
            }
        }
        checkProxyList();
        if (notify && view != null) {
            view.notifyDataSetChanged();
        }
    }

    public void checkProxyList() {
        List<SharedConfig.ProxyInfo> list = proxyRepository.getProxyList();
        for (int a = 0, count = list.size(); a < count; a++) {
            SharedConfig.ProxyInfo proxyInfo = list.get(a);
            proxyRepository.checkProxy(currentAccount, proxyInfo, null);
        }
    }

    public void onItemClick(int position) {
        if (position == useProxyRow) {
            SharedConfig.ProxyInfo current = proxyRepository.getCurrentProxy();
            List<SharedConfig.ProxyInfo> list = proxyRepository.getProxyList();
            if (current == null) {
                if (!list.isEmpty()) {
                    current = list.get(0);
                    proxyRepository.setCurrentProxy(current);
                    if (!useProxySettings) {
                        proxyRepository.applyProxySettings(false, current);
                    }
                } else {
                    if (view != null) {
                        view.showProxySettings();
                    }
                    return;
                }
            }
            useProxySettings = !useProxySettings;
            if (!useProxySettings) {
                useProxyForCalls = false;
            }
            proxyRepository.setProxyEnabled(useProxySettings);
            proxyRepository.applyProxySettings(useProxySettings, current);
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.proxySettingsChanged);
            if (view != null) {
                view.updateProxyCells();
            }
        } else if (position == callsRow) {
            useProxyForCalls = !useProxyForCalls;
            proxyRepository.setProxyForCallsEnabled(useProxyForCalls);
            if (view != null) {
                view.notifyItemChanged(callsRow);
            }
        } else if (position >= proxyStartRow && position < proxyEndRow) {
            List<SharedConfig.ProxyInfo> list = proxyRepository.getProxyList();
            SharedConfig.ProxyInfo info = list.get(position - proxyStartRow);
            useProxySettings = true;
            if (!info.secret.isEmpty()) {
                useProxyForCalls = false;
            }
            proxyRepository.setCurrentProxy(info);
            proxyRepository.applyProxySettings(useProxySettings, info);
            updateRows(false);
            if (view != null) {
                view.updateProxyCells();
                view.notifyItemChanged(useProxyRow);
            }
        } else if (position == proxyAddRow) {
            if (view != null) {
                view.showProxySettings();
            }
        }
    }

    public boolean onItemLongClick(int position) {
        if (position >= proxyStartRow && position < proxyEndRow) {
            List<SharedConfig.ProxyInfo> list = proxyRepository.getProxyList();
            SharedConfig.ProxyInfo info = list.get(position - proxyStartRow);
            if (view != null) {
                view.showDeleteConfirmation(info, position);
            }
            return true;
        }
        return false;
    }

    public void deleteProxyConfirmed(SharedConfig.ProxyInfo info, int position) {
        proxyRepository.deleteProxy(info);
        if (proxyRepository.getCurrentProxy() == null) {
            useProxyForCalls = false;
            useProxySettings = false;
        }
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.proxySettingsChanged);
        updateRows(false);
        if (view != null) {
            view.notifyItemRemoved(position);
            if (proxyRepository.getCurrentProxy() == null) {
                view.notifyItemChanged(useProxyRow, 0);
                view.notifyItemChanged(callsRow, 0);
            }
        }
    }

    public void onNotificationReceived(int id, int account, Object... args) {
        if (id == NotificationCenter.proxySettingsChanged) {
            updateRows(true);
        } else if (id == NotificationCenter.didUpdateConnectionState) {
            int state = proxyRepository.getConnectionState(account);
            if (currentConnectionState != state) {
                currentConnectionState = state;
                SharedConfig.ProxyInfo current = proxyRepository.getCurrentProxy();
                if (current != null) {
                    int idx = proxyRepository.getProxyList().indexOf(current);
                    if (idx >= 0 && view != null) {
                        view.updateProxyCell(idx);
                    }
                }
            }
        } else if (id == NotificationCenter.proxyCheckDone) {
            SharedConfig.ProxyInfo proxyInfo = (SharedConfig.ProxyInfo) args[0];
            int idx = proxyRepository.getProxyList().indexOf(proxyInfo);
            if (idx >= 0 && view != null) {
                view.updateProxyCell(idx);
            }
        }
    }

    public int getItemViewType(int position) {
        if (position == useProxyDetailRow || position == proxyDetailRow) {
            return 0;
        } else if (position == proxyAddRow) {
            return 1;
        } else if (position == useProxyRow || position == callsRow) {
            return 3;
        } else if (position == connectionsHeaderRow) {
            return 2;
        } else if (position >= proxyStartRow && position < proxyEndRow) {
            return 5;
        } else {
            return 4;
        }
    }

    public boolean isEnabled(int position) {
        return position == useProxyRow || position == callsRow || position == proxyAddRow || (position >= proxyStartRow && position < proxyEndRow);
    }

    public SharedConfig.ProxyInfo getProxyInfo(int position) {
        int index = position - proxyStartRow;
        List<SharedConfig.ProxyInfo> list = proxyRepository.getProxyList();
        if (index >= 0 && index < list.size()) {
            return list.get(index);
        }
        return null;
    }

    public boolean isCurrentProxy(SharedConfig.ProxyInfo info) {
        return info != null && info == proxyRepository.getCurrentProxy();
    }

    public SharedConfig.ProxyInfo getCurrentProxy() {
        return proxyRepository.getCurrentProxy();
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getUseProxyRow() {
        return useProxyRow;
    }

    public int getUseProxyDetailRow() {
        return useProxyDetailRow;
    }

    public int getConnectionsHeaderRow() {
        return connectionsHeaderRow;
    }

    public int getProxyStartRow() {
        return proxyStartRow;
    }

    public int getProxyEndRow() {
        return proxyEndRow;
    }

    public int getProxyAddRow() {
        return proxyAddRow;
    }

    public int getProxyDetailRow() {
        return proxyDetailRow;
    }

    public int getCallsRow() {
        return callsRow;
    }

    public int getCallsDetailRow() {
        return callsDetailRow;
    }

    public boolean isUseProxySettings() {
        return useProxySettings;
    }

    public boolean isUseProxyForCalls() {
        return useProxyForCalls;
    }

    public int getCurrentConnectionState() {
        return currentConnectionState;
    }
}
