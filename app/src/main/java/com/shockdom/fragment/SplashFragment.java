package com.shockdom.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.shockdom.R;
import com.shockdom.activity.SmartComixActivity;
import com.shockdom.toolbar.ToolbarCardListPagerFragment;

/**
 * Created by walt on 07/06/15.
 */
public class SplashFragment extends Fragment {

    private static final int FINISH_SPLASH = 1;
    private static final int SPLASH_DELAY_LONG = 2000;

    private boolean hasDelayed;
    private boolean isInForeground;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        WindowUtils.changeStatusBarColor(getActivity(), android.R.color.black);

        final View v = inflater.inflate(R.layout.fragment_splash, container, false);

        //start timer for splash duration
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hasDelayed = true;
                finishSplash();
            }
        });
        v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                v.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                new DelayHandler().sendEmptyMessageDelayed(FINISH_SPLASH, SPLASH_DELAY_LONG);
            }
        });

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        isInForeground = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        isInForeground = true;
        finishSplash();
    }

    private void finishSplash() {
        if (hasDelayed && isInForeground && getActivity() instanceof SmartComixActivity) {
            ToolbarCardListPagerFragment f = new ToolbarCardListPagerFragment();
            ((SmartComixActivity) getActivity()).setFragment(f, false, false);
            VolumeTabPagerFragment f2 = new VolumeTabPagerFragment();
            f.setChildFragment(f2);
        }
    }

    /*
     * Inner class per il delay dello splash
     */
    private class DelayHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if(msg.what == FINISH_SPLASH && !hasDelayed) {
                hasDelayed = true;
                finishSplash();
            }
        }
    }

}
