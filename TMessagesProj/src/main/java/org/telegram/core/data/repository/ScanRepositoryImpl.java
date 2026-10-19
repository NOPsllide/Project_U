package org.telegram.core.data.repository;

import android.graphics.Bitmap;
import org.telegram.messenger.MrzRecognizer;

public class ScanRepositoryImpl implements ScanRepository {

    @Override
    public MrzRecognizer.Result processMrz(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        return MrzRecognizer.recognize(bitmap, false);
    }

    @Override
    public boolean processQr(String text, Runnable onLoadEnd) {
        if (onLoadEnd != null) {
            onLoadEnd.run();
        }
        return true;
    }
}
