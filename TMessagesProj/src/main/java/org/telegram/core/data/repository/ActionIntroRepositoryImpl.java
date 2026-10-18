package org.telegram.core.data.repository;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocationController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;

public class ActionIntroRepositoryImpl implements ActionIntroRepository {

    @Override
    public boolean isLocationEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            LocationManager lm = (LocationManager) ApplicationLoader.applicationContext.getSystemService(Context.LOCATION_SERVICE);
            return lm != null && lm.isLocationEnabled();
        } else if (Build.VERSION.SDK_INT >= 19) {
            try {
                int mode = Settings.Secure.getInt(ApplicationLoader.applicationContext.getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
                return mode != Settings.Secure.LOCATION_MODE_OFF;
            } catch (Throwable e) {
                FileLog.e(e);
            }
        }
        return true;
    }

    @Override
    public void fetchLocationAddress(Location location, LocationController.LocationFetchCallback callback) {
        LocationController.fetchLocationAddress(location, callback);
    }

    @Override
    public String getCurrentUserPhone(int currentAccount) {
        UserConfig userConfig = UserConfig.getInstance(currentAccount);
        TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(userConfig.clientUserId);
        if (user == null) {
            user = userConfig.getCurrentUser();
        }
        if (user != null && user.phone != null) {
            return PhoneFormat.getInstance().format("+" + user.phone);
        }
        return "";
    }

    @Override
    public Bundle buildChannelCreateArgs() {
        Bundle args = new Bundle();
        args.putInt("step", 0);
        return args;
    }

    @Override
    public Bundle buildGroupCreateArgs(long userId, String address, Location location) {
        Bundle args = new Bundle();
        args.putLongArray("result", new long[]{userId});
        args.putInt("chatType", ChatObject.CHAT_TYPE_MEGAGROUP);
        args.putString("address", address);
        args.putParcelable("location", location);
        return args;
    }
}
