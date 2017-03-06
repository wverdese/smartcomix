package com.shockdom.toolbar;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

/**
 * Created by Walt on 25/05/2015.
 */
public interface ToolbarCardList {

    void setChildFragment(Fragment f);
    void addScrollableContent(RecyclerView r);
    void removeScrollableContent(RecyclerView r);
    Toolbar getToolbar();

}
