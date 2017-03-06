package com.shockdom.fragment;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.shockdom.R;
import com.shockdom.activity.SmartComixActivity;
import com.shockdom.events.OnSyncData;
import com.shockdom.events.RequestSyncData;
import com.shockdom.model.SharedData;
import com.shockdom.purchase.PurchaseFragment;
import com.shockdom.tabs.SlidingTabLayout;
import com.shockdom.toolbar.ToolbarCardList;
import com.shockdom.toolbar.ToolbarCardListPager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Walt on 25/05/2015.
 */
public class VolumeTabPagerFragment extends PurchaseFragment implements ToolbarCardList {

    private SharedData data;
    private ImageView mMenuRefresh;
    private boolean isSpinning;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        WindowUtils.changeStatusBarColor(getActivity(), R.color.colorPrimaryDark);

        View v = inflater.inflate(R.layout.fragment_volume_tab_pager, container, false);

        data = SharedData.getInstance();

        if (getArguments() != null) {
            isSpinning = getArguments().getBoolean("isSpinning", false);
        } else {
            bus.post(new RequestSyncData(false));
        }

        SlidingTabLayout mSlidingTabLayout = new SlidingTabLayout(getActivity());
        //set background
        mSlidingTabLayout.setBackgroundResource(R.color.colorPrimary);
        // Set custom tab layout
        mSlidingTabLayout.setCustomTabView(R.layout.custom_tab, 0);
        // Center the tabs in the layout
        mSlidingTabLayout.setDistributeEvenly(true);
        // Customize tab color
        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.colorTabIndicator);
            }
        });

        ViewPager mViewPager = (ViewPager) v.findViewById(R.id.view_pager);

        mViewPager.setAdapter(new PagerAdapter(getChildFragmentManager()));
        mSlidingTabLayout.setViewPager(mViewPager);

        Fragment f = getParentFragment();
        if (f instanceof ToolbarCardListPager) {
            ((ToolbarCardListPager) f).setTabLayout(mSlidingTabLayout);

            mSlidingTabLayout.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageScrollStateChanged(int state) {
                    if (state == ViewPager.SCROLL_STATE_SETTLING) {
                        ((ToolbarCardListPager) getParentFragment()).resetToolbar();
                    }
                }
            });
        }

        if (f != null && f instanceof ToolbarCardList) {
            Toolbar toolbar = ((ToolbarCardList) f).getToolbar();

            ImageView logo = new ImageView(getActivity());
            logo.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            logo.setImageResource(R.drawable.smartcomix_actionbar_logo_material);
            toolbar.addView(logo);

            toolbar.inflateMenu(R.menu.menu_volumes_list);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.action_search) {
                        if (getActivity() instanceof SmartComixActivity) {
                            Fragment f = VolumeSearchListFragment.newInstance(SharedData.FILTER_NONE);
                            ((SmartComixActivity) getActivity()).setFragment(f, true, true);
                            return true;
                        }
                    }
                    return false;
                }
            });
            setupRefresh(inflater, toolbar);
        }

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isSpinning", isSpinning);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (data.isRefreshing())
            startRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (data.isRefreshing())
            stopRefresh();
    }

    public void onEvent(OnSyncData e) {
        if (isSpinning) {
            stopRefresh();
        }
    }

    private void setupRefresh(LayoutInflater inflater, Toolbar toolbar) {
        MenuItem refreshItem = toolbar.getMenu().findItem(R.id.action_refresh);
        if (refreshItem != null) {
            //inflating custom view for refresh button
            mMenuRefresh = (ImageView) inflater.inflate(R.layout.refresh_action_view, null);
            int size = getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_height_material);
            mMenuRefresh.setLayoutParams(new ViewGroup.LayoutParams(size, size));
            refreshItem.setActionView(mMenuRefresh);

            //setting click listener
            mMenuRefresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!data.isRefreshing()) {
                        startRefresh();
                        bus.post(new RequestSyncData(true));
                    }
                }
            });
        }
    }

    private void startRefresh() {
        if (mMenuRefresh != null) {
            Animation rotation = AnimationUtils.loadAnimation(getActivity(), R.anim.clockwise_refresh);
            rotation.setRepeatCount(Animation.INFINITE);
            mMenuRefresh.startAnimation(rotation);
            isSpinning = true;
        }
    }

    private void stopRefresh() {
        if (mMenuRefresh != null) {
            mMenuRefresh.clearAnimation();
            isSpinning = false;
        }
    }

    @Override
    public void setChildFragment(Fragment f) {
        if (getParentFragment() instanceof ToolbarCardList) {
            ((ToolbarCardList) getParentFragment()).setChildFragment(f);
        }
    }

    @Override
    public void addScrollableContent(RecyclerView r) {
        if (getParentFragment() instanceof ToolbarCardList) {
            ((ToolbarCardList) getParentFragment()).addScrollableContent(r);
        }
    }

    @Override
    public void removeScrollableContent(RecyclerView r) {
        if (getParentFragment() instanceof ToolbarCardList) {
            ((ToolbarCardList) getParentFragment()).removeScrollableContent(r);
        }
    }

    @Override
    public Toolbar getToolbar() {
        if (getParentFragment() instanceof ToolbarCardList) {
            return ((ToolbarCardList) getParentFragment()).getToolbar();
        }
        return null;
    }

    private class PagerAdapter extends FragmentPagerAdapter {

        class Section {
            final int filter;
            final String title;

            public Section(int filter, String titleId) {
                this.filter = filter;
                this.title = titleId;
            }
        }

        final List<Section> sections;

        public PagerAdapter(FragmentManager fm) {
            super(fm);
            sections = new ArrayList<>();
            Resources r = getResources();
            sections.add(new Section(SharedData.FILTER_SMARTCOMIX, r.getString(R.string.smartcomix)));
            sections.add(new Section(SharedData.FILTER_SMARTSONIX, r.getString(R.string.smartsonix)));
            sections.add(new Section(SharedData.FILTER_PURCHASED, r.getString(R.string.acquistati)));
            sections.add(new Section(SharedData.FILTER_SAVED, r.getString(R.string.preferiti)));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return sections.get(position).title;
        }

        @Override
        public Fragment getItem(int position) {
            return VolumeListFragment.newInstance(sections.get(position).filter);
        }

        @Override
        public int getCount() {
            return sections.size();
        }

    }
}
