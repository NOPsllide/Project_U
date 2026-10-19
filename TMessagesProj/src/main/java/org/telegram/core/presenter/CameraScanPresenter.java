package org.telegram.core.presenter;

import android.graphics.Bitmap;
import org.telegram.core.data.repository.ScanRepository;
import org.telegram.core.data.repository.ScanRepositoryImpl;
import org.telegram.messenger.MrzRecognizer;

public class CameraScanPresenter {

    public interface CameraScanView {
        void onMrzRecognized(MrzRecognizer.Result result);
        void onQrRecognized(String text);
    }

    private CameraScanView view;
    private final ScanRepository scanRepository;

    public CameraScanPresenter() {
        this(new ScanRepositoryImpl());
    }

    public CameraScanPresenter(ScanRepository scanRepository) {
        this.scanRepository = scanRepository;
    }

    public void attachView(CameraScanView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void processMrz(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        MrzRecognizer.Result result = scanRepository.processMrz(bitmap);
        if (result != null && view != null) {
            view.onMrzRecognized(result);
        }
    }

    public void handleQrResult(String text) {
        if (text != null && view != null) {
            view.onQrRecognized(text);
        }
    }

    public boolean processQr(String text, Runnable onLoadEnd) {
        return scanRepository.processQr(text, onLoadEnd);
    }
}
