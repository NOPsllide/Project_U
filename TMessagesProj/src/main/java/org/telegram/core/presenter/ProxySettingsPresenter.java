package org.telegram.core.presenter;

import android.text.TextUtils;
import org.telegram.core.data.repository.ProxyRepository;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.Utilities;

import java.net.URLEncoder;

public class ProxySettingsPresenter {

    public interface View {
        void finishFragment();
    }

    public static class ParsedProxy {
        public int type;
        public String[] fields;

        public ParsedProxy(int type, String[] fields) {
            this.type = type;
            this.fields = fields;
        }
    }

    private final ProxyRepository proxyRepository;
    private final SharedConfig.ProxyInfo currentProxyInfo;
    private final boolean addingNewProxy;
    private View view;

    public ProxySettingsPresenter(ProxyRepository proxyRepository, SharedConfig.ProxyInfo proxyInfo, boolean addingNewProxy) {
        this.proxyRepository = proxyRepository;
        this.currentProxyInfo = proxyInfo;
        this.addingNewProxy = addingNewProxy;
    }

    public void attachView(View view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void saveProxy(String address, int port, String user, String password, String secret, int currentType) {
        currentProxyInfo.address = address;
        currentProxyInfo.port = port;
        if (currentType == 0) {
            currentProxyInfo.secret = "";
            currentProxyInfo.username = user;
            currentProxyInfo.password = password;
        } else {
            currentProxyInfo.secret = secret;
            currentProxyInfo.username = "";
            currentProxyInfo.password = "";
        }

        if (addingNewProxy) {
            proxyRepository.addProxy(currentProxyInfo);
            proxyRepository.setCurrentProxy(currentProxyInfo);
            proxyRepository.setProxyEnabled(true);
            proxyRepository.applyProxySettings(true, currentProxyInfo);
        } else {
            proxyRepository.saveProxyList();
            boolean enabled = proxyRepository.isProxyEnabled();
            if (proxyRepository.getCurrentProxy() == currentProxyInfo) {
                proxyRepository.applyProxySettings(enabled, currentProxyInfo);
            }
        }
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.proxySettingsChanged);
        if (view != null) {
            view.finishFragment();
        }
    }

    public String generateShareUrl(int currentType, String address, String port, String user, String password, String secret) {
        StringBuilder params = new StringBuilder();
        String url;
        try {
            if (!TextUtils.isEmpty(address)) {
                params.append("server=").append(URLEncoder.encode(address, "UTF-8"));
            }
            if (!TextUtils.isEmpty(port)) {
                if (params.length() != 0) {
                    params.append("&");
                }
                params.append("port=").append(URLEncoder.encode(port, "UTF-8"));
            }
            if (currentType == 1) {
                url = "https://t.me/proxy?";
                if (params.length() != 0) {
                    params.append("&");
                }
                params.append("secret=").append(URLEncoder.encode(secret, "UTF-8"));
            } else {
                url = "https://t.me/socks?";
                if (!TextUtils.isEmpty(user)) {
                    if (params.length() != 0) {
                        params.append("&");
                    }
                    params.append("user=").append(URLEncoder.encode(user, "UTF-8"));
                }
                if (!TextUtils.isEmpty(password)) {
                    if (params.length() != 0) {
                        params.append("&");
                    }
                    params.append("pass=").append(URLEncoder.encode(password, "UTF-8"));
                }
            }
            if (params.length() == 0) {
                return null;
            }
            return url + params.toString();
        } catch (Exception ignore) {
            return null;
        }
    }

    public ParsedProxy parseClipboardText(String clipText) {
        if (clipText == null) {
            return null;
        }
        int pasteType = -1;
        String[] params = null;
        String[] socksStrings = {"t.me/socks?", "tg://socks?"};
        for (String socksString : socksStrings) {
            int index = clipText.indexOf(socksString);
            if (index >= 0) {
                pasteType = 0;
                params = clipText.substring(index + socksString.length()).split("&");
                break;
            }
        }
        if (params == null) {
            String[] proxyStrings = {"t.me/proxy?", "tg://proxy?"};
            for (String proxyString : proxyStrings) {
                int index = clipText.indexOf(proxyString);
                if (index >= 0) {
                    pasteType = 1;
                    params = clipText.substring(index + proxyString.length()).split("&");
                    break;
                }
            }
        }
        if (params != null) {
            String[] pasteFields = new String[5];
            for (String param : params) {
                String[] pair = param.split("=");
                if (pair.length != 2) continue;
                switch (pair[0].toLowerCase()) {
                    case "server":
                        pasteFields[0] = pair[1];
                        break;
                    case "port":
                        pasteFields[1] = pair[1];
                        break;
                    case "user":
                        if (pasteType == 0) {
                            pasteFields[2] = pair[1];
                        }
                        break;
                    case "pass":
                        if (pasteType == 0) {
                            pasteFields[3] = pair[1];
                        }
                        break;
                    case "secret":
                        if (pasteType == 1) {
                            pasteFields[4] = pair[1];
                        }
                        break;
                }
            }
            return new ParsedProxy(pasteType, pasteFields);
        }
        return null;
    }

    public boolean isValid(String address, String port) {
        return !TextUtils.isEmpty(address) && Utilities.parseInt(port) != 0;
    }

    public SharedConfig.ProxyInfo getCurrentProxyInfo() {
        return currentProxyInfo;
    }

    public boolean isAddingNewProxy() {
        return addingNewProxy;
    }
}
