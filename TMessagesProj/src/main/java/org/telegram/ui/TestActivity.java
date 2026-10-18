/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import org.telegram.core.presenter.TestPresenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.webrtc.VideoCapturer;

@TargetApi(18)
public class TestActivity extends BaseFragment implements TestPresenter.TestView {

    private TestPresenter presenter;

    public TestActivity() {
        super();
        presenter = new TestPresenter();
    }

    @Override
    public boolean onFragmentCreate() {
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (presenter != null) {
            presenter.detachView();
        }
    }

    @Override
    public View createView(Context context) {
        if (presenter != null) {
            presenter.attachView(this);
        }
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("Test");

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setBackgroundColor(0xffffffff);
        fragmentView = frameLayout;

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void hangup() {

    }

    public VideoCapturer createVideoCapturer(Context context) {
        return presenter != null ? presenter.createVideoCapturer(context) : null;
    }
}
