package com.shockdom.toolbar;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shockdom.R;
import com.shockdom.fragment.BaseFragmentManager;
import com.shockdom.tabs.SlidingTabLayout;

/**
 * Created by Walt on 25/05/2015.
 */
public class ToolbarCardListPagerFragment extends Fragment implements ToolbarCardListPager {

    private View coloredBackgroundView;
    private View toolbarContainer;
    private Toolbar toolbar;
    private ViewGroup mTabsContainer;

    private MyScrollListener listener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_toolbar_card_list_pager, container, false);

        toolbar = (Toolbar) v.findViewById(R.id.toolbar_pager);

        /*if (getActivity() instanceof AppCompatActivity)
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);*/

        coloredBackgroundView = v.findViewById(R.id.colored_background_view);
        toolbarContainer = v.findViewById(R.id.toolbar_container);
        mTabsContainer = (ViewGroup) v.findViewById(R.id.fake_tab);

        return v;
    }

    public class MyScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            scrollColoredViewParallax(dy);
            if (dy > 0) {
                hideToolbarBy(dy);
            } else {
                showToolbarBy(dy);
            }
        }

        private void scrollColoredViewParallax(int dy) {
            coloredBackgroundView.setTranslationY(coloredBackgroundView.getTranslationY() - dy / 3);
        }

        private void hideToolbarBy(int dy) {
            if (cannotHideMore(dy)) {
                toolbarContainer.setTranslationY(-mTabsContainer.getBottom());
            } else {
                toolbarContainer.setTranslationY(toolbarContainer.getTranslationY() - dy);
            }
        }

        private boolean cannotHideMore(int dy) {
            return Math.abs(toolbarContainer.getTranslationY() - dy) > mTabsContainer.getBottom();
        }

        private void showToolbarBy(int dy) {
            if (cannotShowMore(dy)) {
                toolbarContainer.setTranslationY(0);
            } else {
                toolbarContainer.setTranslationY(toolbarContainer.getTranslationY() - dy);
            }
        }

        private boolean cannotShowMore(int dy) {
            return toolbarContainer.getTranslationY() - dy > 0;
        }

    }

    /**
     * ** methods of interface *****
     */


    @Override
    public void setTabLayout(SlidingTabLayout tabs) {
        mTabsContainer.addView(tabs);
    }

    @Override
    public void resetToolbar() {
        if (toolbarContainer != null && toolbarContainer.getTranslationY() < 0) {
            toolbarContainer.animate()
            .translationY(0)
            .setDuration(200)
            .start();
        }
    }

    public void setChildFragment(Fragment f) {
        BaseFragmentManager.replaceFragment(getChildFragmentManager(), R.id.toolbar_fragment_container, f, false, false);
    }

    public void addScrollableContent(RecyclerView r) {
        listener = new MyScrollListener();
        r.addOnScrollListener(listener);
    }

    public void removeScrollableContent(RecyclerView r) {
        r.removeOnScrollListener(listener);
    }

    public Toolbar getToolbar() {
        return toolbar;
    }
}
