package com.shockdom.typeface;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.shockdom.R;


public class FontTextView extends TextView {

    private static final String TAG =  FontTextView.class.getName();

	private String fontName;
	
	public FontTextView(Context context) {
        super(context);
    }

    public FontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if(isInEditMode())
    		return;
        
        parseAttributes(context, attrs);
    }

    public FontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if(isInEditMode())
    		return;
        
        parseAttributes(context, attrs);
    }

    private void parseAttributes(Context context, AttributeSet attrs) {
        TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.CustomFont);

       final int N = values.getIndexCount();
        for (int i = 0; i < N; ++i)
        {
            int attr = values.getIndex(i);
            if (attr == R.styleable.CustomFont_typeface) {
                fontName = values.getString(attr);

            }
        }
        
        values.recycle();

        //set typeface usign typeface manager for caching typefaces
        Typeface t = null;
        try {
            t = TypefaceManager.getInstance(context.getApplicationContext()).getTypefaceFromAsset(fontName);
            setTypeface(t);
        } catch (Exception e) {
            Log.e(TAG, new StringBuilder().append("TypefaceManager cannot set custom font. It should be situated in Asset folder with path:").append(" ").append(TypefaceManager.getFontAssetPath()).append(fontName).toString(), e);

        }

    }
}
