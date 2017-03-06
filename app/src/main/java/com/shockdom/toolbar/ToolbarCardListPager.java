package com.shockdom.toolbar;

import com.shockdom.tabs.SlidingTabLayout;

/**
 * Created by Walt on 25/05/2015.
 */
public interface ToolbarCardListPager extends ToolbarCardList {

    void setTabLayout(SlidingTabLayout tabs);
    void resetToolbar();

}
