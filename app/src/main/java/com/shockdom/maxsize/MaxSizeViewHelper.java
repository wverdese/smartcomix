/**
 * MaxSizeViewHelper.java
 * clue-android-app
 *
 * Copyright (c) 2015 BioWink GmbH. All rights reserved.
 **/

package com.shockdom.maxsize;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

/**
 * An implementation for MaxSizeViews, using decorator pattern.
 * Created by Walt on 12/05/2015.
 */
public class MaxSizeViewHelper
{
    private static final int[] ATTRS = new int[]{android.R.attr.maxWidth, android.R.attr.maxHeight};

    private Integer maxWidth;
    private Integer maxHeight;

    /** call me in constructor. **/
    public MaxSizeViewHelper(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = null;
        try {
            a = context.obtainStyledAttributes(attrs, ATTRS, defStyleAttr, 0);
            if (a.hasValue(0))
                maxWidth = a.getDimensionPixelSize(0, 0);
            if (a.hasValue(1))
                maxHeight = a.getDimensionPixelSize(1, 0);
        } finally {
            if (a != null) a.recycle();
        }
    }

    /** wrap me **/
    public Integer getMaxWidth() {
        return maxWidth;
    }

    /** wrap me passing yourself **/
    public void setMaxWidth(View v, Integer maxWidth) {
        if (this.maxWidth != maxWidth) {
            this.maxWidth = maxWidth;
            v.requestLayout();
        }
    }

    /** wrap me **/
    public Integer getMaxHeight() {
        return maxHeight;
    }

    /** wrap me passing yourself **/
    public void setMaxHeight(View v, Integer maxHeight) {
        if (this.maxHeight != maxHeight) {
            this.maxHeight = maxHeight;
            v.requestLayout();
        }
    }

    /** call me in onMeasure **/
    public int getOnMeasureWidth(int widthMeasureSpec){
        return maxWidth == null ? widthMeasureSpec : getConstrainedSize(maxWidth, widthMeasureSpec);
    }

    /** call me in onMeasure **/
    public int getOnMeasureHeight(int heightMeasureSpec){
        return maxHeight == null ? heightMeasureSpec : getConstrainedSize(maxHeight, heightMeasureSpec);
    }

    /** doing all the job **/
    private int getConstrainedSize(int maxSize, int measureSpec) {
        final int specMode = View.MeasureSpec.getMode(measureSpec);
        final int specSize = View.MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            default:
            case View.MeasureSpec.UNSPECIFIED:
                return View.MeasureSpec.makeMeasureSpec(maxSize, View.MeasureSpec.AT_MOST);

            case View.MeasureSpec.EXACTLY:
                return View.MeasureSpec.makeMeasureSpec(Math.min(specSize, maxSize), View.MeasureSpec.EXACTLY);

            case View.MeasureSpec.AT_MOST:
                return View.MeasureSpec.makeMeasureSpec(Math.min(specSize, maxSize), View.MeasureSpec.AT_MOST);
        }
    }
}
