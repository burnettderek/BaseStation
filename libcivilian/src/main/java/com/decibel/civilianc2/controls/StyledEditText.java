package com.decibel.civilianc2.controls;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by dburnett on 2/21/2018.
 */

public class StyledEditText extends EditText {
    public StyledEditText(Context context) {
        super(context);
        initilize(context);
    }

    public StyledEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initilize(context);
    }

    public StyledEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initilize(context);
    }

    public StyledEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initilize(context);
    }

    private void initilize(Context context){
        if(typeface == null){
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Exo2-Regular.ttf");
        }
        setTypeface(typeface);
    }

    private static Typeface typeface;
}
