package org.telegram.core.presenter;

import android.content.Context;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.VideoCapturer;

public class TestPresenter {

    public interface TestView {
    }

    private TestView view;

    public void attachView(TestView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public VideoCapturer createVideoCapturer(Context context) {
        CameraEnumerator enumerator = Camera2Enumerator.isSupported(context) ? new Camera2Enumerator(context) : new Camera1Enumerator();
        return enumerator.createCapturer(enumerator.getDeviceNames()[0], null);
    }
}
