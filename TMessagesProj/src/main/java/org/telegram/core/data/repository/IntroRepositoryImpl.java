package org.telegram.core.data.repository;

import android.content.SharedPreferences;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;

public class IntroRepositoryImpl implements IntroRepository {

    private final int currentAccount;

    public IntroRepositoryImpl(int currentAccount) {
        this.currentAccount = currentAccount;
    }

    @Override
    public void recordCrashTime(long time) {
        MessagesController.getGlobalMainSettings().edit().putLong("intro_crashed_time", time).apply();
    }

    @Override
    public void resetCrashTime() {
        MessagesController.getGlobalMainSettings().edit().putLong("intro_crashed_time", 0).apply();
    }

    @Override
    public void updateDcSettings() {
        ConnectionsManager.getInstance(currentAccount).updateDcSettings();
    }

    @Override
    public void loadRemoteLanguages() {
        LocaleController.getInstance().loadRemoteLanguages(currentAccount);
    }

    @Override
    public void checkSuggestedLanguage(SuggestedLanguageCallback callback) {
        LocaleController.LocaleInfo englishInfo = null;
        LocaleController.LocaleInfo systemInfo = null;
        LocaleController.LocaleInfo currentLocaleInfo = LocaleController.getInstance().getCurrentLocaleInfo();
        String systemLang = MessagesController.getInstance(currentAccount).suggestedLangCode;
        if (systemLang == null || systemLang.equals("en") && LocaleController.getInstance().getSystemDefaultLocale().getLanguage() != null && !LocaleController.getInstance().getSystemDefaultLocale().getLanguage().equals("en")) {
            systemLang = LocaleController.getInstance().getSystemDefaultLocale().getLanguage();
            if (systemLang == null) {
                systemLang = "en";
            }
        }

        String arg = systemLang.contains("-") ? systemLang.split("-")[0] : systemLang;
        String alias = LocaleController.getLocaleAlias(arg);
        for (int a = 0; a < LocaleController.getInstance().languages.size(); a++) {
            LocaleController.LocaleInfo info = LocaleController.getInstance().languages.get(a);
            if (info.shortName.equals("en")) {
                englishInfo = info;
            }
            if (info.shortName.replace("_", "-").equals(systemLang) || info.shortName.equals(arg) || info.shortName.equals(alias)) {
                systemInfo = info;
            }
            if (englishInfo != null && systemInfo != null) {
                break;
            }
        }
        if (englishInfo == null || systemInfo == null || englishInfo == systemInfo) {
            return;
        }
        TLRPC.TL_langpack_getStrings req = new TLRPC.TL_langpack_getStrings();
        final LocaleController.LocaleInfo targetLocaleInfo;
        if (systemInfo != currentLocaleInfo) {
            req.lang_code = systemInfo.getLangCode();
            targetLocaleInfo = systemInfo;
        } else {
            req.lang_code = englishInfo.getLangCode();
            targetLocaleInfo = englishInfo;
        }
        req.keys.add("ContinueOnThisLanguage");
        final String finalSystemLang = systemLang;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> {
            if (response != null) {
                TLRPC.Vector vector = (TLRPC.Vector) response;
                if (vector.objects.isEmpty()) {
                    return;
                }
                final TLRPC.LangPackString string = (TLRPC.LangPackString) vector.objects.get(0);
                if (string instanceof TLRPC.TL_langPackString) {
                    AndroidUtilities.runOnUIThread(() -> {
                        SharedPreferences preferences = MessagesController.getGlobalMainSettings();
                        preferences.edit().putString("language_showed2", finalSystemLang.toLowerCase()).apply();
                        if (callback != null) {
                            callback.onSuggestedLanguageLoaded(string.value, targetLocaleInfo);
                        }
                    });
                }
            }
        }, ConnectionsManager.RequestFlagWithoutLogin);
    }

    @Override
    public void applyLanguage(LocaleController.LocaleInfo info, LanguageAppliedCallback callback) {
        if (info == null) {
            return;
        }
        NotificationCenter.getGlobalInstance().addObserver(new NotificationCenter.NotificationCenterDelegate() {
            @Override
            public void didReceivedNotification(int id, int account, Object... args) {
                if (id == NotificationCenter.reloadInterface) {
                    NotificationCenter.getGlobalInstance().removeObserver(this, id);
                    if (callback != null) {
                        callback.onLanguageApplied();
                    }
                }
            }
        }, NotificationCenter.reloadInterface);
        LocaleController.getInstance().applyLanguage(info, true, false, currentAccount);
    }
}
