package com.decibel.civilianc2.controls;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.example.libcivilian.R;

/**
 * Created by dburnett on 3/30/2018.
 */

public class StyledCheckbox extends CheckBox{
    public StyledCheckbox(Context context) {
        super(context);
        init(context);
    }

    public StyledCheckbox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StyledCheckbox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context){
        try {
            if (typeface == null) {
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Exo2-Regular.ttf");
            }
        }catch (Exception e){
            typeface = Typeface.DEFAULT;
        }
        this.setTypeface(typeface);
        this.setTextColor(Color.LTGRAY);
        this.setButtonDrawable(R.drawable.unchecked_small);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isChecked()) {
                    setButtonDrawable(R.drawable.checked_small);
                    setTextColor(Color.WHITE);
                }
                else {
                    setButtonDrawable(R.drawable.unchecked_small);
                    setTextColor(Color.LTGRAY);
                }
            }
        });

    }

    @Override
    public void setChecked(boolean checked){
        super.setChecked(checked);
        if(isChecked()) {
            setButtonDrawable(R.drawable.checked_small);
            setTextColor(Color.WHITE);
        }
        else {
            setButtonDrawable(R.drawable.unchecked_small);
            setTextColor(Color.LTGRAY);
        }
    }

    private static Typeface typeface;
}

