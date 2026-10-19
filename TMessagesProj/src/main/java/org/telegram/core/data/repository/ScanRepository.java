package org.telegram.core.data.repository;

import android.graphics.Bitmap;
import org.telegram.messenger.MrzRecognizer;

public interface ScanRepository {
    MrzRecognizer.Result processMrz(Bitmap bitmap);
    boolean processQr(String text, Runnable onLoadEnd);
}
