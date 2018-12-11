package com.nightonke.githubwidget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;

/**
 * Created by Weiping on 2016/4/27.
 */

public class MySeekBar extends AppCompatSeekBar {

    public MySeekBar(Context context) {
        super(context);
    }
    public MySeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public MySeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    Drawable mThumb;
    @Override
    public void setThumb(Drawable thumb) {
        super.setThumb(thumb);
        mThumb = thumb;
    }
    public Drawable getSeekBarThumb() {
        return mThumb;
    }

}
