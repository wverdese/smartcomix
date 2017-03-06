/**
 * MaxSizeFrameLayout.java
 * clue-android-app
 * <p>
 * Copyright (c) 2015 BioWink GmbH. All rights reserved.
 */

package com.shockdom.maxsize;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Like {@link FrameLayout} but supports {@code android:maxHeight} and {@code android:maxWidth}.
 */
public final class MaxSizeCardView extends CardView {

    private MaxSizeViewHelper mHelper;

    public MaxSizeCardView(Context context)
    {
        this(context, null, 0);
    }

    public MaxSizeCardView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public MaxSizeCardView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        mHelper = new MaxSizeViewHelper(context, attrs, defStyleAttr);
    }

    public int getMaxWidth()
    {
        return mHelper.getMaxWidth();
    }

    public void setMaxWidth(int maxWidth) {
        mHelper.setMaxWidth(this, maxWidth);
    }

    public int getMaxHeight()
    {
        return mHelper.getMaxHeight();
    }

    public void setMaxHeight(int maxHeight) {
        mHelper.setMaxHeight(this, maxHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(mHelper.getOnMeasureWidth(widthMeasureSpec), mHelper.getOnMeasureHeight(heightMeasureSpec));
    }
}

